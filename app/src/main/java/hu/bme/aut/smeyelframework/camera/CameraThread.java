package hu.bme.aut.smeyelframework.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import hu.bme.aut.smeyelframework.timing.Timing;

/**
 * Created on 2014.10.05..
 *
 * @author Ákos Pap
 */
public class CameraThread extends Thread {

    public static final String TAG = "CameraThread";

    private CameraPreviewHolder previewHolder;
    private Camera camera = null;
    private volatile boolean isStopped = false;

    private final List<PreviewListener> previewListeners = new ArrayList<>();
    private PictureRequest pictureRequest = null;
    private final Object lock_pictureRequest = new Object();

    public CameraThread(Context context) {
        this.previewHolder = new CameraPreviewHolder(context);

    }

    @Override
    public void run() {
        try {
            if (! safeOpenCamera()) { return; }

            camera.setPreviewCallback(previewCallback);
            camera.setPreviewDisplay(previewHolder.getHolder());
            camera.startPreview();

            while (! isStopped) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (pictureRequest != null) {
                    synchronized (lock_pictureRequest) {
                        final PictureRequest tmpPictureRequest = pictureRequest;
                        pictureRequest = null;
                        camera.takePicture(
                                new Camera.ShutterCallback() {
                                    @Override
                                    public void onShutter() {
                                        tmpPictureRequest.takenTickstamp = Timing.getCurrentTickstamp();
                                    }
                                },
                                new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        tmpPictureRequest.rawData = data;
                                    }
                                },
                                new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        tmpPictureRequest.image = data;
                                        CameraThread.this.camera.startPreview();
                                        if (tmpPictureRequest.callback != null) {
                                            tmpPictureRequest.callback.onPictureTaken(tmpPictureRequest);
                                        }
                                    }
                                }
                        );
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            camera.setPreviewCallback(null);
            previewHolder.release();
            camera.stopPreview();
//            try {
//                camera.setPreviewDisplay(null);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            releaseCamera();
            EventBus.getDefault().unregister(this);
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            synchronized (previewListeners) {
                for (PreviewListener l : previewListeners) {
                    l.onPreview(data);
                }
            }
        }
    };

    public void finish() {
        isStopped = true;
    }

    public void requestPicture(PictureRequest request) {
        synchronized (lock_pictureRequest) {
            pictureRequest = request;
        }
        this.interrupt();
    }

    private boolean safeOpenCamera() {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return safeOpenCamera(i);
            }
        }

        return false;
    }

    private boolean safeOpenCamera(int id) {
        boolean ret = false;

        try {
            camera = Camera.open(id);
            ret = (camera != null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera #" + id + "!", e);
        }

        return ret;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
            Log.d(TAG, "Released camera");
        }
    }

    public void registerPreviewListener(PreviewListener l) {
        synchronized (previewListeners) {
            if (! previewListeners.contains(l)) {
                previewListeners.add(l);
            }
        }
    }

    public void unregisterPreviewListener(PreviewListener l) {
        synchronized (previewListeners) {
            previewListeners.remove(l);
        }
    }

    public byte[] convertYuv2Jpeg(byte[] yuv) {
        Camera.Size size = this.camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(yuv, ImageFormat.NV21, size.width, size.height, null);
        Rect rectangle = new Rect(0, 0, size.width, size.height);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        image.compressToJpeg(rectangle, 100, out2);
        return out2.toByteArray();
    }

    public static interface PreviewListener {
//        void onPreview(Mat m);
        void onPreview(byte[] imgData);

    }

    public static byte[] convertYuv2Jpeg(byte[] yuv, Camera.Size size) {
        YuvImage image = new YuvImage(yuv, ImageFormat.NV21, size.width, size.height, null);
        Rect rectangle = new Rect(0, 0, size.width, size.height);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        image.compressToJpeg(rectangle, 100, out2);
        return out2.toByteArray();
    }

    public static class CameraPreviewHolder {
        private final SurfaceHolder.Callback surfaceHolderCallback;
        private Context context;

        private WindowManager windowManager;
        private SurfaceView surfaceView;
        private final WindowManager.LayoutParams params;

        public CameraPreviewHolder(Context context) {
            this.context = context;

            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            params = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            surfaceView = new SurfaceView(context);
            surfaceView.setFocusable(true);

            SurfaceHolder holder = surfaceView.getHolder();
            surfaceHolderCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            };
            holder.addCallback(surfaceHolderCallback);
            windowManager.addView(surfaceView, params);

        }

        public void release() {
            try {
                surfaceView.getHolder().removeCallback(surfaceHolderCallback);
                windowManager.removeView(surfaceView);
            } catch (Exception e) {
                // view wasn't attached...
            }
        }

        public SurfaceHolder getHolder() {
            return surfaceView.getHolder();
        }

        public void prepare() {
            windowManager.addView(surfaceView, params);
        }
    }

    public static interface PictureTakenListener {
        void onPictureTaken(PictureRequest request);
    }

    public static class PictureRequest {
        public long desiredTickstamp;
        public long takenTickstamp;
        public byte[] rawData;
        public byte[] image;
        public final PictureTakenListener callback;

        public PictureRequest(long desiredTickstamp, PictureTakenListener callback) {
            this.desiredTickstamp = desiredTickstamp;
            this.callback = callback;
        }

        public static final Comparator<? super PictureRequest> COMPARATOR = new Comparator<PictureRequest>() {
            @Override
            public int compare(PictureRequest lhs, PictureRequest rhs) {
                return (int) (lhs.desiredTickstamp - rhs.desiredTickstamp);
            }
        };
    }
}

package hu.bme.aut.smeyelframework.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import hu.bme.aut.smeyelframework.timing.Timing;

/**
* Created on 2014.10.24..
*
* @author Ákos Pap
*/
public class CameraHelper {
    public static final String TAG = "CameraHelper";

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;

    private Camera camera = null;

    private final List<PreviewListener> previewListeners = new ArrayList<>();

    private final PriorityBlockingQueue<PictureRequest> pictureRequestQueue =
            new PriorityBlockingQueue<>(2, PictureRequest.COMPARATOR);

    private QueueMonitor queueMonitor;

    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;


    public CameraHelper(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);

        mediaRecorder = new MediaRecorder();
    }

    public File startVideoRecording() {
        camera.unlock();
        mediaRecorder.reset();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        final File destinationFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mediaRecorder.setOutputFile(destinationFile.toString());

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        mediaRecorder.start();
        isRecording = true;

        return destinationFile;
    }

    public void stopVideoRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }

    private void startQueueMonitor() {
        queueMonitor = new QueueMonitor();
        queueMonitor.start();
    }

    private void stopQueueMonitor() {
        if (queueMonitor != null) {
            queueMonitor.finish();
            Log.d(TAG, "Stopped thread");
            queueMonitor.interrupt();
            Log.d(TAG, "Interrupted thread");
            try {
                queueMonitor.join(1000);
                Log.d(TAG, "Joined thread");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                safeOpenCamera();
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(previewCallback);
                camera.startPreview();
                startQueueMonitor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopQueueMonitor();
            releaseCamera();
        }
    };

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

    public void requestPicture(PictureRequest request) {
        pictureRequestQueue.add(request);
        queueMonitor.interrupt();
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
            Log.d(TAG, "opened camera #" + id);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera #" + id + "!", e);
        }
        ret = (camera != null);

        return ret;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
            Log.d(TAG, "Released camera");
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "SMEyeL/Framework");
        dir.mkdirs();

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(dir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(dir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static interface PreviewListener {
        //        void onPreview(Mat m);
        void onPreview(byte[] imgData);
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

    class QueueMonitor extends Thread {

        @Override
        public void run() {
            while (! isStopped) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    /* no need to do anything */
                }

                if (! pictureRequestQueue.isEmpty()) {
                    final PictureRequest tmpPictureRequest = pictureRequestQueue.peek();
                    if (Timing.asMillis(tmpPictureRequest.desiredTickstamp - Timing.getCurrentTickstamp()) > 2 * 1000) {
                        // next request is scheduled more than 2 seconds away. Do nothing.
                    } else {
                        pictureRequestQueue.poll(); // this one will be the next served request.
                        while (Timing.asMillis(tmpPictureRequest.desiredTickstamp - Timing.getCurrentTickstamp()) > 300) {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) { /* do nothing */ }
                        }
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
                                        if (tmpPictureRequest.callback != null) {
                                            tmpPictureRequest.callback.onPictureTaken(tmpPictureRequest);
                                        }
                                        camera.startPreview();
                                    }
                                }
                        );
                    }
                }
            }
        }

        private volatile boolean isStopped = false;
        public void finish() {
            isStopped = true;
        }
    }
}

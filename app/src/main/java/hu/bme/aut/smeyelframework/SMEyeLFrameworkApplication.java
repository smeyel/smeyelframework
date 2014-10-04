package hu.bme.aut.smeyelframework;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.HashMap;
import java.util.Map;

import hu.bme.aut.smeyelframework.events.EventActivity;
import hu.bme.aut.smeyelframework.functions.CameraPreviewActivity;
import hu.bme.aut.smeyelframework.functions.tests.TimingTestActivity;
import hu.bme.aut.smeyelframework.timing.Timing;

/**
 * Created on 2014.09.21..
 *
 * @author Ákos Pap
 */
public class SMEyeLFrameworkApplication extends Application {

    private static final String TAG = "SMEyeLFrameworkApplication";

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully!");
                Timing.init();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
//    private CameraPreviewHolder cameraPreviewHolder;

    @Override
    public void onCreate() {
        super.onCreate();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, loaderCallback);
//        cameraPreviewHolder = new CameraPreviewHolder(this);
    }

    public static int getServerPort() {
        // TODO from shared prefs (in onCreate)
        return 6000;
    }

    public static boolean isBase64Allowed() {
        // TODO from shared prefs (in onCreate)
        return false;
    }


    /** ########### Activity registry ############################################ */

    private static final Map<String, Class<? extends EventActivity>> registry = new HashMap<>();

    static {
        registerActivity("CameraPreview", CameraPreviewActivity.class);
        registerActivity("Idle", MainActivity.class);
        registerActivity("TimingTest", TimingTestActivity.class);
    }

    public static void registerActivity(String name, Class<? extends EventActivity> clazz) {
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "Can't register activity because name is empty or null!");
            return;
        }
        if (clazz == null) {
            Log.e(TAG, "Can't register activity because class is null!");
            return;
        }

        Class<? extends EventActivity> previousMapping = registry.put(name, clazz);
        if (previousMapping != null) {
            Log.w(TAG, "Registered activity for name " + name + ", but overrode previous value: " + previousMapping.getName());
        } else {
            Log.i(TAG, "Registered activity for name " + name);
        }
    }

    public static Class<? extends EventActivity> getActivity(String name) {
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "Can't get activity with empty name!");
            return null;
        }

        return registry.get(name);
    }

//    public CameraPreviewHolder getCameraPreviewHolder() {
//        return cameraPreviewHolder;
//    }
//
//    public static class CameraPreviewHolder {
//        private Context context;
//
//        private WindowManager windowManager;
//        private SurfaceView surfaceView;
//        private final WindowManager.LayoutParams params;
//
//        public CameraPreviewHolder(Context context) {
//            this.context = context;
//
//            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            params = new WindowManager.LayoutParams(1, 1,
//                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                    PixelFormat.TRANSLUCENT);
//
//            surfaceView = new SurfaceView(context);
//            surfaceView.setFocusable(true);
//
//            SurfaceHolder holder = surfaceView.getHolder();
//            holder.addCallback(new SurfaceHolder.Callback() {
//                @Override
//                public void surfaceCreated(SurfaceHolder holder) {
//
//                }
//
//                @Override
//                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//                }
//
//                @Override
//                public void surfaceDestroyed(SurfaceHolder holder) {
//
//                }
//            });
//            windowManager.addView(surfaceView, params);
//
//        }
//
//        public void release() {
//            try {
//                windowManager.removeView(surfaceView);
//            } catch (Exception e) {
//                // view wasn't attached...
//            }
//        }
//
//        public SurfaceHolder getHolder() {
//            return surfaceView.getHolder();
//        }
//
//        public void prepare() {
//            windowManager.addView(surfaceView, params);
//        }
//    }
}

package hu.bme.aut.smeyelframework;

import android.app.Application;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

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

    @Override
    public void onCreate() {
        super.onCreate();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, loaderCallback);
    }

    public static int getServerPort() {
        // TODO from shared prefs (in onCreate)
        return 6000;
    }

    public static boolean isBase64Allowed() {
        // TODO from shared prefs (in onCreate)
        return false;
    }
}

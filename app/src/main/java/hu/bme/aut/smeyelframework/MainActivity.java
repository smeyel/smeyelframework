package hu.bme.aut.smeyelframework;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import hu.bme.aut.smeyelframework.functions.CameraPreviewActivity;
import hu.bme.aut.smeyelframework.functions.LedActivity;
import hu.bme.aut.smeyelframework.functions.tests.CommTestActivity;
import hu.bme.aut.smeyelframework.functions.tests.TimingTestActivity;


public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.commTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CommTestActivity.class));
            }
        });

        findViewById(R.id.timingTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TimingTestActivity.class));
            }
        });

        findViewById(R.id.camPrevTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraPreviewActivity.class));
            }
        });

        findViewById(R.id.findLed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LedActivity.class));
            }
        });
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* OpenCV specific init, for example: enable camera view */

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("app");

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }
}

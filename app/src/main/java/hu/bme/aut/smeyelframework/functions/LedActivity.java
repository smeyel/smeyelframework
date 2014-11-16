package hu.bme.aut.smeyelframework.functions;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.camera.CameraHelper;

public class LedActivity extends Activity {

    private SurfaceView preview;
    private CameraHelper cameraHelper;

    File f = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        preview = (SurfaceView) findViewById(R.id.cameraPreview);
        cameraHelper = new CameraHelper(preview);


        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f = cameraHelper.startVideoRecording();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraHelper.isRecording()) {
                    cameraHelper.stopVideoRecording();
                    if (f != null) {
                        Toast.makeText(LedActivity.this, "Created video: " + f.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}

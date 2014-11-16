package hu.bme.aut.smeyelframework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
}

package hu.bme.aut.smeyelframework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import hu.bme.aut.smeyelframework.functions.communicationtest.CommTestActivity;
import hu.bme.aut.smeyelframework.functions.communicationtest.TimingTestActivity;


public class MainActivity extends Activity {

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
    }

}

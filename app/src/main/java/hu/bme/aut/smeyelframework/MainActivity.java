package hu.bme.aut.smeyelframework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import hu.bme.aut.smeyelframework.communication.autrar.CommunicationThread;
import hu.bme.aut.smeyelframework.functions.tests.CommTestActivity;
import hu.bme.aut.smeyelframework.functions.tests.TimingTestActivity;


public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private CommunicationThread communicationThreadRunnable;
    private Thread communicationThreadThread;

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

    @Override
    protected void onResume() {
        super.onResume();

        communicationThreadRunnable = new CommunicationThread();
        communicationThreadThread = new Thread(communicationThreadRunnable);
        communicationThreadThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (communicationThreadRunnable != null) {
            communicationThreadRunnable.stop();
            if (communicationThreadThread != null) {
                communicationThreadThread.interrupt();
            }
        }
    }
}

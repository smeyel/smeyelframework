package hu.bme.aut.smeyelframework.functions.tests;

import android.os.Bundle;
import android.util.Log;

import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.communication.autrar.StringCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.events.EventActivity;
import hu.bme.aut.smeyelframework.timing.Timing;

public class TimingTestActivity extends EventActivity {

    public static final String TAG = "TimingTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing_test);

        new Thread() {
            @Override
            public void run() {
                Timing t = Timing.instance();
                t.start("FullProcess");

                t.start("ToThePower");
                double pow = Math.pow(1234, 123);
                Log.d(TAG, "1234^123 = " + pow);
                t.stop("ToThePower");

                t.start("Sleep 1234 ms");
                try {
                    sleep(1234);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t.stop("Sleep 1234 ms");

                t.stop("FullProcess");

                RarContainer container = new RarContainer();
                container.addItem(t.getLog().pack());

                StringCommunicator sc = new StringCommunicator();
                sc.send(container);
                Log.i(TAG, sc.toString());
            }
        }.start();
    }
}

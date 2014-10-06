package hu.bme.aut.smeyelframework.events;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import de.greenrobot.event.EventBus;
import hu.bme.aut.smeyelframework.camera.CameraThread;
import hu.bme.aut.smeyelframework.communication.autrar.InboundCommunicationThread;

/**
 * Created on 2014.09.22..
 *
 * @author Ákos Pap
 */
public abstract class EventActivity extends Activity {

    public static final String TAG = "EventActivity";

    private InboundCommunicationThread communicationThread;

    private CameraThread cameraThread;

    @Override
    protected void onPause() {
        super.onPause();

        stopCommunicationThread();
        stopCameraThread();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        startCommunicationThread();
        if (needsCameraThread()) {
            startCameraThread();
        }
    }

    protected boolean needsCameraThread() {
        return false;
    }

    protected CameraThread getCameraThread() {
        return cameraThread;
    }

    public void onEvent(Events.ChangeOperatingMode event) {
        if (! this.getClass().equals(event.targetActivity)) {
            Log.d(TAG, "Switching to " + event.targetActivity.getName());
            Intent i = new Intent(this, event.targetActivity);

            if (event.args != null) {
                i.putExtras(event.args);
            }

            startActivity(i);
        } else {
            Log.d(TAG, "No need to switch to " + event.targetActivity.getName() + " already in " + this.getClass().getName());
        }
    }

    protected void startCommunicationThread() {
        communicationThread = new InboundCommunicationThread();
        communicationThread.start();
    }

    protected void startCameraThread() {
        cameraThread = new CameraThread(getApplicationContext());
        cameraThread.start();
    }

    protected void stopCommunicationThread() {
        final String TAG = EventActivity.TAG + "_comm";
        if (communicationThread != null) {
            communicationThread.finish();
            Log.d(TAG, "Stopped thread");
            communicationThread.interrupt();
            Log.d(TAG, "Interrupted thread");
            try {
                communicationThread.join(1000);
                Log.d(TAG, "Joined thread");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void stopCameraThread() {
        final String TAG = EventActivity.TAG + "_camera";
        if (cameraThread != null) {
            cameraThread.finish();
            Log.d(TAG, "Stopped thread");
            cameraThread.interrupt();
            Log.d(TAG, "Interrupted thread");
            try {
                cameraThread.join(1000);
                Log.d(TAG, "Joined thread");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void restartCommunicationThread() {
        stopCommunicationThread();
        startCommunicationThread();
    }
}

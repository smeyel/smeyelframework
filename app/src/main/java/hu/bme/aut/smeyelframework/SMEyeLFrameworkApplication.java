package hu.bme.aut.smeyelframework;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.Socket;

import hu.bme.aut.smeyelframework.communication.autrar.InboundCommunicationThread;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
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

    private static InboundCommunicationThread communicationThread;

    @Override
    public void onCreate() {
        super.onCreate();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, loaderCallback);

        registerPingMsgHandler();
    }

    private void registerPingMsgHandler() {
        MessageHandlerRepo.registerHandler(
                new MessageType(Types.Subject.PING, Types.Action.QUERY),
                new MessageHandler() {
                    @Override
                    public void handleMessage(RarItem msg, Socket socket) throws IOException {
                        RarItem pong = new RarItem();
                        pong.setSubject(Types.Subject.PONG);
                        pong.setAction(Types.Action.INFO);

                        new StreamCommunicator(socket.getOutputStream()).send(pong);
                    }
                }
        );
    }

    public static int getServerPort() {
        // TODO from shared prefs (in onCreate)
        return 6000;
    }

    public static boolean isBase64Allowed() {
        // TODO from shared prefs (in onCreate)
        return false;
    }


    private static int activeActivities = 0;
    public static void activityBorn(Activity activity) {
        activeActivities++;
        if (communicationThread == null) {
            communicationThread = new InboundCommunicationThread();
            communicationThread.start();
        }
    }

    public static void activityDied(Activity activity) {
        activeActivities--;
        if (activeActivities == 0 && communicationThread != null) {
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
}

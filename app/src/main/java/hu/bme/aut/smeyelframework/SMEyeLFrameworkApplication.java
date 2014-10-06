package hu.bme.aut.smeyelframework;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
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

                        socket.close();
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

}

package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import hu.bme.aut.smeyelframework.SMEyeLFrameworkApplication;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
import hu.bme.aut.smeyelframework.timing.MeasurementLog;
import hu.bme.aut.smeyelframework.timing.Timing;

/**
 * Created on 2014.09.21..
 *
 * @author Ákos Pap
 */
public class CommunicationThread implements Runnable {

    public static final String TAG = "CommunicationThread";

    private volatile boolean isStopped = false;

    private ServerSocket ss;
    private Timing timing;


    @Override
    public void run() {

        waitForTimingLoad();

        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(SMEyeLFrameworkApplication.getServerPort()));
            ss.setSoTimeout(500); // Interrupts the accept method to be able to stop thread.
        } catch (IOException e) {
            Log.e(TAG, "Couldn't connect to specified port!", e);
        }

        Socket s = null;
main:   while (! isStopped) {
            try {
                Log.i(TAG, "Waiting for connection on port " + ss.getLocalPort());
                while (true) {
                    try {
                        s = ss.accept();
                        break; // breaks the waiting loop, and continues with normal flow.
                    } catch (InterruptedIOException e) {
                        if (isStopped) {
                            continue main; // need to stop, continue main loop so that it ends.
                        }
                    }
                }
                Log.i(TAG, "Connected to " + s.getRemoteSocketAddress().toString());

                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();

                timing.start(Measurements.ALL);

                String message = readMessage(in);

                JSONObject jobj = new JSONObject(message);

                String subject = jobj.getString(Types.Subject.KEY);
                Log.i(TAG , "Received a " + subject + " message: " + jobj);


                RarContainer rarContainer = new RarContainer();

                switch (subject) {
                    case Types.Subject.TAKE_PICTURE: {
                        long desiredTickstamp = Timing.getCurrentTickstamp();
                        if (jobj.has(Types.Type.KEY) && Types.Type.TIMESTAMP.equals(jobj.getString(Types.Type.KEY))) {
                            long desiredTimestamp = (long) jobj.getJSONArray(Types.Misc.KEY_VALUES).getDouble(0);
                            long messageTimestamp = jobj.getLong(Types.Misc.KEY_TIMESTAMP);
                            long deltaUS = desiredTimestamp - messageTimestamp;

                            desiredTickstamp = Timing.getTickStampAtDelta(deltaUS);
                        }

                        waitForTickstamp(desiredTickstamp);

                        // TODO set mode to PICTURE_PER_REQUEST
                        // TODO take picture & fetch image bytes

                        RarItem rarItem = new RarItem();
                        rarContainer.addItem(rarItem);
                        rarItem.setAction(Types.Action.INFO);
                        rarItem.setSubject(Types.Subject.CAMERA_IMAGE);

                        if (SMEyeLFrameworkApplication.isBase64Allowed()) {
//                            TODO rarItem.setB64Data(Base64.encodeToString(imageData, Base64.DEFAULT));
                            rarItem.setB64Data(Base64.encodeToString("Here comes the image in bytes...".getBytes(), Base64.NO_WRAP));
                        } else {
//                            TODO rarItem.setBinarySize(imagedata.length);
                            rarItem.setBinarySize("Here comes the image in bytes...".getBytes().length);
                            rarContainer.addPayload("Here comes the image in bytes...".getBytes());
                        }

                        break;
                    }

                    case Types.Subject.PING: {
                        RarItem rarItem = new RarItem();
                        rarContainer.addItem(rarItem);
                        rarItem.setAction(Types.Action.CONFIRM);
                        rarItem.setSubject(Types.Subject.PONG);

                        break;
                    }

                    case Types.Subject.LOG: {
                        MeasurementLog log = timing.getLog();
                        rarContainer.addItem(log.pack());

                        break;
                    }

                }

                new StreamCommunicator(out).send(rarContainer);

                timing.stop(Measurements.ALL);

            } catch (SocketException e) {
                if (e.getMessage().contains("closed")) {
                    Log.e(TAG, "Socket is closed.", e);
                } else {
                    e.printStackTrace();
                }
                isStopped = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace(); // fatal
                    }
                }
            }
        } // while (! isStopped)

        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isStopped = true;
    }

    public boolean isStopping() {
        return isStopped;
    }

    /** Waits until the Timing module is loaded. */
    private void waitForTimingLoad() {
        while ((timing = Timing.instance()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Interruption is not a problem...
            }
        }
    }

    /**
     * Reads the message from the InputStream. A '#' marks the end of the JSON message in the stream.
     *
     * @param in The stream to read from.
     * @return The message as string, without the leading '#'.
     * @throws IOException If anything goes wrong, including premature ending of the stream.
     */
    private String readMessage(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        timing.start(Measurements.RECEPTION);

        int ch = in.read();
        while (ch != -1 && ch != '#') {
            baos.write(ch);
            ch = in.read();
        }

        if (ch == -1) {
            Log.w(TAG, "Connection terminated before reaching end of message! Received " + baos.size() + " bytes.");
            throw new IOException("Connection terminated before reaching end of message! Received " + baos.size() + " bytes.");
        }

        timing.stop(Measurements.RECEPTION);

        return baos.toString();
    }

    /**
     * Waits until system reaches the desired tickstamp.
     *
     * @param desiredTickstamp The tickstamp to wait for.
     */
    private void waitForTickstamp(long desiredTickstamp) {
        timing.start(Measurements.WAITING);
        long currentTickstamp = Timing.getCurrentTickstamp();
        while (! isStopped && desiredTickstamp > currentTickstamp) {
            if (desiredTickstamp - currentTickstamp > (5 * Core.getTickFrequency())) {
                // if desired tickstamp is more than 5 seconds's worth of ticks
                // away, sleep ~ 3s
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    // interruption is not a problem
                }
            }
        }
        timing.stop(Measurements.WAITING);
    }

    public static class Measurements {
        public static final String TAG = "Communication";

        public static final String RECEPTION = cm("ReceptionMs");
        public static final String PRE_PROCESS = cm("PreProcessMs");
        public static final String WAITING = cm("WaitingMs");
        public static final String TAKE_PICTURE = cm("TakePictureMs");
        public static final String ALL = cm("AllMs");
        public static final String ALL_NO_COMM = cm("AllNoCommMs");
        public static final String POST_PROCESS_JPEG = cm("PostProcessJpegGMs");
        public static final String POST_PROCESS_POST_JPEG = cm("PostProcessPostJpegMs");
        public static final String SEND_JSON = cm("SendingJsonMs");
        public static final String SEND_JPEG = cm("SendingJpegMs");

        private static String cm(String measurement) {
            return TAG + "::" + measurement;
        }
    }
}

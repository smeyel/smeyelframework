package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import hu.bme.aut.smeyelframework.SMEyeLFrameworkApplication;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.timing.Timing;

/**
 * Created on 2014.10.03..
 *
 * @author Ákos Pap
 */
public class InboundCommunicationThread extends Thread {

    public static final String TAG = "InboundCommunicationThread";

    private volatile boolean isStopped = false;

    private Timing timing;

    @Override
    public void run() {
        waitForTimingLoad();

        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(SMEyeLFrameworkApplication.getServerPort()));
            ss.setSoTimeout(500); // Interrupts the accept method to be able to stop thread.
        } catch (IOException e) {
            Log.e(TAG, "Couldn't connect to specified port!", e);
            return;
        }

        Socket s = null;
        while (! isStopped) {
            try {
                Log.i(TAG, "Waiting for connection on port " + ss.getLocalPort());
                while (! isStopped) {
                    try {
                        s = ss.accept();
                        break; // breaks the waiting loop, and continues with normal flow.
                    } catch (InterruptedIOException e) {

                    }
                }
                if (isStopped) { break; }
                Log.i(TAG, "Connected to " + s.getRemoteSocketAddress().toString());


                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();

                String message = readMessage(in);
                JSONObject jobj = new JSONObject(message);
                Log.d(TAG, "Received message:\n" + jobj.toString(2));

                RarItem item = BaseCommunicator.gson.fromJson(message, RarItem.class);


                MessageType mt = MessageType.fromMsg(jobj);
                MessageHandler handler = MessageHandlerRepo.getForType(mt);

                handler.handleMessage(item, s);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } // main loop

        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
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

        int ch = in.read();
        while (ch != -1 && ch != '#') {
            baos.write(ch);
            ch = in.read();
        }

        if (ch == -1) {
            Log.w(TAG, "Connection terminated before reaching end of message! Received " + baos.size() + " bytes.");
            throw new IOException("Connection terminated before reaching end of message! Received " + baos.size() + " bytes.");
        }

        return baos.toString();
    }
}

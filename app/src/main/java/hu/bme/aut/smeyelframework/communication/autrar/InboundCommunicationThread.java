package hu.bme.aut.smeyelframework.communication.autrar;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
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
            Log.e(TAG, "Couldn't open specified port!", e);
            return;
        }

        Socket s = null;
        while (! isStopped) {
            try {
                Log.i(TAG, "Waiting for connection on port " + ss.getLocalPort());
                while (!isStopped) {
                    try {
                        s = ss.accept();
                        break; // breaks the waiting loop, and continues with normal flow.
                    } catch (InterruptedIOException e) {
                        /* no need to do anything */
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (s == null || s.isClosed()) {
                continue; // unsuccessful connection, wait for a next one.
            }
            Log.i(TAG, "Connected to " + s.getRemoteSocketAddress().toString());

            while (!isStopped && s.isConnected()) {
                try {
                    InputStream in = s.getInputStream();

                    String message;
                    try {
                        message = readMessage(in); // Blocks till incoming message
                    } catch (UnfinishedJsonMessageException e) {
                        break; // possibly disconnected, finish listening and stop thread.
                    }
                    RarItem item = BaseCommunicator.gson.fromJson(message, RarItem.class);
                    Log.d(TAG, "Received message:\n" + item.toPrettyString());

                    MessageType mt = MessageType.fromMsg(item);
                    MessageHandler handler = MessageHandlerRepo.getForType(mt);

                    Log.d(TAG, "Executing a handler for the message");
                    new AsyncMessageHandler(handler, item, s).execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Disconnected.");
        }

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
            throw new UnfinishedJsonMessageException("Connection terminated before reaching end of message! Received " + baos.size() + " bytes.");
        }

        return baos.toString();
    }

    private static class AsyncMessageHandler extends AsyncTask<Void, Void, Void> {

        private MessageHandler handler;
        private RarItem item;
        private Socket s;

        private AsyncMessageHandler(MessageHandler handler, RarItem item, Socket s) {
            this.handler = handler;
            this.item = item;
            this.s = s;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                handler.handleMessage(item, s);
            } catch (IOException e) {
                Log.e("AsyncMessageHandler", "Failed to handle message!", e);
            }

            return null;
        }
    }

    private static class UnfinishedJsonMessageException extends IOException {
        public UnfinishedJsonMessageException() {}

        public UnfinishedJsonMessageException(String detailMessage) {
            super(detailMessage);
        }

        public UnfinishedJsonMessageException(String message, Throwable cause) {
            super(message, cause);
        }

        public UnfinishedJsonMessageException(Throwable cause) {
            super(cause == null ? null : cause.toString(), cause);
        }
    }
}

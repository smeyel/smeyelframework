package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;

import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;

/**
 * Handles outbound RAR communication on a given {@link java.io.OutputStream}.
 *
 * <p>
 * Created on 2014.09.18..
 * @author Ákos Pap
 */
public abstract class BaseCommunicator {

    public static final String TAG = "SMEyeL::Framework::Communicator";

    public static final Gson gson;
    static {
        GsonBuilder builder = new GsonBuilder();
//        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        // see http://stackoverflow.com/a/16558757 for disableHtmlEscaping
        gson = builder.disableHtmlEscaping().create();
    }

    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Descendants should close the stream here, if necessary.
     */
    protected void cleanup() {
        // Override if required
    }

    /**
     * Serializes the container's JSON data, appends a '#' character,
     * then writes out the raw payload(s) if any.
     *
     * @param container The RarContainer to send.
     */
    public void send(RarContainer container) {

        OutputStream os = null;

        try {
            Log.d(TAG, "Writing a container with " + container.getItems().size() + " items and "
                    + container.getPayloads().size() + " payloads.");
            os = getOutputStream();

            String msg = gson.toJson(container.getItems()) + '#';

            os.write(msg.getBytes());

            for (int i = 0; i < container.getPayloads().size(); i++) {
                os.write(container.getPayloads().get(i));
            }

            os.flush();
        } catch (IOException e) {
            Log.e(TAG, "IOException...", e);
        } finally {
           cleanup();
        }
    }
}

package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;

import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;

/**
 * Created on 2014.09.18..
 *
 * @author Ákos Pap
 */
public abstract class BaseCommunicator {

    public static final String TAG = "SMEyeL::Framework::Communicator";

    static final Gson gson;
    static {
        GsonBuilder builder = new GsonBuilder();
//        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        gson = builder.create();
    }

    protected abstract OutputStream getOutputStream() throws IOException;

    protected void cleanup() {
        // Override if required
    }

    public void send(RarContainer container) {

        OutputStream os = null;

        try {
            os = getOutputStream();

            String msg = gson.toJson(container.getItems());

            os.write(msg.getBytes());

            for (int i = 0; i < container.getPayloads().size(); i++) {
                os.write(container.getPayloads().get(i));
            }

            os.flush();
        } catch (IOException e) {
            Log.e(TAG, "IOException...", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace(); // fatal...
                }
            }

            cleanup();
        }
    }
}

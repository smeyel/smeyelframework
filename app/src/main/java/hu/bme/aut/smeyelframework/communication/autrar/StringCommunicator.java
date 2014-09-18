package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 2014.09.18..
 *
 * @author Ákos Pap
 */
public class StringCommunicator extends BaseCommunicator {

    public static final String TAG = "SMEyeL::Framework::SocketCommunicator";

    ByteArrayOutputStream baos;

    public StringCommunicator() {
        this.baos = new ByteArrayOutputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return baos;
    }

    @Override
    protected void cleanup() {
        Log.i(TAG, "Content: " + baos.toString());
    }
}

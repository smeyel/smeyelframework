package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Communicates through a socket with the given address and port.
 *
 * Created on 2014.09.18..
 * @author Ákos Pap
 */
public class SocketCommunicator extends BaseCommunicator {

    public static final String TAG = "SMEyeL::Framework::SocketCommunicator";

    private String serverAddress;
    private int serverPort;
    private Socket socket;

    public SocketCommunicator(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        try {
            socket = new Socket(serverAddress, serverPort);
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host: " + serverAddress + ":" + serverPort, e);
        } catch (IOException e) {
            Log.e(TAG, "IOException...", e);
        }
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        if (socket != null && socket.isConnected()) {
            return socket.getOutputStream();
        } else {
            throw new IOException("Socket is null, or is not connected!");
        }
    }

    @Override
    protected void cleanup() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(); // fatal
            }
        }
    }
}

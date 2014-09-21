package hu.bme.aut.smeyelframework.communication.autrar;

import java.io.IOException;
import java.io.OutputStream;

/**
 *  Redirects the message through the provided {@link java.io.OutputStream}.
 *
 * <p>
 * Created on 2014.09.21..
 * @author Ákos Pap
 */
public class StreamCommunicator extends BaseCommunicator {

    OutputStream stream;

    public StreamCommunicator(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return stream;
    }
}

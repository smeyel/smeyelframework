package hu.bme.aut.smeyelframework.communication.autrar;

import java.io.IOException;
import java.net.Socket;

import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;

/**
* Created on 2014.10.03..
*
* @author Ákos Pap
*/
public interface MessageHandler {

    /**
     * Handle the incoming message, and send the response through {@code out}.
     * <strong>Implementations MUST close the stream themselves!</strong>
     * <p>
     * Implementations should do their stuff fast, or asynchronously, so that
     * the main comm. thread doesn't block. TODO hardcode async?
     *
     * @param msg The incoming message.
     * @param socket The stream to the sender of the message.
     * @throws IOException If writing to the stream fails.
     */
    void handleMessage(RarItem msg, Socket socket) throws IOException;
}

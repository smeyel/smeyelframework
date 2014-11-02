package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;

/**
* Created on 2014.10.03..
*
* @author Ákos Pap
*/
public class MessageHandlerRepo {
    public static final String TAG = "MessageHandlerRepo";

    private static final Map<MessageType, MessageHandler> map;

    static {
        map = new HashMap<>();
    }

    public static void registerHandler(MessageType type, MessageHandler handler) {
        if (map.containsKey(type)) {
            Log.w(TAG, "Replacing handler for " + type);
        }

        map.put(type, handler);
    }

    public static void unregisterHandler(MessageType type) {
        map.remove(type);
    }

    public static MessageHandler getForType(MessageType type) {
        if (! map.containsKey(type) || map.get(type) == null) {
            return new NullOrNotFoundHandler();
        }

        return map.get(type);
    }

    private static class NullOrNotFoundHandler implements MessageHandler {

        @Override
        public void handleMessage(RarItem msg, Socket socket) throws IOException {
            Log.e("NullOrNotFoundHandler", "Message type is null, or there are no registered " +
                    "handlers for this message: " + msg);

            RarItem item = new RarItem();
            item.setAction(Types.Action.ERROR);
            item.setText("ERROR! There are no handlers registered for this message!");

            new StreamCommunicator(socket.getOutputStream()).send(item);
        }
    }
}

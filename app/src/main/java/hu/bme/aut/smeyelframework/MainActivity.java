package hu.bme.aut.smeyelframework;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.Socket;

import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
import hu.bme.aut.smeyelframework.events.EventActivity;
import hu.bme.aut.smeyelframework.functions.tests.CommTestActivity;
import hu.bme.aut.smeyelframework.functions.tests.TimingTestActivity;


public class MainActivity extends EventActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.commTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CommTestActivity.class));
            }
        });

        findViewById(R.id.timingTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TimingTestActivity.class));
            }
        });


        MessageType takePicture = new MessageType(Types.Subject.TAKE_PICTURE, Types.Action.COMMAND);
        MessageHandlerRepo.registerHandler(takePicture, new MessageHandler() {
            @Override
            public void handleMessage(RarItem msg, Socket socket) throws IOException {
                Log.d(TAG, "Taking picture...");
                byte[] img = "Hello!".getBytes();

                RarContainer container = new RarContainer();
                RarItem item = new RarItem();
                item.setSubject(Types.Subject.TAKE_PICTURE);
                item.setAction(Types.Action.INFO);
                item.setBinarySize(img.length);

                container.addItem(item);
                container.addPayload(img);

                new StreamCommunicator(socket.getOutputStream()).send(container);

                socket.close();
            }
        });
    }
}

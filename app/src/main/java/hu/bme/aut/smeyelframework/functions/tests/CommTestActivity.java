package hu.bme.aut.smeyelframework.functions.tests;

import android.app.Activity;
import android.os.Bundle;

import java.util.Arrays;

import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.communication.autrar.BaseCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.StringCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;

public class CommTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_test);

        RarItem item = new RarItem();
        item.setAction(Types.Action.INFO);
        item.setSubject(Types.Subject.OBSTACLE);
        item.setType(Types.Type.RECT_2D);
        item.setValues(Arrays.asList(55.0, 40.0, 200.0, 110.0));
        item.setMessageID(55);
        item.setTimestamp(1522651885l);

        RarContainer cont = new RarContainer();
        cont.addItem(item);

        BaseCommunicator comm = new StringCommunicator();

        comm.send(cont);
    }
}

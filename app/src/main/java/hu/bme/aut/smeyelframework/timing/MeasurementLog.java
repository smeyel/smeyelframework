package hu.bme.aut.smeyelframework.timing;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;

/**
 * A thread safe container for {@link hu.bme.aut.smeyelframework.timing.LogItem}s.
 * Call {@link #pack()} to convert to a {@link hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer}.
 *
 * <p>
 * Created on 2014.09.21..
 * @author Ákos Pap
 */
public class MeasurementLog {

    public static final String TAG = "MeasurementLog";

    /** Stores the LogItems. */
    private Vector<LogItem> data;

    public MeasurementLog() {
        data = new Vector<>(10);
    }

    /** Pushes a new LogItem to the end of the store. */
    public void push(LogItem logItem) {
        data.add(logItem);
    }

    public void clear() {
        data.clear();
    }

    /**
     * Constructs a new {@link hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer}
     * that has a single item. THat item has the type {@link hu.bme.aut.smeyelframework.communication.autrar.model.Types.Type#MEASUREMENT_LOG}
     * and in the items field the list of LogItems.
     *
     * @return The RarContainer.
     */
    public RarContainer pack() {
        RarItem rarItem = new RarItem();

        rarItem.setType(Types.Type.MEASUREMENT_LOG);
        List<LogItem> items = new ArrayList<LogItem>(data);
        rarItem.setItems(items);

        RarContainer container = new RarContainer();
        container.addItem(rarItem);

        return container;
    }

    public void send(OutputStream os) {
        StreamCommunicator comm = new StreamCommunicator(os);

        comm.send(this.pack());
    }
}

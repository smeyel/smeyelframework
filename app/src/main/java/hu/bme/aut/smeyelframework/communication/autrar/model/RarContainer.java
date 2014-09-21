package hu.bme.aut.smeyelframework.communication.autrar.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for Rar items and for optional raw payloads.
 *
 * Created on 2014.09.18..
 * @author Ákos Pap
 */
public class RarContainer {

    List<RarItem> items = new ArrayList<RarItem>();

    transient // so that Gson doesn't try to serialize it. There's no @Exclude annotation
    List<byte[]> payloads = new ArrayList<byte[]>();

    public void addItem(RarItem item) {
        if (items == null) {
            items = new ArrayList<RarItem>();
        }

        items.add(item);
    }

    public void addPayload(byte[] payload) {
        if (payloads == null) {
            payloads = new ArrayList<byte[]>();
        }

        payloads.add(payload);
    }

    public List<RarItem> getItems() {
        return items;
    }

    public void setItems(List<RarItem> items) {
        this.items = items;
    }

    public List<byte[]> getPayloads() {
        return payloads;
    }

    public void setPayloads(List<byte[]> payloads) {
        this.payloads = payloads;
    }
}

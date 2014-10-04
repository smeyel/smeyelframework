package hu.bme.aut.smeyelframework.communication.autrar;

import android.text.TextUtils;
import android.util.Log;

import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;

/**
* Created on 2014.10.03..
*
* @author Ákos Pap
*/
public class MessageType {
    public String subject;
    public String action;

    public static MessageType fromMsg(RarItem item) {
        MessageType mt = null;
        if (! TextUtils.isEmpty(item.getSubject()) && ! TextUtils.isEmpty(item.getAction())) {
                mt = new MessageType(item.getSubject(), item.getAction());
        } else {
            Log.w("MessageType", "Message object is not a valid message! " + item.toString());
        }

        return mt;
    }

    public MessageType(String subject, String action) {
        this.subject = subject;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MessageType) {
            MessageType other = (MessageType) o;
            return subject.equalsIgnoreCase(other.subject)
                    && action.equalsIgnoreCase(other.action);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + subject.hashCode();
        hash = hash * 31 + action.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "[" + subject + " - " + action + "]";
    }
}

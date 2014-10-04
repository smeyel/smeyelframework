package hu.bme.aut.smeyelframework.communication.autrar;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import hu.bme.aut.smeyelframework.communication.autrar.model.Types;

/**
* Created on 2014.10.03..
*
* @author Ákos Pap
*/
public class MessageType {
    public String subject;
    public String action;

    public static MessageType fromMsg(JSONObject jobj) {
        MessageType mt = null;
        if (jobj.has(Types.Subject.KEY) && jobj.has(Types.Action.KEY)) {
            try {
                mt = new MessageType(jobj.getString(Types.Subject.KEY), jobj.getString(Types.Action.KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.w("MessageType", "Message object is not a valid message! " + jobj.toString());
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

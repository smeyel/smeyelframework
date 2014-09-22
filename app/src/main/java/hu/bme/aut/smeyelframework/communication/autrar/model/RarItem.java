package hu.bme.aut.smeyelframework.communication.autrar.model;

import java.util.List;

/**
 * A single message according to the AUT Rar specification.
 *
 * <p>
 * Created on 2014.09.18..
 * @author Ákos Pap
 */
public class RarItem {
    protected String action;
    protected String subject;
    protected Integer subjectID;
    protected String type;
    protected List<Double> values;
    protected Integer messageID;
    protected Long timestamp;
    protected Integer binarySize;
    protected String b64Data;
    protected String text;
    protected String parentMessageID;
    protected List<?> items;

    public RarItem() {
        timestamp = System.currentTimeMillis();
    }

    public RarItem(boolean noInitTimestamp) {
        if (! noInitTimestamp) {
            timestamp = System.currentTimeMillis();
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(Integer subjectID) {
        this.subjectID = subjectID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public Integer getMessageID() {
        return messageID;
    }

    public void setMessageID(Integer messageID) {
        this.messageID = messageID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getBinarySize() {
        return binarySize;
    }

    public void setBinarySize(Integer binarySize) {
        this.binarySize = binarySize;
    }

    public String getB64Data() {
        return b64Data;
    }

    public void setB64Data(String b64Data) {
        this.b64Data = b64Data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getParentMessageID() {
        return parentMessageID;
    }

    public void setParentMessageID(String parentMessageID) {
        this.parentMessageID = parentMessageID;
    }

    public List<?> getItems() {
        return items;
    }

    public void setItems(List<?> items) {
        this.items = items;
    }
}

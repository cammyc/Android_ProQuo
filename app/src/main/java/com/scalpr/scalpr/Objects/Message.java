package com.scalpr.scalpr.Objects;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Cam on 10/16/2016.
 */
public class Message implements Serializable{
    private long ID;
    private long conversationID;
    private long senderID;
    private String text;
    private Date timeStamp;

    public Message(){
        ID = -1;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getConversationID() {
        return conversationID;
    }

    public void setConversationID(long conversationID) {
        this.conversationID = conversationID;
    }

    public long getSenderID() {
        return senderID;
    }

    public void setSenderID(long senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}

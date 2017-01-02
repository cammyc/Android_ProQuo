package com.scalpr.scalpr.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Cam on 10/16/2016.
 */
public class Conversation implements Serializable{
    private long ID;
    private long attractionID;
    private long buyerID;
    private long sellerID;
    private String buyerName;
    private String sellerName;
    private String title;
    private String attractionImageURL;
    private Message lastMessage;
    private ArrayList<Message> messages;
    private Date creationTimeStamp;
    private boolean isLastMessageRead;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getAttractionID() {
        return attractionID;
    }

    public void setAttractionID(long attractionID) {
        this.attractionID = attractionID;
    }

    public long getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(long buyerID) {
        this.buyerID = buyerID;
    }

    public long getSellerID() {
        return sellerID;
    }

    public void setSellerID(long sellerID) {
        this.sellerID = sellerID;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAttractionImageURL() {
        return attractionImageURL;
    }

    public void setAttractionImageURL(String attractionImageURL) {
        this.attractionImageURL = attractionImageURL;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public Date getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(Date creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public boolean isLastMessageRead() {
        return isLastMessageRead;
    }

    public void setLastMessageRead(boolean lastMessageRead) {
        isLastMessageRead = lastMessageRead;
    }
}

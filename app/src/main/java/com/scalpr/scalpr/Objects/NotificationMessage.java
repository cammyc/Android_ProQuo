package com.scalpr.scalpr.Objects;

/**
 * Created by Cam on 11/19/2016.
 */
public class NotificationMessage {

    private long messageID;
    private String message;
    private long convoID;
    private String yourName;
    private String imageURL;
    private String attractionName;

    public NotificationMessage(){
        messageID = -1;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getConvoID() {
        return convoID;
    }

    public void setConvoID(long convoID) {
        this.convoID = convoID;
    }

    public String getYourName() {
        return yourName;
    }

    public void setYourName(String yourName) {
        this.yourName = yourName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }
}

package com.scalpr.scalpr.Objects;

/**
 * Created by Cam on 9/20/2016.
 */
public class Attraction {
    private long ID;
    private long creatorID;
    private String venueName;
    private String name;
    private double ticketPrice;
    private int numTickets;
    private String description;
    private String date;
    private String imageURL;
    private double lat;
    private double lon;
    private String timeStamp;
    private User user;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(long creatorID) {
        this.creatorID = creatorID;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getNumTickets() {
        return numTickets;
    }

    public void setNumTickets(int numTickets) {
        this.numTickets = numTickets;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void copyAttractionSerializable(AttractionSerializable ap){
        this.ID = ap.getID();
        this.creatorID = ap.getCreatorID();
        this.venueName = ap.getVenueName();
        this.name = ap.getName();
        this.ticketPrice = ap.getTicketPrice();
        this.numTickets = ap.getNumTickets();
        this.description = ap.getDescription();
        this.date = ap.getDate();
        this.imageURL = ap.getImageURL();
        this.lat = ap.getLat();
        this.lon = ap.getLon();
        this.user = ap.getUser();
    }

    public AttractionSerializable toSerializable(){
        AttractionSerializable attrSer = new AttractionSerializable();

        attrSer.setID(this.getID());
        attrSer.setCreatorID(this.getCreatorID());
        attrSer.setVenueName(this.getVenueName());
        attrSer.setName(this.getName());
        attrSer.setTicketPrice(this.getTicketPrice());
        attrSer.setNumTickets(this.getNumTickets());
        attrSer.setDescription(this.getDescription());
        attrSer.setDate(this.getDate());
        attrSer.setImageURL(this.getImageURL());
        attrSer.setLat(this.getLat());
        attrSer.setLon(this.getLon());
        attrSer.setUser(this.getUser());

        return attrSer;
    }
}

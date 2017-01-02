package com.scalpr.scalpr.Objects;

import android.os.Parcel;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by Cam on 9/18/2016.
 */
public class User implements Serializable{
    private long UserID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String displayPicURL;

    public long getUserID() {
        return UserID;
    }

    public void setUserID(long userID) {
        UserID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayPicURL() {
        return displayPicURL;
    }

    public void setDisplayPicURL(String displayPicURL) {
        this.displayPicURL = displayPicURL;
    }
}

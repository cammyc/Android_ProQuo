package com.scalpr.scalpr.Objects;

import java.util.Calendar;

/**
 * Created by Cam on 4/5/2017.
 */

public class Filters {
    private String startDate;
    private String endDate;
    private boolean showRequested;
    private boolean showSelling;
    private int minPrice;
    private int maxPrice;
    private int numTickets;

    public Filters(){
        Calendar mcurrentDate=Calendar.getInstance();
        final int mYear=mcurrentDate.get(Calendar.YEAR);
        final int mMonth=mcurrentDate.get(Calendar.MONTH);
        final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        startDate = mYear + "-" + (mMonth + 1) + "-" + mDay;
        endDate = (mYear + 1) + "-" + (mMonth + 1) + "-" + mDay;
        showRequested = true;
        showSelling = true;
        minPrice = 0;
        maxPrice = 1000;
        numTickets = -1;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isShowRequested() {
        return showRequested;
    }

    public void setShowRequested(boolean showRequested) {
        this.showRequested = showRequested;
    }

    public boolean isShowSelling() {
        return showSelling;
    }

    public void setShowSelling(boolean showSelling) {
        this.showSelling = showSelling;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getNumTickets() {
        return numTickets;
    }

    public void setNumTickets(int numTickets) {
        this.numTickets = numTickets;
    }
}

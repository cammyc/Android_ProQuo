package com.scalpr.scalpr.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lapism.searchview.SearchItem;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.Message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cam on 9/21/2016.
 */
public class DatabaseHelper {

    Context c;
    DbInitializer mDB;
    public static boolean dbUpdatedSinceLastCheck = false; //this is only for attraction Table

    public DatabaseHelper(Context _c){
        c = _c;
        mDB = new DbInitializer(c);
    }

    public DatabaseHelper(Context _c, boolean truncate){
        c = _c;
        mDB = new DbInitializer(c);

        if(truncate)
            mDB.getWritableDatabase().execSQL("DELETE FROM " + DbInitializer.FeedEntry.TABLE_NAME);
    }

    public static boolean hasdbUpdatedSinceLastCheck(){
        boolean hasChanged = dbUpdatedSinceLastCheck;
        dbUpdatedSinceLastCheck = false;
        return hasChanged;
    }

    public void addAttractionsToDB(ArrayList<Attraction> attractions){
        SQLiteDatabase db = mDB.getWritableDatabase();

        for(int i = 0; i < attractions.size(); i++){
            Attraction a = attractions.get(i);
            ContentValues values = new ContentValues();
            values.put(DbInitializer.FeedEntry.COLUMN_ID, a.getID());
            values.put(DbInitializer.FeedEntry.COLUMN_CREATORID,a.getCreatorID());
            values.put(DbInitializer.FeedEntry.COLUMN_VENUENAME,a.getVenueName());
            values.put(DbInitializer.FeedEntry.COLUMN_NAME,a.getName());
            values.put(DbInitializer.FeedEntry.COLUMN_TICKETPRICE,a.getTicketPrice());
            values.put(DbInitializer.FeedEntry.COLUMN_NUMBEROFTICKETS,a.getNumTickets());
            values.put(DbInitializer.FeedEntry.COLUMN_DESCRIPTION,a.getDescription());
            values.put(DbInitializer.FeedEntry.COLUMN_DATE,a.getDate());
            values.put(DbInitializer.FeedEntry.COLUMN_IMAGEURL,a.getImageURL());
            values.put(DbInitializer.FeedEntry.COLUMN_LAT,a.getLat());
            values.put(DbInitializer.FeedEntry.COLUMN_LON,a.getLon());
            values.put(DbInitializer.FeedEntry.COLUMN_TIMESTAMP,a.getTimeStamp());
            values.put(DbInitializer.FeedEntry.COLUMN_POSTTYPE, a.getPostType());
            long id = db.insert(DbInitializer.FeedEntry.TABLE_NAME, null, values);

        }
        
        db.close();
    }

    public void addMessagesToDB(ArrayList<Message> messages){
        SQLiteDatabase db = mDB.getWritableDatabase();
        try {
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                if (m.getID() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_ID, m.getID());
                    values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID, m.getConversationID());
                    values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_SENDER_ID, m.getSenderID());
                    values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_TEXT, m.getText());
                    values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_TIMESTAMP, m.getTimeStamp().toString());
                    long id = db.insert(DbInitializer.FeedEntry.MESSAGES_TABLE_NAME, null, values);
                    Log.d("INSERT", id + "");
                }
            }

        }catch (Exception ex){

        }
        db.close();
    }

    public void addMessageToDB(Message m){
        SQLiteDatabase db = mDB.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_ID, m.getID());
        values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID,m.getConversationID());
        values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_SENDER_ID, m.getSenderID());
        values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_TEXT, m.getText());
        values.put(DbInitializer.FeedEntry.COLUMN_MESSAGE_TIMESTAMP, m.getTimeStamp().toString());
        long id = db.insert(DbInitializer.FeedEntry.MESSAGES_TABLE_NAME, null, values);

        db.close();
    }

    public boolean updateAttractionDetails(Attraction a){
        SQLiteDatabase db = mDB.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbInitializer.FeedEntry.COLUMN_VENUENAME,a.getVenueName());
        values.put(DbInitializer.FeedEntry.COLUMN_NAME,a.getName());
        values.put(DbInitializer.FeedEntry.COLUMN_TICKETPRICE,a.getTicketPrice());
        values.put(DbInitializer.FeedEntry.COLUMN_NUMBEROFTICKETS,a.getNumTickets());
        values.put(DbInitializer.FeedEntry.COLUMN_DESCRIPTION,a.getDescription());
        values.put(DbInitializer.FeedEntry.COLUMN_DATE,a.getDate());
        values.put(DbInitializer.FeedEntry.COLUMN_IMAGEURL,a.getImageURL());
        values.put(DbInitializer.FeedEntry.COLUMN_POSTTYPE, a.getPostType());

        int didUpdate = db.update(DbInitializer.FeedEntry.TABLE_NAME, values, DbInitializer.FeedEntry.COLUMN_ID + " = " + a.getID(), null);

        db.close();
        dbUpdatedSinceLastCheck = true;
        return (didUpdate > 0);
    }

    public boolean deleteAttraction(long attractionID){
        SQLiteDatabase db = mDB.getReadableDatabase();

        int didUpdate = db.delete(DbInitializer.FeedEntry.TABLE_NAME, DbInitializer.FeedEntry.COLUMN_ID + " = " + attractionID, null);

        db.close();
        dbUpdatedSinceLastCheck = true;
        return (didUpdate > 0);
    }

    public boolean updateAttractionLocation(Attraction a){

        SQLiteDatabase db = mDB.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbInitializer.FeedEntry.COLUMN_LAT,a.getLat());
        values.put(DbInitializer.FeedEntry.COLUMN_LON,a.getLon());

        int didUpdate = db.update(DbInitializer.FeedEntry.TABLE_NAME, values, DbInitializer.FeedEntry.COLUMN_ID + " = " + a.getID(), null);
        db.close();
        dbUpdatedSinceLastCheck = true;
        return (didUpdate > 0);
    }

    public ArrayList<Message> getMessagesFromDB(long conversationID, long greaterThanMessageID){
        ArrayList<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = mDB.getReadableDatabase();
        String[] columns = new String[]{"*"};

        String venueAttractionSearch = "(" + DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID + " = ? AND " + DbInitializer.FeedEntry.COLUMN_MESSAGE_ID +" > ?) ORDER BY " + DbInitializer.FeedEntry.COLUMN_MESSAGE_ID + " DESC";
        String[] whereArgs = new String[] {conversationID+"", greaterThanMessageID+""};

        Cursor c = db.query(DbInitializer.FeedEntry.MESSAGES_TABLE_NAME, columns, venueAttractionSearch, whereArgs, null, null, null);

        try {
            while (c.moveToNext()) {
                Message m = new Message();

                m.setID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_ID)));
                m.setConversationID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID)));
                m.setSenderID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_SENDER_ID)));
                m.setText(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_TEXT)));
                m.setTimeStamp(MiscHelper.parseUTCStringToDate(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_TIMESTAMP)), "EEE MMM dd HH:mm:ss zzz yyyy"));
                //get all messages in db for conversation then only select new messages where ID is > whats in db already.

                messages.add(m);
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return messages;
    }

    public Message getMessageFromDB(long conversationID, long greaterThanMessageID){
        Message m = new Message();
        SQLiteDatabase db = mDB.getReadableDatabase();
        String[] columns = new String[]{"*"};

        String venueAttractionSearch = "(" + DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID + " = ? AND " + DbInitializer.FeedEntry.COLUMN_MESSAGE_ID +" = ?) ORDER BY " + DbInitializer.FeedEntry.COLUMN_MESSAGE_ID + " DESC";
        String[] whereArgs = new String[] {conversationID+"", greaterThanMessageID+""};

        Cursor c = db.query(DbInitializer.FeedEntry.MESSAGES_TABLE_NAME, columns, venueAttractionSearch, whereArgs, null, null, null);

        try {
            while (c.moveToNext()) {
                m.setID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_ID)));
                m.setConversationID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID)));
                m.setSenderID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_SENDER_ID)));
                m.setText(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_TEXT)));
                m.setTimeStamp(MiscHelper.parseUTCStringToDate(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_MESSAGE_TIMESTAMP)), "EEE MMM dd HH:mm:ss zzz yyyy"));
                //get all messages in db for conversation then only select new messages where ID is > whats in db already.
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return m;
    }

    public boolean deleteMessagesFromConversation(String where){
        SQLiteDatabase db = mDB.getWritableDatabase();

        int didUpdate = db.delete(DbInitializer.FeedEntry.MESSAGES_TABLE_NAME, where, null);

        db.close();
        return (didUpdate > 0);
    }



    public ArrayList<Attraction> getAttractionsFromDB(String query){
        ArrayList<Attraction> attractions = new ArrayList<Attraction>();
        SQLiteDatabase db = mDB.getReadableDatabase();
        String[] columns = new String[]{"*"};

        String venueAttractionSearch = "";
        String[] whereArgs = new String[]{};

        if(!query.equals("")){
            venueAttractionSearch = "(VenueName LIKE ? OR Name LIKE ?)";
            whereArgs = new String[] {
                    query+"%",
                    query+"%"
            };
        }

        //Cursor c = db.rawQuery("SELECT * FROM " + DbInitializer.FeedEntry.TABLE_NAME + venueAttractionSearch, null);
        Cursor c = db.query(DbInitializer.FeedEntry.TABLE_NAME, columns, venueAttractionSearch, whereArgs, null, null, null);

        try {
            while (c.moveToNext()) {
                Attraction a = new Attraction();

                a.setID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_ID)));
                a.setCreatorID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_CREATORID)));
                a.setVenueName(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_VENUENAME)));
                a.setName(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_NAME)));
                a.setTicketPrice(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_TICKETPRICE)));
                a.setNumTickets(c.getInt(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_NUMBEROFTICKETS)));
                a.setDescription(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_DESCRIPTION)));
                a.setDate(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_DATE)));
                a.setImageURL(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_IMAGEURL)));
                a.setLat(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_LAT)));
                a.setLon(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_LON)));
                a.setTimeStamp(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_TIMESTAMP)));
                a.setPostType(c.getInt(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_POSTTYPE)));
                attractions.add(a);
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return attractions;
    }

    /**
     * never let query be based off user input
     * @param query to search by
     * @return list of attractions in local database
     */
    public ArrayList<Attraction> getAttractionsFromDBFreeQuery(String query){
        ArrayList<Attraction> attractions = new ArrayList<Attraction>();
        SQLiteDatabase db = mDB.getReadableDatabase();
        String[] columns = new String[]{"*"};

        //Cursor c = db.rawQuery("SELECT * FROM " + DbInitializer.FeedEntry.TABLE_NAME + venueAttractionSearch, null);
        Cursor c = db.rawQuery(query, null);

        try {
            while (c.moveToNext()) {
                Attraction a = new Attraction();

                a.setID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_ID)));
                a.setCreatorID(c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_CREATORID)));
                a.setVenueName(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_VENUENAME)));
                a.setName(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_NAME)));
                a.setTicketPrice(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_TICKETPRICE)));
                a.setNumTickets(c.getInt(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_NUMBEROFTICKETS)));
                a.setDescription(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_DESCRIPTION)));
                a.setDate(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_DATE)));
                a.setImageURL(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_IMAGEURL)));
                a.setLat(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_LAT)));
                a.setLon(c.getDouble(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_LON)));
                a.setTimeStamp(c.getString(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_TIMESTAMP)));
                a.setPostType(c.getInt(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_POSTTYPE)));

                attractions.add(a);
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return attractions;
    }

    public List<SearchItem> getSearchSuggestions(){
        List<SearchItem> suggestionsList = new ArrayList<>();
        SQLiteDatabase db = mDB.getReadableDatabase();

        //not vulnerable to sqlite injection but i guess this is good practice
        Cursor c = db.query(DbInitializer.FeedEntry.TABLE_NAME, new String[]{DbInitializer.FeedEntry.COLUMN_NAME}, null, null, DbInitializer.FeedEntry.COLUMN_NAME, null, null);
        //Cursor c = db.rawQuery("SELECT " + DbInitializer.FeedEntry.COLUMN_NAME+" FROM " + DbInitializer.FeedEntry.TABLE_NAME + " GROUP BY " + DbInitializer.FeedEntry.COLUMN_NAME, null);

        String[] usedStrings = new String[c.getCount()];
        int i = 0;
        try {
            while (c.moveToNext()) {
                String name = c.getString(0);
                SearchItem itemName = new SearchItem(name);
                suggestionsList.add(itemName);
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        return suggestionsList;
    }

    public ArrayList<Long> getAttractionIDsFromDB(){
        ArrayList<Long> attractionIDs = new ArrayList<Long>();
        SQLiteDatabase db = mDB.getReadableDatabase();


        Cursor c = db.rawQuery("SELECT ID FROM " + DbInitializer.FeedEntry.TABLE_NAME, null);

        try {
            while (c.moveToNext()) {

                long ID = c.getLong(c.getColumnIndex(DbInitializer.FeedEntry.COLUMN_ID));
                attractionIDs.add(ID);
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return attractionIDs;
    }

    public String getCommaSepIDsFromDB(){
        String commaSeperateString = "";

        SQLiteDatabase db = mDB.getReadableDatabase();


        Cursor c = db.rawQuery("SELECT ID FROM " + DbInitializer.FeedEntry.TABLE_NAME, null);

        try {
            while (c.moveToNext()) {
                long ID = c.getLong(0);
                String comma = (c.isLast()) ? "" : ",";
                commaSeperateString += "ID != " + ID + comma;
            }
        }catch (Exception ex) {
            Log.d("DB_LOG",ex.toString());
        }finally{
            c.close();
        }

        db.close();

        return commaSeperateString;
    }

    public void clearMessages(){
        mDB.getWritableDatabase().execSQL("DELETE FROM " + DbInitializer.FeedEntry.MESSAGES_TABLE_NAME);
    }

}

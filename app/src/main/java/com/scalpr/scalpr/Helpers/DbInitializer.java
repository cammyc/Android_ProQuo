package com.scalpr.scalpr.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.scalpr.scalpr.Objects.Attraction;

import org.w3c.dom.Attr;

import java.util.ArrayList;

/**
 * Created by Cam on 9/21/2016.
 */
public class DbInitializer extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 8; // updated 03/29/2017
    public static final String DATABASE_NAME = "Scalpr.db";
    private static final String TEXT_TYPE = "TEXT";
    private static final String INT_TYPE = "INTEGER";
    private static final String DOUBLE_TYPE = "DOUBLE";
    private static final String DECIMAL_LAT_TYPE = "DECIMAL(10,8)";
    private static final String DECIMAL_LON_TYPE = "DECIMAL(11,8)";
    private static final String DATE_TYPE = "DATE";
    private static final String TIMESTAMP_TYPE = "TIMESTAMP";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_ATTRACTION_TABLE =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    FeedEntry.COLUMN_CREATORID + " " + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_VENUENAME + " " + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME + " " + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_TICKETPRICE + " " + DOUBLE_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NUMBEROFTICKETS + " " + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_DESCRIPTION + " " + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_DATE + " " + DATE_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_IMAGEURL + " " + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_LAT + " " + DECIMAL_LAT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_LON + " " + DECIMAL_LON_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_TIMESTAMP + " " + TIMESTAMP_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_POSTTYPE + " " + INT_TYPE + " )";

    private static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + FeedEntry.MESSAGES_TABLE_NAME + " (" +
                    FeedEntry.COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY, " +
                    FeedEntry.COLUMN_MESSAGE_CONVERSATION_ID + " " + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_MESSAGE_SENDER_ID + " " + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_MESSAGE_TEXT + " " + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_MESSAGE_TIMESTAMP + " " + TIMESTAMP_TYPE + " )";

    private static final String SQL_DELETE_ATTRACTION_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    private static final String SQL_DELETE_MESSAGES_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.MESSAGES_TABLE_NAME;

    public DbInitializer(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ATTRACTION_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ATTRACTION_ENTRIES);
        db.execSQL(SQL_DELETE_MESSAGES_ENTRIES);
        onCreate(db);
    }

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "Attractions";
        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_CREATORID = "CreatorID";
        public static final String COLUMN_VENUENAME = "VenueName";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_TICKETPRICE = "TicketPrice";
        public static final String COLUMN_NUMBEROFTICKETS = "NumberOfTickets";
        public static final String COLUMN_DESCRIPTION = "Description";
        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_IMAGEURL = "ImageURL";
        public static final String COLUMN_LAT = "Lat";
        public static final String COLUMN_LON = "Lon";
        public static final String COLUMN_TIMESTAMP = "TimeStamp";
        public static final String COLUMN_POSTTYPE = "PostType";

        public static final String MESSAGES_TABLE_NAME = "Messages";
        public static final String COLUMN_MESSAGE_ID = "ID";
        public static final String COLUMN_MESSAGE_CONVERSATION_ID = "ConversationID";
        public static final String COLUMN_MESSAGE_SENDER_ID = "SenderID";
        public static final String COLUMN_MESSAGE_TEXT = "Message";
        public static final String COLUMN_MESSAGE_TIMESTAMP = "Timestamp";



    }

}
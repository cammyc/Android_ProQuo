package com.scalpr.scalpr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.scalpr.scalpr.Adapters.AttractionListAdapter;
import com.scalpr.scalpr.Adapters.EditAttractionListAdapter;
import com.scalpr.scalpr.Helpers.AttractionHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.DbInitializer;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.HttpResponseListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AttractionList extends AppCompatActivity{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout llLoadAttractionsFailed;
    Button bTryAgain;
    private ProgressBar pbEditAttractions;

    private AttractionHelper atHelper;
    private UserHelper loginHelper;
    private DatabaseHelper dbHelper;

    HttpResponseListener getUserAttractionsResponseListener;
    private int SET_EDIT_LOCATION_ACTIVITY_RESULT = 2;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attractions);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        pbEditAttractions = (ProgressBar) findViewById(R.id.pbEditAttractions);
        pbEditAttractions.setVisibility(View.GONE);
        mRecyclerView.setHasFixedSize(true);
        context = this;

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        atHelper = new AttractionHelper(this);
        loginHelper = new UserHelper(this);
        dbHelper = new DatabaseHelper(this);

        ArrayList<Attraction> attractionsInit = dbHelper.getAttractionsFromDBFreeQuery("SELECT * FROM "+ DbInitializer.FeedEntry.TABLE_NAME + " ORDER BY " + DbInitializer.FeedEntry.COLUMN_DATE + " ASC");
        ArrayList<Attraction> attractionsFinal = new ArrayList<Attraction>();

        if (attractionsInit.size() > 0) {

            String lastDate = "";
            int offset = 0;
            for (int i = 0; i < attractionsInit.size(); i++) {
                Attraction a = attractionsInit.get(i);

                if (!a.getDate().equals(lastDate)) {
                    lastDate = a.getDate();
                    Attraction at = new Attraction();
                    at.setID(-1);
                    at.setDate(a.getDate());
                    attractionsFinal.add(at);
                }

                attractionsFinal.add(a);
            }

            mAdapter = new AttractionListAdapter(context, attractionsFinal);

            mRecyclerView.setAdapter(mAdapter);
        }else{
            //find attractions on map!
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

}

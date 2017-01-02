package com.scalpr.scalpr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.scalpr.scalpr.Adapters.EditAttractionListAdapter;
import com.scalpr.scalpr.Helpers.AttractionHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.HttpResponseListener;

import org.w3c.dom.Attr;

import java.util.ArrayList;

public class EditAttractions extends AppCompatActivity{

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

        llLoadAttractionsFailed = (LinearLayout) findViewById(R.id.llLoadAttractionsFailed);
        bTryAgain = (Button) findViewById(R.id.bTryLoadMyAttractionsAgain);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        pbEditAttractions = (ProgressBar) findViewById(R.id.pbEditAttractions);
        mRecyclerView.setHasFixedSize(true);
        context = this;

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        atHelper = new AttractionHelper(this);
        loginHelper = new UserHelper(this);
        dbHelper = new DatabaseHelper(this);

        getUserAttractionsResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                pbEditAttractions.setVisibility(View.VISIBLE);
                llLoadAttractionsFailed.setVisibility(View.GONE);
            }

            @Override
            public void requestCompleted(String response) {
                pbEditAttractions.setVisibility(View.GONE);
                ArrayList<Attraction> attractionsInit = atHelper.getAttractions(response);
                ArrayList<Attraction> attractionsTemp = new ArrayList<Attraction>();

                if (attractionsInit.size() > 0){

                    String lastDate = "";
                    int offset = 0;
                    for (int i = 0; i < attractionsInit.size(); i++) {
                        Attraction a = attractionsInit.get(i);

                        if(!a.getDate().equals(lastDate)){
                            lastDate = a.getDate();
                            Attraction at = new Attraction();
                            at.setID(-1);
                            at.setDate(a.getDate());
                            attractionsTemp.add( at);
                        }

                        attractionsTemp.add(a);
                    }

                    final ArrayList<Attraction> attractions = attractionsTemp;

                    mAdapter = new EditAttractionListAdapter(context, attractions);
                    mRecyclerView.setAdapter(mAdapter);

                    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                            if (attractions.get(viewHolder.getAdapterPosition()).getID() == -1)
                                return 0;
                            return super.getSwipeDirs(recyclerView, viewHolder);
                        }

                        @Override
                        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    context);

                            // set title
                            alertDialogBuilder.setTitle("Delete Ticket");

                            // set dialog message
                            alertDialogBuilder
                                    .setMessage("Are you sure you want to delete this ticket?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, int id) {
                                            final Attraction selectedAttr = attractions.get(viewHolder.getAdapterPosition());


                                            HttpResponseListener listener = new HttpResponseListener() {
                                                @Override
                                                public void requestStarted() {
                                                    pbEditAttractions.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void requestCompleted(String response) {
                                                    if (response.equals("1")) {
                                                        attractions.remove(viewHolder.getAdapterPosition());
                                                        mRecyclerView.getAdapter().notifyDataSetChanged();
                                                        clearView(mRecyclerView, viewHolder);
                                                        dialog.dismiss();
                                                        dbHelper.deleteAttraction(selectedAttr.getID());
                                                        Toast.makeText(context, "Ticket Deleted", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                                        clearView(mRecyclerView, viewHolder);
                                                        dialog.dismiss();
                                                        Toast.makeText(context, "Unable to delete ticket. Please check your internet connection.", Toast.LENGTH_LONG).show();
                                                    }
                                                    pbEditAttractions.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void requestEndedWithError(VolleyError error) {
                                                    mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                                    clearView(mRecyclerView, viewHolder);
                                                    dialog.dismiss();
                                                    Toast.makeText(context, "Unable to delete ticket. Please check your internet connection.", Toast.LENGTH_LONG).show();
                                                    pbEditAttractions.setVisibility(View.GONE);
                                                }
                                            };

                                            atHelper.DeleteAttractionRequest(listener, loginHelper.getLoggedInUser().getUserID(), selectedAttr.getID());
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                                            clearView(mRecyclerView, viewHolder);
                                            dialog.dismiss();
                                        }
                                    });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        }

                        @Override
                        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                // Get RecyclerView item from the ViewHolder
                                View itemView = viewHolder.itemView;

                                Paint pBG = new Paint();
                                pBG.setColor(Color.parseColor("#e74c3c"));

                                Paint pWhite = new Paint();
                                pWhite.setColor(Color.WHITE);
                                pWhite.setTextSize(60);
                                pWhite.setTypeface(Typeface.DEFAULT_BOLD);
                                if (dX > 0) {
                                    /* Set your color for positive displacement */
                                    Rect r = new Rect(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
                                    // Draw Rect with varying right side, equal to displacement dX
                                    c.drawRect(new RectF(r), pBG);

                                    float yPos = ((itemView.getHeight() / 2) - ((pWhite.descent() + pWhite.ascent()) / 2));

                                    Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                                            R.mipmap.ic_delete_white_2);

                                    Bitmap scaled = Bitmap.createScaledBitmap(icon, (int) (icon.getWidth() * .66), (int) (icon.getHeight() * .66), true);

                                    icon.recycle();

                                    c.drawBitmap(scaled,
                                            (float) itemView.getLeft() + MiscHelper.convertDpToPixel(10, context),
                                            (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - scaled.getHeight()) / 2,
                                            pWhite);

                                    c.drawText("DELETE TICKET",
                                            (float) itemView.getLeft() + MiscHelper.convertDpToPixel(14, context) + scaled.getWidth(),
                                            (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - pWhite.ascent()) / 2,
                                            pWhite);
                                }

                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            }
                        }

                    };

                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                    itemTouchHelper.attachToRecyclerView(mRecyclerView);

                }else{
                    Toast.makeText(context, "You don't have any active tickets...", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbEditAttractions.setVisibility(View.GONE);
                llLoadAttractionsFailed.setVisibility(View.VISIBLE);
            }
        };

        atHelper.getUsersAttractionsRequest(getUserAttractionsResponseListener, loginHelper.getLoggedInUser().getUserID());

        bTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atHelper.getUsersAttractionsRequest(getUserAttractionsResponseListener, loginHelper.getLoggedInUser().getUserID());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == SET_EDIT_LOCATION_ACTIVITY_RESULT){

        }
    }

}

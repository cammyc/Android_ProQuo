package com.scalpr.scalpr.Adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.scalpr.scalpr.EditTicketLocation;
import com.scalpr.scalpr.Helpers.AttractionHelper;
import com.scalpr.scalpr.Helpers.BingImageSearchHelper;
import com.scalpr.scalpr.Helpers.BitmapHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.DecimalDigitsInputFilter;
import com.scalpr.scalpr.Helpers.InputFilterMinMax;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.R;

import java.util.ArrayList;
import java.util.Calendar;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Cam on 9/21/2016.
 */
public class AttractionListAdapter extends RecyclerView.Adapter<AttractionListAdapter.ViewHolder> {
    private int SET_EDIT_LOCATION_ACTIVITY_RESULT_CODE = 2;

    private ArrayList<Attraction> attractions;
    private Context c;
    private BitmapHelper bmHelper;

    HttpResponseListener imageResponseListener;
    AttractionHelper attractionHelper;
    UserHelper loginHelp;
    DatabaseHelper dbHelper;


    private EditText etVenueName, etAttractionName,etAttractionPrice,etNumberOfTickets, etAttractionDatePicker,etAttractionDescription,etAttractionImageSearch;
    private String selectedImageURL;
    private String[] imageURLs;

    private Attraction tempEditedAttraction;
    private AttractionListAdapter adapter;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView ivAttractionPic;
        public TextView tvAttractionName, tvVenueName, tvTicketPriceAndNumTickets, tvEditAttractionDate;
        public LinearLayout llEditAttractionDetails, llEditAttractionLocation;
        public HttpResponseListener updateAttractionResponseListener;
        public LinearLayout ll;
        public ViewHolder(View v) {
            super(v);
            ivAttractionPic = (ImageView) v.findViewById(R.id.ivEditTicketPic);
            tvAttractionName = (TextView) v.findViewById(R.id.tvEditAttractionName);
            tvVenueName = (TextView) v.findViewById(R.id.tvEditVenueName);
            tvTicketPriceAndNumTickets = (TextView) v.findViewById(R.id.tvEdtPriceAndTicketNum);
            tvEditAttractionDate = (TextView) v.findViewById(R.id.tvEditAttractionDate);
            llEditAttractionDetails = (LinearLayout) v.findViewById(R.id.llEditAttractionDetailsButton);
            llEditAttractionLocation = (LinearLayout) v.findViewById(R.id.llEditAttractionLocationButton);
            ll = (LinearLayout) v.findViewById(R.id.llEditAttractionListItem);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AttractionListAdapter(Context _c, ArrayList<Attraction> _attractions) {
        attractions = _attractions;
        c = _c;
        bmHelper = new BitmapHelper(c);
        attractionHelper = new AttractionHelper(c);
        loginHelp  = new UserHelper(c);
        dbHelper = new DatabaseHelper(c);
        adapter = this;

    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if(attractions.get(position).getID() < 0){
            return -1;
        }else{
            return 1;
        }

//        return attractions.get(position).getID();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AttractionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view

        if(viewType == -1){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_attraction_list_date_item, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_attractions_list_item, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Attraction a = attractions.get(position);

        if(a.getID() != -1){
            try{
                Glide.with(c).load(a.getImageURL()).bitmapTransform(new CropCircleTransformation(c)).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.ivAttractionPic);
            }catch (Exception ex){

            }

            holder.ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
//
//                    // set title
//                    alertDialogBuilder.setTitle("Logout");
//
//                    // set dialog message
//                    alertDialogBuilder
//                            .setTitle("Edit Post")
//                            .setMessage("What would you like to do?")
//                            .setCancelable(false)
//                            .setPositiveButton("Update Details",new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog,int id) {
//                                    dialog.cancel();
//                                    showDialog(a, holder.updateAttractionResponseListener, position);
//                                }
//                            })
//                            .setNeutralButton("Update Location", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                    attemptChangeLocation(a.getID(), a.getLat(), a.getLon());
//                                }
//                            })
//                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            });
//
//                    // create alert dialog
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    alertDialog.setCanceledOnTouchOutside(true);
//                    alertDialog.show();
                    final Dialog dialog = new Dialog(c);
                    dialog.setContentView(R.layout.attraction_list_dialog);
                    Button bContactSeller = (Button) dialog.findViewById(R.id.bContactSellerFromAttrListDialog);
                    Button bViewOnMap = (Button) dialog.findViewById(R.id.bViewOnMapFromAttrListDialog);
                    Button bCancel = (Button) dialog.findViewById(R.id.bAttrListCancelDialog);

                   bContactSeller.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            //showDialog(a, holder.updateAttractionResponseListener, position);
                        }
                    });

                   bViewOnMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            //attemptChangeLocation(a.getID(), a.getLat(), a.getLon());
                        }
                    });

                    bCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

            holder.tvAttractionName.setText(a.getName());
            holder.tvVenueName.setText(a.getVenueName());
            holder.tvTicketPriceAndNumTickets.setText("$" + MiscHelper.formatDouble(a.getTicketPrice()) + " · " + a.getNumTickets() + " Tickets");

            holder.updateAttractionResponseListener = new HttpResponseListener() {
                @Override
                public void requestStarted() {
                    Toast.makeText(c,"Updating Post...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void requestCompleted(String response) {

                    holder.tvAttractionName.setText(tempEditedAttraction.getName());
                    holder.tvVenueName.setText(tempEditedAttraction.getVenueName());
                    holder.tvTicketPriceAndNumTickets.setText("$" + MiscHelper.formatDouble(tempEditedAttraction.getTicketPrice()) + " · " + tempEditedAttraction.getNumTickets() + " Tickets");

                    try{
                        Glide.with(c).load(tempEditedAttraction.getImageURL()).bitmapTransform(new CropCircleTransformation(c)).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.ivAttractionPic);
                    }catch (Exception ex){

                    }

                    boolean inDB = dbHelper.updateAttractionDetails(tempEditedAttraction);

                    if(!tempEditedAttraction.getDate().equals(MiscHelper.formatDate(a.getDate()))){
                        ((Activity) c).recreate();
                    }else{//no need to set if recreating activity
                        attractions.get(position).setName(tempEditedAttraction.getName());
                        attractions.get(position).setVenueName(tempEditedAttraction.getVenueName());
                        attractions.get(position).setTicketPrice(tempEditedAttraction.getTicketPrice());
                        attractions.get(position).setNumTickets(tempEditedAttraction.getNumTickets());
                        attractions.get(position).setDescription(tempEditedAttraction.getDescription());
                        attractions.get(position).setDate(tempEditedAttraction.getDate());
                        attractions.get(position).setImageURL(tempEditedAttraction.getImageURL());
                    }

                    Toast.makeText(c,"Successfully updated post!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void requestEndedWithError(VolleyError error) {
                    Toast.makeText(c, "Unable to update post. Please Try Again.", Toast.LENGTH_LONG).show();
                }
            };

        }else{
            //holder.tvEditAttractionDate.setText(MiscHelper.formatDate(a.getDate(), "EEE dd MMMM yyyy"));
            holder.tvEditAttractionDate.setText(MiscHelper.formatDate(a.getDate(), "MM/d/yyyy"));
        }

    }

    private void showDialog(final Attraction a, final HttpResponseListener updateAttractionResponseListener, final int position){
        final Dialog dialog = new Dialog(c);
        dialog.setContentView(R.layout.sell_ticket_dialog);
        dialog.setTitle("Sell Ticket");

        final ImageView ivSelectedMarkerIcon = (ImageView) dialog.findViewById(R.id.ivSelectedImage);
            ivSelectedMarkerIcon.setVisibility(View.VISIBLE);
            Glide.with(c).load(a.getImageURL()).centerCrop().into(ivSelectedMarkerIcon);

        Button dialogButton = (Button) dialog.findViewById(R.id.bDoneSellTicket);
            dialogButton.setText("Save Changes");

        final ProgressBar pbLoadAttractionImages = (ProgressBar) dialog.findViewById(R.id.pbLoadAttractionImages);

        selectedImageURL = a.getImageURL();

        etVenueName = (EditText) dialog.findViewById(R.id.etVenueName);
        etAttractionName = (EditText) dialog.findViewById(R.id.etAttractionName);
        etAttractionPrice = (EditText) dialog.findViewById(R.id.etAttractionPrice);
        etNumberOfTickets = (EditText) dialog.findViewById(R.id.etAttractionNumberOfTickets);
        etAttractionDatePicker = (EditText) dialog.findViewById(R.id.etAttractionDatePicker);
        etAttractionDatePicker.setInputType(InputType.TYPE_NULL);
        etAttractionDescription = (EditText) dialog.findViewById(R.id.etAttractionDescription);
        etAttractionImageSearch = (EditText) dialog.findViewById(R.id.etAttractionImageSearch);

        etVenueName.setText(a.getVenueName());
        etAttractionName.setText(a.getName());
        etAttractionPrice.setText(MiscHelper.formatDouble(a.getTicketPrice()));
        etNumberOfTickets.setText(a.getNumTickets() + "");
        etAttractionDatePicker.setText(MiscHelper.formatDate(a.getDate()));

        //fix below

//        SimpleDateFormat df = new SimpleDateFormat("MMM/dd/yyyy");
//
//        try{
//            String date = df.parse(a.getDate()).toString();
//            etAttractionDatePicker.setText(date);
//        }catch (Exception ex){
//            etAttractionDatePicker.setText(a.getDate());
//        }

        etAttractionDescription.setText(a.getDescription());


        final TextInputLayout tilMarkerImageSearch = (TextInputLayout) dialog.findViewById(R.id.tilMarkerImageSearchLayout);
        etAttractionImageSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        final BingImageSearchHelper imageHelper = new BingImageSearchHelper(c);


        etAttractionPrice.setFilters( new InputFilter[]{new DecimalDigitsInputFilter(2), new InputFilterMinMax(0,1000000)});
        etNumberOfTickets.setFilters(new InputFilter[]{new InputFilterMinMax(1,1000000)});


        final RecyclerView mRecyclerView = (RecyclerView) dialog.findViewById(R.id.my_recycler_view);
        mRecyclerView.setVisibility(View.GONE);
        tilMarkerImageSearch.setVisibility(View.GONE);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               boolean success = attemptEditAttraction(a.getID(), dialog, updateAttractionResponseListener);
            }
        });

        ivSelectedMarkerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivSelectedMarkerIcon.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                tilMarkerImageSearch.setVisibility(View.VISIBLE);
                if(etAttractionImageSearch.getText().toString().trim().equals("")) {
                    etAttractionImageSearch.setText(a.getName());
                    imageHelper.BingImageRequest(imageResponseListener, etAttractionImageSearch.getText().toString());
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(c, mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        selectedImageURL = imageURLs[position];
                        ivSelectedMarkerIcon.setVisibility(View.VISIBLE);
                        Glide.with(c).load(selectedImageURL).centerCrop().into(ivSelectedMarkerIcon);

                        mRecyclerView.setVisibility(View.GONE);
                        tilMarkerImageSearch.setVisibility(View.GONE);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        imageResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                pbLoadAttractionImages.setVisibility(View.VISIBLE);
            }

            @Override
            public void requestCompleted(String response) {

                pbLoadAttractionImages.setVisibility(View.GONE);
                imageURLs = imageHelper.getImageThumbsFromJSON(response);
                // specify an adapter (see also next example)
                RecyclerViewImageAdapter mAdapter = new RecyclerViewImageAdapter(imageURLs,c);
                mRecyclerView.invalidate();
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbLoadAttractionImages.setVisibility(View.GONE);
            }
        };



        etAttractionImageSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    mRecyclerView.setVisibility(View.VISIBLE);
                    imageHelper.BingImageRequest(imageResponseListener,etAttractionImageSearch.getText().toString());
                }
                return false;
            }
        });



        etAttractionDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate=Calendar.getInstance();
                int mYear=mcurrentDate.get(Calendar.YEAR);
                int mMonth=mcurrentDate.get(Calendar.MONTH);
                int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker=new DatePickerDialog(c, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        etAttractionDatePicker.setText((selectedmonth + 1)+"/"+selectedday+"/"+selectedyear);
                        etAttractionDatePicker.setError(null);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select ticket expiration date");
                mDatePicker.show();
            }
        });


        dialog.show();
    }

    private void attemptChangeLocation(long attractionID, double lat, double lon){

        Bundle b = new Bundle();
        b.putLong("attractionID", attractionID);
        b.putDouble("lat", lat);
        b.putDouble("lon", lon);

        Intent intent = new Intent(c, EditTicketLocation.class);
        intent.putExtra("bundle",b);
        ((Activity) c).startActivityForResult(intent, SET_EDIT_LOCATION_ACTIVITY_RESULT_CODE);
    }

    private boolean attemptEditAttraction(long attractionID, Dialog dialog, HttpResponseListener updateAttractionResponseListener){
        etVenueName.setError(null);
        etAttractionName.setError(null);
        etAttractionPrice.setError(null);
        etNumberOfTickets.setError(null);
        etAttractionDatePicker.setError(null);
        etAttractionDescription.setError(null);
        etAttractionImageSearch.setError(null);


        String venueName = etVenueName.getText().toString().trim();
        String attractionName = etAttractionName.getText().toString().trim();
        String price = etAttractionPrice.getText().toString().trim();
        String numberOfTickets = etNumberOfTickets.getText().toString().trim();
        String date = etAttractionDatePicker.getText().toString().trim();
        String description = etAttractionDescription.getText().toString().trim();

        View focusView = null;

        if(TextUtils.isEmpty(venueName)){
            etVenueName.setError(c.getString(R.string.error_field_required));
            focusView = etVenueName;
            focusView.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(attractionName)){
            etAttractionName.setError(c.getString(R.string.error_field_required));
            focusView = etAttractionName;
            focusView.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(price)){
            etAttractionPrice.setError(c.getString(R.string.error_field_required));
            focusView = etAttractionPrice;
            focusView.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(numberOfTickets)){
            etNumberOfTickets.setError(c.getString(R.string.error_field_required));
            focusView = etNumberOfTickets;
            focusView.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(date)){
            etAttractionDatePicker.setError(c.getString(R.string.error_field_required));
//            focusView = etAttractionDatePicker;
//            focusView.requestFocus();
            return false;
        }

        if(TextUtils.isEmpty(selectedImageURL)){
            etAttractionImageSearch.setError("Selected Image Required.");
            focusView = etAttractionImageSearch;
            focusView.requestFocus();
            return false;
        }

        dialog.dismiss();
        tempEditedAttraction = new Attraction();
        tempEditedAttraction.setID(attractionID);
        tempEditedAttraction.setName(attractionName);
        tempEditedAttraction.setVenueName(venueName);
        tempEditedAttraction.setTicketPrice(Double.parseDouble(price));
        tempEditedAttraction.setNumTickets(Integer.parseInt(numberOfTickets));
        tempEditedAttraction.setDescription(description);
        tempEditedAttraction.setDate(date);
        tempEditedAttraction.setImageURL(selectedImageURL);

        attractionHelper.UpdateAttractionDetailsRequest(updateAttractionResponseListener, loginHelp.getLoggedInUser().getUserID(), attractionID, venueName, attractionName, price, numberOfTickets, description, date, selectedImageURL);
        return true;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return attractions.size();
    }
}
package com.scalpr.scalpr.Adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.scalpr.scalpr.EditAttractions;
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
                Glide.with(c).load(a.getImageURL())
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                holder.ivAttractionPic.setImageBitmap(new BitmapHelper(c).getCircleBitmap(((GlideBitmapDrawable) resource).getBitmap() , holder.ivAttractionPic.getWidth(), holder.ivAttractionPic.getHeight(), a.getPostType()));
                            }
                        });

                       // .bitmapTransform(new CropCircleTransformation(c)).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.ivAttractionPic);
            }catch (Exception ex){

            }

            holder.ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Dialog dialog = new Dialog(c);
                    dialog.setContentView(R.layout.attraction_list_dialog);
                    Button bContactSeller = (Button) dialog.findViewById(R.id.bContactSellerFromAttrListDialog);
                    Button bViewOnMap = (Button) dialog.findViewById(R.id.bViewOnMapFromAttrListDialog);
                    Button bCancel = (Button) dialog.findViewById(R.id.bAttrListCancelDialog);

                    final String sellerRequester = (a.getPostType() == 1) ? "Seller" : "Requester";

                    bContactSeller.setText("CONTACT " + sellerRequester.toUpperCase());

                   bContactSeller.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(loginHelp.isUserLoggedIn()){
                                if(a.getCreatorID() == loginHelp.getLoggedInUser().getUserID()){
                                    Toast.makeText(c, "This is your post", Toast.LENGTH_LONG).show();
                                }else{
                                    Bundle b = new Bundle();
                                    b.putSerializable("attraction",a.toSerializable());
                                    b.putBoolean("centerMap", true);
                                    b.putBoolean("contactSeller", true);

                                    Intent data = new Intent();
                                    data.putExtra("bundle",b);

                                    ((Activity) c).setResult(2,data);
                                    ((Activity) c).finish();

                                }
                            }else{
                                Toast.makeText(c, "Please login to contact the " + sellerRequester.toLowerCase(), Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                            //showDialog(a, holder.updateAttractionResponseListener, position);
                        }
                    });

                   bViewOnMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle b = new Bundle();
                            b.putSerializable("attraction",a.toSerializable());
                            b.putBoolean("centerMap", true);

                            Intent data = new Intent();
                            data.putExtra("bundle",b);

                            ((Activity) c).setResult(2,data);
                            ((Activity) c).finish();

                            dialog.dismiss();
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
            String requestingOrSelling = (a.getPostType() == 1) ? "Being Sold" : "Requested";
            holder.tvTicketPriceAndNumTickets.setText("$" + MiscHelper.formatDouble(a.getTicketPrice()) + " · " + a.getNumTickets() + " Tickets " + requestingOrSelling);
            
            int color = MiscHelper.getPostColor(this.c, a.getPostType());
            holder.tvTicketPriceAndNumTickets.setTextColor(color);

        }else{
            //holder.tvEditAttractionDate.setText(MiscHelper.formatDate(a.getDate(), "EEE dd MMMM yyyy"));
            holder.tvEditAttractionDate.setText(MiscHelper.formatDate(a.getDate(), "MM/d/yyyy"));
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return attractions.size();
    }
}
package com.scalpr.scalpr.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Attraction;
import com.scalpr.scalpr.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Cam on 9/23/2016.
 */
public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View vWindowContents;
    private Context c;
    private Attraction a;
    private final Map<Marker, Bitmap> images = new HashMap<>();
    private final Map<Marker, Target<Bitmap>> targets = new HashMap<>();
    private final long userID;

    public MarkerInfoWindowAdapter(Context _c){
        c = _c;
        vWindowContents = ((Activity) _c).getLayoutInflater().inflate(R.layout.marker_info_window, null);
        userID = new UserHelper(_c).getLoggedInUser().getUserID();
    }


    /** initiates loading the info window and makes sure the new image is used in case it changed */
    public void showInfoWindow(Marker marker) {
        Glide.clear(targets.get(marker)); // will do images.remove(marker) too
        marker.showInfoWindow(); // indirectly calls getInfoContents which will return null and start Glide load
    }
    /** use this to discard a marker to make sure all resources are freed and not leaked */
    public void remove(Marker marker) {
        images.remove(marker);
        Glide.clear(targets.remove(marker));
        marker.remove();
    }

    @Override
    public View getInfoContents(Marker marker) {
        //render(marker, vWindowContents);

        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return render(marker, vWindowContents);
    }

    private View render(Marker m, View v){
        try{
            JSONObject obj = new JSONObject(m.getSnippet());
            TextView tvAttractionDate = (TextView) v.findViewById(R.id.tvIwDate);
            TextView tvAttractionName = (TextView) v.findViewById(R.id.tvIwAttractionName);
            TextView tvVenueName = (TextView) v.findViewById(R.id.tvIwVenueName);
            TextView tvNumTickets = (TextView) v.findViewById(R.id.tvIwNumTickets);
            TextView tvTicketPrice = (TextView) v.findViewById(R.id.tvIwPrice);
            TextView tvDescriptionText = (TextView) v.findViewById(R.id.tvIwDescriptionText);
            Button bContactSeller = (Button) v.findViewById(R.id.bContactSeller);

            if(obj.getLong("creatorID") == userID)
                bContactSeller.setText("THIS IS YOUR POST");
            else
                bContactSeller.setText("CONTACT SELLER");


            ImageView ivImage = (ImageView) v.findViewById(R.id.ivIwImage);

            String description = obj.getString("description");

            if(TextUtils.isEmpty(description)){
                tvDescriptionText.setVisibility(View.GONE);
            }else{
                tvDescriptionText.setVisibility(View.VISIBLE);
            }

            tvDescriptionText.setText(description);
            tvAttractionDate.setText(MiscHelper.formatDateToEnglish(obj.getString("date")));
            tvAttractionName.setText(obj.getString("attractionName"));
            tvVenueName.setText(obj.getString("venueName"));
            tvNumTickets.setText(obj.getString("numTickets"));
            tvTicketPrice.setText("$" + obj.getString("ticketPrice")+"/Ticket");

            Bitmap image = images.get(m);
            if (image == null) {
                Glide.with(c).load(obj.getString("imageURL")).asBitmap().transform(new CropCircleTransformation(c)).dontAnimate().into(getTarget(m));
            } else {
                ivImage.setImageBitmap(image);
            }
        }catch (Exception ex){
            Log.d("error", ex.toString());
        }

        return v;
    }



    private Target<Bitmap> getTarget(Marker marker) {
        Target<Bitmap> target = targets.get(marker);
        if (target == null) {
            target = new InfoTarget(marker);
        }
        return target;
    }
    private class InfoTarget extends SimpleTarget<Bitmap> {
        Marker marker;
        InfoTarget(Marker marker) {
            super((int)MiscHelper.convertDpToPixel(80,c), (int) MiscHelper.convertDpToPixel(80,c)); // otherwise Glide will load original sized bitmap which is huge
            this.marker = marker;
        }
        @Override public void onLoadCleared(Drawable placeholder) {
            images.remove(marker); // clean up previous image, it became invalid
            // don't call marker.showInfoWindow() to update because this is most likely called from Glide.into()
        }
        @Override public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            // this prevents recursion, because Glide load only starts if image == null in getInfoContents
            images.put(marker, resource);
            // tell the maps API it can try to call getInfoContents again, this time finding the loaded image
            marker.showInfoWindow();
        }
    }
}
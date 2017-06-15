package com.scalpr.scalpr.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.scalpr.scalpr.Helpers.BitmapHelper;
import com.scalpr.scalpr.Helpers.ConversationHelper;
import com.scalpr.scalpr.Helpers.DatabaseHelper;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Conversation;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.Message;
import com.scalpr.scalpr.R;
import com.scalpr.scalpr.SelectedConversationActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Cam on 10/16/2016.
 */
public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    Context context;
    ArrayList<Conversation> conversations;
    long myUserID;
    Date now;

    public ConversationListAdapter(Context _c, ArrayList<Conversation> _conversations){
        context = _c;
        conversations = _conversations;
        myUserID = new UserHelper(_c).getLoggedInUser().getUserID();
        now = new Date();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvTitle, tvLastMessage, tvTimeStamp;
        public ImageView ivAttractionImage;
        public LinearLayout llConversationListItem;
        public ViewHolder(View v) {
            super(v);

            tvTitle = (TextView) v.findViewById(R.id.tvConvoTitle);
            tvLastMessage = (TextView) v.findViewById(R.id.tvConvoLastMessageText);
            tvTimeStamp = (TextView) v.findViewById(R.id.tvConvoLastMessageTimeStamp);
            llConversationListItem = (LinearLayout) v.findViewById(R.id.llConversationListItem);
            ivAttractionImage = (ImageView) v.findViewById(R.id.ivConvoListItemAttractionImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Conversation c = conversations.get(position);
        String myName;
        String yourName;
        boolean ImSellingToYou;

        if(c.getBuyerID() == myUserID){
            myName = c.getBuyerName();
            yourName = c.getSellerName();
            ImSellingToYou = false;
            holder.tvTitle.setText(c.getSellerName());
        }else {
            myName = c.getSellerName();
            yourName = c.getBuyerName();
            ImSellingToYou = true;
            holder.tvTitle.setText(c.getBuyerName());
        }

        String[] otherUser = yourName.split(" ");

        try{
            Glide.with(context).load(c.getAttractionImageURL()).asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            holder.ivAttractionImage.setImageBitmap(new BitmapHelper(context).getCircleBitmap(resource , holder.ivAttractionImage.getWidth(), holder.ivAttractionImage.getHeight(), c.getPostType()));
                        }
                    });
        }catch (Exception ex){

        }
//        holder.tvTitle.setText(yourName + " - " + c.getTitle());

        if(c.getLastMessage() != null){
            holder.tvLastMessage.setText(c.getLastMessage().getText());
            holder.tvTimeStamp.setText(formatTimeStamp(c.getLastMessage().getTimeStamp()));
        }else{
            holder.tvLastMessage.setText("No messages sent...");
            holder.tvTimeStamp.setText(formatTimeStamp(c.getCreationTimeStamp()));
        }


        if(!c.isLastMessageRead()){
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
            holder.tvTimeStamp.setTypeface(null, Typeface.BOLD);
            holder.tvLastMessage.setTextColor(Color.BLACK);

        }else{
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvTimeStamp.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMessage.setTextColor( holder.tvLastMessage.getTextColors().getDefaultColor());
        }

        holder.llConversationListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                conversations.get(position).setLastMessageRead(true);
                notifyItemChanged(position);

                Intent intent = new Intent(context, SelectedConversationActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("convo", c);
                intent.putExtra("convoBundle", b);
                context.startActivity(intent);
            }
        });
    }

    private String formatTimeStamp(Date lastTime){
        SimpleDateFormat sameDay = new SimpleDateFormat("yyyyMMdd");

        long diff = now.getTime() - lastTime.getTime();
        long days = diff / 86400000;

        if(sameDay.format(lastTime).equals(sameDay.format(now))){
            return MiscHelper.formatDate(lastTime, "h:mm a");
        }else if (days < 7){
            return MiscHelper.formatDate(lastTime, "EEE");
        }else{
            return MiscHelper.formatDate(lastTime, "MMM d");
        }

    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }
}




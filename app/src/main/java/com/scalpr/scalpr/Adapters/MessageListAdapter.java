package com.scalpr.scalpr.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.scalpr.scalpr.Helpers.MiscHelper;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.Conversation;
import com.scalpr.scalpr.Objects.Message;
import com.scalpr.scalpr.R;

import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Cam on 11/7/2016.
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    Context context;
    ArrayList<Message> messages;
    String imageURL;
    long myUserID;
    int MY_MESSAGE = -4;
    int YOUR_MESSAGE = -5;
    int SENDING_MESSAGE = -2;
    int TIME_BREAK = -3;


    public MessageListAdapter(Context _c, ArrayList<Message> _messages, String _imageURL){
        context = _c;
        messages = _messages;
        myUserID = new UserHelper(_c).getLoggedInUser().getUserID();
        imageURL = _imageURL;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView myText, yourText, tvTimeBreak;
        ImageView ivImage;
        ProgressBar pbMessageSending;
        public ViewHolder(View v) {
            super(v);
            myText = (TextView) v.findViewById(R.id.tvMyMessage);
            yourText = (TextView) v.findViewById(R.id.tvYourMessage);
            pbMessageSending = (ProgressBar) v.findViewById(R.id.pbMessageSending);
            tvTimeBreak = (TextView) v.findViewById(R.id.tvMessageTimeBreak);
            ivImage = (ImageView) v.findViewById(R.id.ivAttrImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == MY_MESSAGE){//my message
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message_list_item, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }else if (viewType == TIME_BREAK) {//time break
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_time_break, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }else{//your message
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_message_list_item, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message m = messages.get(position);

        if(m.getID() == TIME_BREAK){
            String conversationCreated = "";
            if(position == messages.size()-1){
                conversationCreated = "Conversation created on ";
            }
            Date date = m.getTimeStamp();
            if(DateUtils.isToday(date.getTime())){
                holder.tvTimeBreak.setText(conversationCreated + MiscHelper.formatDate(date,"h:mm a"));
            }else{
                holder.tvTimeBreak.setText(conversationCreated + MiscHelper.formatDate(date, "EEE MMM d").toUpperCase() + " AT " +MiscHelper.formatDate(date, "h:mm a"));
            }
        }else{
            if(m.getSenderID() == myUserID){//my message
                holder.myText.setText(m.getText());
                holder.myText.setMovementMethod(LinkMovementMethod.getInstance());
                if(m.getID() == SENDING_MESSAGE){
                    holder.pbMessageSending.setVisibility(View.VISIBLE);
                }else{
                    holder.pbMessageSending.setVisibility(View.GONE);
                }
            }else{//your message
                holder.yourText.setText(m.getText());
                holder.yourText.setMovementMethod(LinkMovementMethod.getInstance());
                Glide.with(context).load(imageURL).asBitmap().transform(new CropCircleTransformation(context)).dontAnimate().into(holder.ivImage);

            }
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if(messages.get(position).getSenderID() == myUserID){
            return MY_MESSAGE;
        }else if (messages.get(position).getID() < 0){
            return (int) messages.get(position).getSenderID();
        }else{
            return YOUR_MESSAGE;
        }
    }
}

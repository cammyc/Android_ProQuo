package com.scalpr.scalpr.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.scalpr.scalpr.R;

/**
 * Created by Cam on 9/19/2016.
 */
public class RecyclerViewImageAdapter extends RecyclerView.Adapter<RecyclerViewImageAdapter.ViewHolder> {
    private String[] mDataset;
    private Context c;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
           mImageView = (ImageView) v.findViewById(R.id.ivPotentialMarker);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewImageAdapter(String[] myDataset, Context _c) {
        mDataset = myDataset;
        c = _c;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_horizontal_image, parent, false);
        // set the view's size, margins, paddings and layout parameters



        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Glide.with(c).load(mDataset[position]).centerCrop().into(holder.mImageView);
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}

package com.scalpr.scalpr.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scalpr.scalpr.BuildConfig;
import com.scalpr.scalpr.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InterfaceAddress;
import java.net.URL;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Cam on 9/20/2016.
 */
public class BitmapHelper {

    Context c;
    ArrayList<setMarkerImageAsync> tasks;

    public BitmapHelper(Context _c){
        c = _c;
        tasks = new ArrayList<setMarkerImageAsync>();
    }

    public void cancelAndClearTasks(){
        for(int i = 0; i < tasks.size(); i++){
            tasks.get(i).cancel(true);
        }
    }

    public void formatMarker(String imageURL, final Marker m, final int postType){
        try{

//            DrawableTypeRequest<String> request = Glide.with(c).load(imageURL);
//
//            request.asBitmap().into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                    m.setIcon(BitmapDescriptorFactory.fromBitmap(formatBitmap(price, resource)));
//                }
//            });

            //Glide.cl

            setMarkerImageAsync task = new setMarkerImageAsync(m,imageURL, postType);
            task.execute();
            tasks.add(task);
        }catch (Exception ex){

        }
    }

    public Bitmap formatBitmap(Bitmap bFromURL, int postType){
//        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//        Bitmap bmp = Bitmap.createBitmap(200, 200, conf);
//        Canvas canvas1 = new Canvas(bmp);
//
//// paint defines the text color, stroke width and size
//        Paint color = new Paint();
//        color.setTextSize(40);
//        color.setFakeBoldText(true);
//        color.setTextAlign(Paint.Align.CENTER);
//        color.setColor(Color.WHITE);
//
//        Paint black = new Paint();
//        black.setStyle(Paint.Style.FILL);
//        black.setColor(Color.BLACK);


        BitmapFactory.Options b = new BitmapFactory.Options();
        b.inScaled = true;
        int widthHeight = (int) MiscHelper.convertDpToPixel(55, this.c);
        Bitmap temp = Bitmap.createScaledBitmap(cropToSquare(bFromURL), widthHeight, widthHeight, false);

//        canvas1.drawBitmap(temp, 0, 0, color);
//        canvas1.drawRect(0, 150, 200, 200, black);
//        int xPos = (canvas1.getWidth() / 2);
//
//        canvas1.drawText("$" + MiscHelper.format((long) Double.parseDouble(_price)), xPos, 190, color);

        Bitmap finalBitmap = getCircleCroppedBitmap(temp, postType);

        return finalBitmap;
    }

    public Bitmap getCircleBitmap(Bitmap b, int width, int height, int postType){
        try {
            Bitmap temp = Bitmap.createScaledBitmap(cropToSquare(b), width, height, false);
            return getCircleCroppedBitmap(temp, postType);

        }catch (Exception ex){
            float widthHeight = MiscHelper.convertDpToPixel(60, this.c);

            Bitmap temp = Bitmap.createScaledBitmap(cropToSquare(b), (int) widthHeight, (int) widthHeight, false);
            return getCircleCroppedBitmap(temp, postType);
        }
    }

        public Bitmap getCircleCroppedBitmap(Bitmap bitmap, int postType) {
//            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(output);
//
//            final int color = 0xff424242;
//            final Paint paint = new Paint();
//            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//            paint.setAntiAlias(true);
//            canvas.drawARGB(0, 0, 0, 0);
//            paint.setColor(color);
//
//            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//                    bitmap.getWidth() / 2, paint);
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(bitmap, rect, rect, paint);
//
//            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//                    bitmap.getWidth() / 2, paint);
//
//            final Paint border = new Paint();
//            border.setXfermode(null);
//            border.setStyle(Paint.Style.STROKE);
//            border.setColor(Color.WHITE);
//            border.setStrokeWidth(10);
//            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//                    bitmap.getWidth() / 2, border);

            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            float borderWidth = MiscHelper.convertDpToPixel(4, this.c);

            int radius = Math.min(h / 2, w / 2);
            Bitmap output = Bitmap.createBitmap(w + (int) borderWidth*2, h + (int) borderWidth*2, Bitmap.Config.ARGB_8888);

            Paint p = new Paint();
            p.setAntiAlias(true);

            Canvas c = new Canvas(output);
            c.drawARGB(0, 0, 0, 0);
            p.setStyle(Paint.Style.FILL);

            c.drawCircle((w / 2) + borderWidth, (h / 2) + borderWidth, radius, p);

            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            c.drawBitmap(bitmap, borderWidth, borderWidth, p);
            p.setXfermode(null);
            p.setStyle(Paint.Style.STROKE);
            int color = MiscHelper.getPostColor(this.c, postType);
            p.setColor(color);
            p.setStrokeWidth(borderWidth);

            c.drawCircle((w / 2) + borderWidth, (h / 2) +borderWidth, radius, p);
            return output;
        }

    public Bitmap cropToSquare(Bitmap bitmap){

        if(bitmap != null){
            int width  = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = (height > width) ? width : height;
            int newHeight = (height > width)? height - ( height - width) : height;
            int cropW = (width - height) / 2;
            cropW = (cropW < 0)? 0: cropW;
            int cropH = (height - width) / 2;
            cropH = (cropH < 0)? 0: cropH;
            Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

            return cropImg;
        }else{
            return null;
        }

    }


    class setMarkerImageAsync extends AsyncTask<Void,Void,Bitmap>
    {
        Marker m;
        String URL;
        int postType;

        public setMarkerImageAsync(Marker _m, String _URL, int _postType) {
            m = _m;
            URL = _URL;
            postType = _postType;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            URL url ;
            Bitmap bmp = null;
            try {
                url = new URL(URL);
               bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if(result != null) {
                Bitmap b = formatBitmap(result, postType);
                m.setIcon(BitmapDescriptorFactory.fromBitmap(b));
            }
            m.setVisible(true);

        }
    }
}

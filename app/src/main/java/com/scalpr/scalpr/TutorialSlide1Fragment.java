package com.scalpr.scalpr;

/**
 * Created by anorris on 2017-08-08.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TutorialSlide1Fragment extends Fragment{

    ImageView circleCrop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_slide1, container, false);

        int grey = Color.parseColor("#EEEEEE");
        circleCrop = (ImageView) rootView.findViewById(R.id.circle_crop1);
        circleCrop.setColorFilter(grey);
        circleCrop = (ImageView) rootView.findViewById(R.id.circle_crop2);
        circleCrop.setColorFilter(grey);

        return rootView;
    }

}

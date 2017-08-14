package com.scalpr.scalpr;

/**
 * Created by anorris on 2017-08-08.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import io.apptik.widget.MultiSlider;

public class TutorialSlide2Fragment extends Fragment{

    Spinner tutSpin;
    MultiSlider tutSlide;
    TextView tutMinPrice, tutMaxPrice;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_slide2, container, false);

        tutSpin = (Spinner) rootView.findViewById(R.id.tutorialSpinner);
        ArrayList<String> options = new ArrayList<String>();
        options.add("Any");
        options.add("1");
        options.add("2");
        options.add("3");
        options.add("4+");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_row, options);
        adapter.setDropDownViewResource(R.layout.spinner_row);
        tutSpin.setAdapter(adapter);


        tutMinPrice = (TextView) rootView.findViewById(R.id.tutFilterMinPrice);
        tutMaxPrice = (TextView) rootView.findViewById(R.id.tutFilterMaxPrice);

        tutSlide = (MultiSlider) rootView.findViewById(R.id.rsFilterMinMaxPrice);

        tutSlide.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                if(thumbIndex == 0){
                    tutMinPrice.setText("$" + value);
                }else{
                    tutMaxPrice.setText("$" + value);
                }
            }
        });

        return rootView;
    }

}

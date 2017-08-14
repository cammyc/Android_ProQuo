package com.scalpr.scalpr;

/**
 * Created by anorris on 2017-08-08.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TutorialSlide4Fragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_slide4, container, false);

        Button doneButton = (Button) rootView.findViewById(R.id.doneTutorial);


        doneButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                getActivity().finish();
            }
        });

        return rootView;
    }

}

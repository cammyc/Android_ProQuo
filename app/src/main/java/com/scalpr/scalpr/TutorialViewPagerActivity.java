package com.scalpr.scalpr;

/**
 * Created by anorris on 2017-08-08.
 */

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;


public class TutorialViewPagerActivity extends FragmentActivity {

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_viewpager);

        mPager = (ViewPager) findViewById(R.id.tutorial_viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tutorial_tabs);
        tabLayout.setupWithViewPager(mPager,true);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position){
                case 0:
                    return new TutorialSlide1Fragment();
                case 1:
                    return new TutorialSlide2Fragment();
                case 2:
                    return new TutorialSlide3Fragment();
                case 3:
                    return new TutorialSlide4Fragment();
                default:
                    return new TutorialSlide1Fragment();
            }

        }

        @Override
        public int getCount(){
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed(){

    }
}

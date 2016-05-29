package com.mattleibold.bulktracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Vector;

/**
 * Created by Matt on 5/26/2016.
 */
public class PicturePagerAdapter extends FragmentStatePagerAdapter {

    private Vector<ProgressPicture> mPics;

    public PicturePagerAdapter(FragmentManager fm, Vector<ProgressPicture> pics) {
        super(fm);
        mPics = pics;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ProgressPictureFragment();
        Bundle args = new Bundle();

        ProgressPicture pic = mPics.get(i);
        args.putString(ProgressPictureFragment.ARG_FILEPATH, pic.filepath);
        args.putString(ProgressPictureFragment.ARG_DATE, pic.date);
        args.putDouble(ProgressPictureFragment.ARG_WEIGHT, pic.weight);
        args.putInt(ProgressPictureFragment.ARG_INDEX, i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mPics.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public ProgressPicture getPictureData(int position) {
        return mPics.get(position);
    }
}

package com.mattleibold.bulktracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgressPictureFragment extends Fragment {
    public static final String ARG_INDEX = "index";
    public static final String ARG_FILEPATH = "filepath";
    public static final String ARG_DATE = "date";
    public static final String ARG_WEIGHT = "weight";

    public int mIndex;
    public String mFilepath;
    public String mDate;
    public double mWeight;

    public static ProgressPictureFragment newInstance(int index, String filepath,
                                                      String date, double weight) {
        ProgressPictureFragment fragment = new ProgressPictureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        args.putString(ARG_FILEPATH, filepath);
        args.putString(ARG_DATE, date);
        args.putDouble(ARG_WEIGHT, weight);
        fragment.setArguments(args);
        return fragment;
    }

    public ProgressPictureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_INDEX);
            mFilepath = getArguments().getString(ARG_FILEPATH);
            mDate = getArguments().getString(ARG_DATE);
            mWeight = getArguments().getDouble(ARG_WEIGHT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_progress_picture, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.progressPicture);
        Utilities.loadBitmapWithRotation(imageView, mFilepath);

        TextView weightTextView = (TextView) rootView.findViewById(R.id.weightText);
        weightTextView.setText(mWeight + " lbs");

        TextView dateTextView = (TextView) rootView.findViewById(R.id.dateText);
        dateTextView.setText(mDate);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

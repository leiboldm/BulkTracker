package com.mattleibold.bulktracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by Matt on 5/24/2016.
 */
 public class ThumbnailAdapter extends BaseAdapter {

    // A class to encapsulate all the data needed to draw a thumbnail
    public static class ThumbnailData {
        public String filepath;
        public String date;
        public String weight;
        public ThumbnailData(String fp_in, String d_in, String w_in) {
            filepath = fp_in; date = d_in; weight = w_in;
        }
    }

    private Context mContext;
    private Vector<ThumbnailData> mThumbnails;
    private Vector<Bitmap> bitmaps;

    public ThumbnailAdapter(Context c, Vector<ThumbnailData> data) {
        mContext = c;
        mThumbnails = data;
        bitmaps = new Vector<Bitmap>();
    }

    public int getCount() {
        return mThumbnails.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

        View thumbnailView;

        if (convertView == null) {
            thumbnailView = inflater.inflate(R.layout.photo_gallery_layout, null);
            thumbnailView.setPadding(8, 8, 8, 8);
        } else {
            thumbnailView = (View) convertView;
        }

        // set the thumbnail image
        ThumbnailData data = mThumbnails.get(position);
        ImageView image = (ImageView) thumbnailView.findViewById(R.id.thumbnail_image);
        Bitmap thumbnail = Utilities.loadBitmapWithRotation(image, data.filepath);
        bitmaps.add(thumbnail);
        image.setAdjustViewBounds(true);

        // set the date text
        TextView dateText = (TextView) thumbnailView.findViewById(R.id.date_text);
        dateText.setText(data.date);

        // set the weight text
        TextView weightText = (TextView) thumbnailView.findViewById(R.id.weight_text);
        weightText.setText(data.weight);
        Log.d("BTLOG", "drawing thumbnail " + data.date + " " + data.weight);

        return thumbnailView;
    }

    public void recycleBitmaps() {
        for (Bitmap bm : bitmaps) {
            bm.recycle();
        }
    }
}
package com.mattleibold.bulktracker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;


public class PictureGalleryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_gallery);
        drawGallery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Utilities.handleOptionsItemSelected(this, id);

        return super.onOptionsItemSelected(item);
    }

    private void drawGallery() {
        DBHelper db = new DBHelper(getApplicationContext());
        Vector<ProgressPicture> pics = db.getAllProgressPictures();
        Vector<ThumbnailAdapter.ThumbnailData> thumbnails = new Vector< >();
        Log.d("BTLOG", "Thumbnail count: " + thumbnails.size());
        for (ProgressPicture pic : pics) {
            String date = pic.date;
            String weight = pic.weight + " lbs";
            String filepath = pic.filepath;
            File picFile = new File(filepath);

            if (picFile.exists()) {
                Log.d("BTLOG", "Adding thumbnail " + filepath);
                ThumbnailAdapter.ThumbnailData thumbnail = new ThumbnailAdapter.ThumbnailData(filepath, date, weight);
                thumbnails.add(thumbnail);
            } else {
                // remove progress pictures from the database if they no longer exist in the filesystem
                db.deleteProgressPicture(pic.id);
            }
        }

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ThumbnailAdapter(this, thumbnails));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(PictureGalleryActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
                Intent fullPictureIntent = new Intent(getApplicationContext(), PicturePagerActivity.class);
                fullPictureIntent.putExtra(PicturePagerActivity.ARG_POSITION, position);
                startActivity(fullPictureIntent);
            }
        });
    }
}

package com.mattleibold.bulktracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.util.Vector;


public class PicturePagerActivity extends ActionBarActivity {
    public static final String ARG_POSITION = "position";

    PicturePagerAdapter mPicturePagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_pager);

        Bundle extras = getIntent().getExtras();

        int position = 0;
        if (extras != null) position = extras.getInt(ARG_POSITION);

        DBHelper db = new DBHelper(getApplicationContext());
        Vector<ProgressPicture> pics = db.getAllProgressPictures();
        mPicturePagerAdapter = new PicturePagerAdapter(getSupportFragmentManager(), pics);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPicturePagerAdapter);
        mViewPager.setCurrentItem(position);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        Toast.makeText(PicturePagerActivity.this, "" + position,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_photo) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_delete)
                    .setMessage(getString(R.string.confirm_delete_msg) + "?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int whichButton) {
                            int position = mViewPager.getCurrentItem();
                            ProgressPicture pic = mPicturePagerAdapter.getPictureData(position);
                            Utilities.deleteProgressPicture(getApplicationContext(), pic);
                            Toast.makeText(PicturePagerActivity.this, getString(R.string.photo_deleted),
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), PictureGalleryActivity.class);
                            startActivity(intent);
                            d.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int whichButton) {
                            d.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        } else {
            Utilities.handleOptionsItemSelected(this, id);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.hideStatusBar(this);
    }
}

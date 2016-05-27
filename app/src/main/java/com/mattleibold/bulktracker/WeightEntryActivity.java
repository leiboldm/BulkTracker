package com.mattleibold.bulktracker;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class WeightEntryActivity extends ActionBarActivity {

    private String date;
    private int time;
    private Vector<String> progressPicturePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_entry);

        // Hide picture related UI if the user has no camera
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            View pictureLabel = findViewById(R.id.progressPictureLabel);
            pictureLabel.setVisibility(View.GONE);
            View pictureButton = findViewById(R.id.takePictureButton);
            pictureButton.setVisibility(View.GONE);
        }
        progressPicturePaths = new Vector<String>();

        date = Utilities.getCurrentDateString();
        time = Utilities.getSecondsSinceStartOfDay();
        setDateTimeView();

        String passedWeight = "";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                passedWeight = extras.getString("weightValue");
            }
        }

        EditText weightEntry = (EditText) findViewById(R.id.weightValue);
        weightEntry.setText(passedWeight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weight_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Utilities.handleOptionsItemSelected(this, id);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //EditText weightET = (EditText) findViewById(R.id.weightValue);
        //weightET.requestFocus();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onDestroy() {
        // delete photos that weren't saved
        for (String filename : progressPicturePaths) {
            File file = new File(filename);
            file.delete();
            Log.d("BTLOG", "Deleting " + filename);
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filename))));
        }
        super.onDestroy();
    }

    // called when the user submits a new weight entry
    public void logWeight(View view) {
        DBHelper db = new DBHelper(getApplicationContext());
        EditText et = (EditText) findViewById(R.id.weightValue);
        double pounds = 0.0;
        try {
            pounds = Double.parseDouble(et.getText().toString());
        } catch (Throwable e) {
            // do nothing if the weight entered is empty or otherwise an invalid number
            return;
        }

        EditText commentET = (EditText) findViewById(R.id.commentValue);
        String comment = commentET.getText().toString();

        db.insertWeight(pounds, date, time, comment);
        Log.d("BTLOG", "Weight added: " + String.valueOf(pounds));

        for (String filepath : progressPicturePaths) {
            boolean success = db.insertProgressPicture(pounds, date, time, filepath);
            Log.d("BTLOG", "Inserting progress picture " + filepath + ": " + success);
        }

        // clear the input fields after submitting
        progressPicturePaths.clear();
        commentET.setText("");
        et.setText("");
        LinearLayout pictureContainer = (LinearLayout) findViewById(R.id.pictureContainer);
        pictureContainer.removeAllViews();

        Intent intent = new Intent(this, WeightHistoryActivity.class);
        startActivity(intent);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment df = new TimePickerFragment();
        df.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment df  = new DatePickerFragment();
        df.show(getFragmentManager(), "datePicker");
    }

    // Called by TimePickerFragment.onTimeSet
    public void setTime(int hour, int minute) {
        time = hour * 3600 + minute * 60;
        setDateTimeView();
    }

    // Called by DatePickerFragment.onDateSet
    public void setDate(int year, int month, int day) {
        date = String.format("%04d", year) + "/" + String.format("%02d", month) + "/" +
                String.format("%02d", day);
        setDateTimeView();
    }

    // update the view to display the date and time stored in private members date, time
    public void setDateTimeView() {
        String timeStr = Utilities.secondsToTimeString(time);
        TextView dateTimeView = (TextView) findViewById(R.id.dateTimeValue);
        dateTimeView.setText(date + " " + timeStr);
    }

    // file object containing the most recent picture
    private File photoFile;

    // the request code (id) for starting a camera activity to take a progress picture
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    // Creates a file to store a picture in, then launches an image capture activity
    public void takeProgressPicture(View v) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageRoot = new File(storageDir, getString(R.string.app_name));
        imageRoot.mkdirs();
        photoFile = File.createTempFile(imageFileName, ".jpg", imageRoot);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            LinearLayout pictureContainer = (LinearLayout) findViewById(R.id.pictureContainer);
            ImageView thumbnail = new ImageView(this);
            String photoFilePath = photoFile.getAbsolutePath();
            progressPicturePaths.add(photoFilePath);

            Bitmap image = Utilities.loadBitmapWithRotation(photoFilePath);

            thumbnail.setAdjustViewBounds(true);
            thumbnail.setMaxHeight(400);
            thumbnail.setImageBitmap(image);
            thumbnail.setPadding(10, 0, 10, 0);

            galleryAddPic(photoFilePath);

            pictureContainer.addView(thumbnail);
            Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
            takePictureButton.setText(getString(R.string.take_another_picture));
        }
    }

    // Alert external photo gallery applications of progress pictures
    private void galleryAddPic(String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}

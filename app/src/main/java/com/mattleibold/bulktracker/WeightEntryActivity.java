package com.mattleibold.bulktracker;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class WeightEntryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_entry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weight_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText weightET = (EditText) findViewById(R.id.weightValue);
        weightET.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void logWeight(View view) {
        DBHelper db = new DBHelper(getApplicationContext());
        String default_comment = "";
        EditText et = (EditText) findViewById(R.id.weightValue);
        double pounds = Double.parseDouble(et.getText().toString());

        db.insertWeight(pounds, Utilities.getCurrentDateString(),
                Utilities.getSecondsSinceStartOfDay(), default_comment);
        Log.d("BTLOG", "Weight added: " + String.valueOf(pounds));

        Intent intent = new Intent(this, WeightHistoryActivity.class);
        startActivity(intent);
    }
}

package com.mattleibold.bulktracker;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class WeightHistoryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_history);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weight_history, menu);
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
    protected void onStart() {
        super.onStart();

        LinearLayout layout = (LinearLayout) findViewById(R.id.weightHistoryLayout);
        if (layout.getChildCount() > 0) layout.removeAllViews();
        DBHelper db = new DBHelper(getApplicationContext());
        ArrayList<DBHelper.WeightEntry> weights = db.getAllWeights();
        Log.d("BULKTRACKER", "weights.size() = " + String.valueOf(weights.size()));
        for (DBHelper.WeightEntry we : weights) {
            TextView tv = new TextView(this);
            tv.setText("" + we.weight + " lbs " + we.date + " " + we.makeTimeString());
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(tv);
        }
    }
}

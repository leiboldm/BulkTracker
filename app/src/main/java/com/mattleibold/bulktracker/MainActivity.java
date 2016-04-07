package com.mattleibold.bulktracker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void logWeight(View view) {
        DBHelper db = new DBHelper(getApplicationContext());
        String default_comment = "";
        EditText et = (EditText) findViewById(R.id.weightValue);
        double pounds = Double.parseDouble(et.getText().toString());

        db.insertWeight(pounds, Utilities.getCurrentDateString(),
                Utilities.getSecondsSinceStartOfDay(), default_comment);
        Log.d("BTLOG", "Weight added: " + String.valueOf(pounds));

        Intent intent = new Intent(this, GraphViewActivity.class);
        startActivity(intent);
    }

    public void showWeightEntryActivity(View view) {
        Intent intent = new Intent(this, WeightEntryActivity.class);
        startActivity(intent);
    }

    public void showWeightHistoryActivity(View view) {
        Intent intent = new Intent(this, WeightHistoryActivity.class);
        startActivity(intent);
    }

    public void showGraphViewActivity(View view) {
        Intent intent = new Intent(this, GraphViewActivity.class);
        startActivity(intent);
    }
}

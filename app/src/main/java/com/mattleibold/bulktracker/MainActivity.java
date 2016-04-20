package com.mattleibold.bulktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,
                PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC,
                System.currentTimeMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
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

        Toast toast = Toast.makeText(this, getString(R.string.weight_added), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
        et.setText("");

        Intent intent = new Intent(this, GraphViewActivity.class);
        startActivity(intent);
    }

    public void showWeightEntryActivity(View view) {
        Intent intent = new Intent(this, WeightEntryActivity.class);
        EditText weightET = (EditText) findViewById(R.id.weightValue);
        String weightString = weightET.getText().toString();
        intent.putExtra("weightValue", weightString);
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

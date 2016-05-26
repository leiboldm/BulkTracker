package com.mattleibold.bulktracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;


public class WeightHistoryActivity extends ActionBarActivity implements View.OnClickListener {

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
        refreshTable();
    }

    public void refreshTable() {
        TableLayout layout = (TableLayout) findViewById(R.id.weightHistoryTable);
        if (layout.getChildCount() > 0) layout.removeAllViews();
        DBHelper db = new DBHelper(getApplicationContext());
        ArrayList<DBHelper.WeightEntry> weights = db.getAllWeights();
        Log.d("BTLOG", "weights.size() = " + String.valueOf(weights.size()));

        TableLayout.LayoutParams table_lp = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams table_row_lp = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        table_row_lp.setMargins(5, 0, 5, 0);

        for (DBHelper.WeightEntry we : weights) {
            TextView weightView = new TextView(this);
            weightView.setText("" + we.weight + " " + getString(R.string.lbs));
            weightView.setTag("weight");

            TextView dateView = new TextView(this);
            dateView.setText(we.date);

            TextView timeView = new TextView(this);
            timeView.setText(we.makeTimeString());

            TextView commentView = new TextView(this);
            commentView.setText(we.comment);

            Button deleteButton = new Button(this);
            deleteButton.setText(R.string.delete);
            deleteButton.setId(we.id);
            deleteButton.setTag("deleteButton");
            deleteButton.setOnClickListener(this);

            TableRow tr = new TableRow(this);
            tr.addView(weightView, table_row_lp);
            tr.addView(dateView, table_row_lp);
            tr.addView(timeView, table_row_lp);
            tr.addView(deleteButton, table_row_lp);

            layout.addView(tr, table_lp);
            layout.addView(commentView, table_lp);
            Log.d("BTLOG", "Drawing TableRow " + we.weight + " " + we.date);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() == "deleteButton") {
            AlertDialog confirmDelete = confirmDeleteDialog(view);
            confirmDelete.show();
        }
    }

    public AlertDialog confirmDeleteDialog(final View view) {
        View table_row = (View) view.getParent();
        TextView weightTV = (TextView) table_row.findViewWithTag("weight");
        String weightStr = weightTV.getText().toString();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(getString(R.string.confirm_delete_msg) + " " + weightStr + "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int whichButton) {
                        int id = view.getId();
                        DBHelper db = new DBHelper(getApplicationContext());
                        boolean success = db.deleteWeightEntry(id);
                        if (success) {
                            refreshTable();
                        }
                        d.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int whichButton) {
                        d.dismiss();
                    }
                })
                .create();
        return dialog;
    }
}

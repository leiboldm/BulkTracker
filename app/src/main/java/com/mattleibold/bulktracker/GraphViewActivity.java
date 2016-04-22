package com.mattleibold.bulktracker;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.lang.Math.abs;
import static java.lang.Math.exp;


public class GraphViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph_view, menu);
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

    public static final int maxDataPoints = 10000;

    @Override
    protected void onStart() {
        super.onStart();
        drawChart();
    }

    private void drawChart() {
        GraphView chart = new GraphView(this);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.graphViewMainLayout);
        mainLayout.addView(chart);
        DBHelper db = new DBHelper(getApplicationContext());
        ArrayList<DBHelper.WeightEntry> weights = db.getAllWeights();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (DBHelper.WeightEntry we : weights) {
            DataPoint dp = new DataPoint(we.makeDate(), we.weight);
            series.appendData(dp, true, maxDataPoints);
        }
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(weights.get(0).makeDate());
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(weights.get(weights.size() - 1).makeDate());
        Calendar curDate = Calendar.getInstance();
        curDate.setTime(new Date());
        if (endDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR) &&
                endDate.get(Calendar.YEAR) == curDate.get(Calendar.YEAR)) {
            chart.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("LLL d"))
            );
        } else {
            chart.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("LLL d, yyyy"))
            );
        }

        chart.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

        // set manual x bounds to have nice steps
        chart.getViewport().setMinX(weights.get(0).makeDate().getTime());
        chart.getViewport().setMaxX(weights.get(weights.size() - 1).makeDate().getTime());
        chart.getViewport().setXAxisBoundsManual(true);
        series.setTitle(getString(R.string.raw_data));
        chart.addSeries(series);

        LineGraphSeries<DataPoint> locallyWeightedRegressionSeries = createFilteredSeries(weights);
        locallyWeightedRegressionSeries.setColor(Color.RED);
        locallyWeightedRegressionSeries.setTitle(getString(R.string.smoothed_data));
        chart.addSeries(locallyWeightedRegressionSeries);
        chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        chart.getLegendRenderer().setVisible(true);
    }

    private class TimeDiffWE {
        public long diff;
        public DBHelper.WeightEntry we;
        public TimeDiffWE (long diff_in, DBHelper.WeightEntry we_in) {
            diff = diff_in;
            we = we_in;
        }
    }

    // helper method for createFilteredSeries()
    private void addAveragePoint(DBHelper.WeightEntry minPoint,DBHelper.WeightEntry maxPoint,
                                 ArrayList<DBHelper.WeightEntry> array) {
        double avgWeight = (minPoint.weight + maxPoint.weight) / 2.0;
        int avgTime = (minPoint.time + maxPoint.time) / 2;
        DBHelper.WeightEntry averageWeightEntry = new DBHelper.WeightEntry(avgWeight,
                minPoint.date,  avgTime, "");
        array.add(averageWeightEntry);
    }

    // computes a locally weighted regression for the raw weight data
    private LineGraphSeries<DataPoint> createFilteredSeries(ArrayList<DBHelper.WeightEntry> weights) {
        LineGraphSeries<DataPoint> filteredSeries = new LineGraphSeries<DataPoint>();
        ArrayList<DBHelper.WeightEntry> dailyMinMaxWeights = new ArrayList<DBHelper.WeightEntry>();
        if (weights.size() > 0) {
            String curDate = weights.get(0).date;
            DBHelper.WeightEntry curMin = weights.get(0);
            DBHelper.WeightEntry curMax = weights.get(0);
            for (int i = 0; i < weights.size(); i++) {
                DBHelper.WeightEntry we = weights.get(i);
                if (!we.date.equals(curDate)) {
                    addAveragePoint(curMin, curMax, dailyMinMaxWeights);
                    curDate = we.date;
                    curMin = we;
                    curMax = we;
                } else {
                    if (we.weight > curMax.weight) {
                        curMax = we;
                    } else if (we.weight < curMin.weight) {
                        curMin = we;
                    }
                }
            }
            addAveragePoint(curMin, curMax, dailyMinMaxWeights);
        } else {
            return filteredSeries;
        }

        Date startDate = weights.get(0).makeDate();
        Date endDate = weights.get(weights.size() - 1).makeDate();
        // add one day to endDate to forecast one day ahead of last measurement
        endDate.setTime(endDate.getTime() + 1000 * 24 * 60 * 60);
        // period parameter for locally weighted regression, larger period = smoother graph
        int period = 3; // days
        // milliseconds between data points, default is one day
        long interval = 24 * 60 * 60 * 60 * 1000; // milliseconds between data points, default is one day
        interval = (endDate.getTime() - startDate.getTime()) / 50;
        for (Date iDate = new Date(startDate.getTime());
             iDate.before(endDate);
             iDate.setTime(iDate.getTime() + interval)) {

            double averageWeight = 0;
            double totalNormalizationFactor = 0;
            for (DBHelper.WeightEntry we : dailyMinMaxWeights) {
                long time_diff = abs(we.makeDate().getTime() - iDate.getTime());
                double tau = ((double)time_diff) / (24 * 1000 * 60 * 60 * period);
                double normalizationFactor = exp(0.0 - tau);
                averageWeight += (we.weight * normalizationFactor);
                totalNormalizationFactor += normalizationFactor;
            }
            averageWeight = averageWeight / totalNormalizationFactor;
            filteredSeries.appendData(new DataPoint(iDate, averageWeight), true, maxDataPoints);
        }
        return filteredSeries;
    }
}

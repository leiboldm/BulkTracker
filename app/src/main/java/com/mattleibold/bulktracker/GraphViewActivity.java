package com.mattleibold.bulktracker;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class GraphViewActivity extends ActionBarActivity {

    private GraphView chart;

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

    @Override
    protected void onStart() {
        super.onStart();

        GraphView chart = new GraphView(this);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.graphViewMainLayout);
        mainLayout.addView(chart);
        DBHelper db = new DBHelper(getApplicationContext());
        ArrayList<DBHelper.WeightEntry> weights = db.getAllWeights();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> movingAverageSeries = new LineGraphSeries<DataPoint>();
        Queue<Double> movingAverageQueue = new LinkedList<Double>();
        double total = 0;
        int movingAverageCount = 0;
        int movingAveragePeriod = 7;
        int maxDataPoints = 1000;
        for (DBHelper.WeightEntry we : weights) {
            DataPoint dp = new DataPoint(we.makeDate(), we.weight);
            series.appendData(dp, true, maxDataPoints);
            movingAverageQueue.add(we.weight);
            if (movingAverageQueue.size() > movingAveragePeriod) {
                double oldest = movingAverageQueue.remove();
                total = total - oldest + we.weight;
            } else {
                total += we.weight;
                movingAverageCount += 1;
            }
            double smoothedPoint = total / movingAverageCount;
            DataPoint smoothDP = new DataPoint(we.makeDate(), smoothedPoint);
            movingAverageSeries.appendData(smoothDP, true, maxDataPoints);
        }
        chart.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        chart.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

        // set manual x bounds to have nice steps
        chart.getViewport().setMinX(weights.get(0).makeDate().getTime());
        chart.getViewport().setMaxX(weights.get(weights.size() - 1).makeDate().getTime());
        chart.getViewport().setXAxisBoundsManual(true);
        series.setTitle(getString(R.string.raw_data));
        chart.addSeries(series);
        movingAverageSeries.setColor(Color.RED);
        movingAverageSeries.setTitle(getString(R.string.smoothed_data));
        chart.addSeries(movingAverageSeries);
        chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        chart.getLegendRenderer().setVisible(true);
    }
}

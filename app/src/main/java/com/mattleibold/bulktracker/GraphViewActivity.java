package com.mattleibold.bulktracker;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.abs;
import static java.lang.Math.exp;


public class GraphViewActivity extends ActionBarActivity {

    public final int maxDataPoints = 10000;

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
        int id = item.getItemId();
        Utilities.handleOptionsItemSelected(this, id);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        drawChart();
    }

    private void drawChart() {
        GraphView chart = new GraphView(this);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.graphViewMainLayout);
        mainLayout.removeAllViews();
        mainLayout.addView(chart);
        ChartLoaderTask loaderTask = new ChartLoaderTask(this, chart);
        loaderTask.execute(0);
    }
}

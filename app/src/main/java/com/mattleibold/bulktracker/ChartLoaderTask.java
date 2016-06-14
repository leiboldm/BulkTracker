package com.mattleibold.bulktracker;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.abs;
import static java.lang.Math.exp;

/**
 * Created by Matt on 6/14/2016.
 * Loads one series and adds it to the GraphView provided in the constructor
 */
public class ChartLoaderTask extends AsyncTask<Integer, Void, Void> {
    private WeakReference<GraphView> mGraphViewReference;
    private Context mContext;
    private Integer mType;
    private ArrayList<DBHelper.WeightEntry> mWeights;
    private LineGraphSeries<DataPoint> mRawSeries;
    private LineGraphSeries<DataPoint> mFilteredSeries;

    public static final int maxDataPoints = 10000;

    public ChartLoaderTask(Context context, GraphView graphView) {
        mContext = context;
        mGraphViewReference = new WeakReference<GraphView>(graphView);
    }

    @Override
    protected Void doInBackground(Integer ... params) {
        DBHelper db = new DBHelper(mContext);
        mWeights = db.getAllWeights();
        mRawSeries = getRawSeries();
        mFilteredSeries = createFilteredSeries();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mGraphViewReference != null) {
            GraphView chart = mGraphViewReference.get();

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(mWeights.get(0).makeDate());
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(mWeights.get(mWeights.size() - 1).makeDate());
            Calendar curDate = Calendar.getInstance();
            curDate.setTime(new Date());
            if (endDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR) &&
                    endDate.get(Calendar.YEAR) == curDate.get(Calendar.YEAR)) {
                chart.getGridLabelRenderer().setLabelFormatter(
                        new DateAsXAxisLabelFormatter(mContext, new SimpleDateFormat("LLL d"))
                );
            } else {
                chart.getGridLabelRenderer().setLabelFormatter(
                        new DateAsXAxisLabelFormatter(mContext, new SimpleDateFormat("LLL d, yyyy"))
                );
            }

            chart.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

            // set manual x bounds to have nice steps
            chart.getViewport().setMinX(mWeights.get(0).makeDate().getTime());
            chart.getViewport().setMaxX(mWeights.get(mWeights.size() - 1).makeDate().getTime());
            chart.getViewport().setXAxisBoundsManual(true);
            mRawSeries.setTitle(mContext.getString(R.string.raw_data));
            chart.addSeries(mRawSeries);

            mFilteredSeries.setColor(Color.RED);
            mFilteredSeries.setTitle(mContext.getString(R.string.smoothed_data));
            chart.addSeries(mFilteredSeries);
            chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
            chart.getLegendRenderer().setVisible(true);
        }
    }

    private LineGraphSeries<DataPoint> getRawSeries() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (DBHelper.WeightEntry we : mWeights) {
            DataPoint dp = new DataPoint(we.makeDate(), we.weight);
            series.appendData(dp, true, maxDataPoints);
        }
        return series;
    }

    // computes a locally weighted regression for the raw weight data
    private LineGraphSeries<DataPoint> createFilteredSeries() {
        LineGraphSeries<DataPoint> filteredSeries = new LineGraphSeries<DataPoint>();
        ArrayList<DBHelper.WeightEntry> dailyMinMaxWeights = new ArrayList<DBHelper.WeightEntry>();
        if (mWeights.size() > 0) {
            String curDate = mWeights.get(0).date;
            DBHelper.WeightEntry curMin = mWeights.get(0);
            DBHelper.WeightEntry curMax = mWeights.get(0);
            for (int i = 0; i < mWeights.size(); i++) {
                DBHelper.WeightEntry we = mWeights.get(i);
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

        Date startDate = mWeights.get(0).makeDate();
        Date endDate = mWeights.get(mWeights.size() - 1).makeDate();
        // add one day to endDate to forecast one day ahead of last measurement
        endDate.setTime(endDate.getTime() + 1000 * 24 * 60 * 60);
        // period parameter for locally weighted regression, larger period = smoother graph
        int period = 3; // days
        // milliseconds between data points
        long interval = (endDate.getTime() - startDate.getTime()) / 50;
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

    // helper method for createFilteredSeries()
    private void addAveragePoint(DBHelper.WeightEntry minPoint,DBHelper.WeightEntry maxPoint,
                                 ArrayList<DBHelper.WeightEntry> array) {
        double avgWeight = (minPoint.weight + maxPoint.weight) / 2.0;
        int avgTime = (minPoint.time + maxPoint.time) / 2;
        DBHelper.WeightEntry averageWeightEntry = new DBHelper.WeightEntry(avgWeight,
                minPoint.date,  avgTime, "");
        array.add(averageWeightEntry);
    }
}

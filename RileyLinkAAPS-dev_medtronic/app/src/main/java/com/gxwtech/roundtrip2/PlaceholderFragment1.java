package com.gxwtech.roundtrip2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.gxwtech.roundtrip2.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment1 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private com.gxwtech.roundtrip2.ui.main.PageViewModel pageViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root1 = inflater.inflate(R.layout.fragment_graph1, container, false);
        LineGraphSeries<DataPoint> series1;
        double x,y;
        double[] glucose1={12,23,34,24,56};
//        ArrayList<Double> bolus=new ArrayList<>();
//        SharedPreferences appSharedPrefs = PreferenceManager
//                .getDefaultSharedPreferences(getContext());
//        String boluslist = appSharedPrefs.getString("MyBolusList", "");
//        String bolusList[]=boluslist.split(",");
//        for(int i=0;i<bolusList.length;i++){
//            bolus.add(Double.parseDouble(bolusList[i]));
//        }


        x=0;
        GraphView graph= root1.findViewById(R.id.graph);
        series1=new LineGraphSeries<>();
        int numDataPoints=glucose1.length;
//        int numDataPoints= bolus.size();
        for(int i=0; i<numDataPoints;i++){
            x= x+0.1;
//            y= bolus.get(i);
            y=glucose1[i];
            series1.appendData(new DataPoint(x,y),true,100);
        }
        StaticLabelsFormatter staticLabelsFormatter;
        staticLabelsFormatter = new StaticLabelsFormatter(graph);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.black));
        graph.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.black));
        graph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.red));
//        graph.getGridLabelRenderer().setHorizontalLabelsAngle(145);
        graph.getGridLabelRenderer().setTextSize(50f);
        graph.getGridLabelRenderer().setLabelsSpace(10);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.setTitle("Insulin Over Time(mg/min)");
        graph.setTitleColor(R.color.red);
        graph.setTitleTextSize(80);
        graph.animate();
        graph.setHorizontalScrollBarEnabled(true);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollable(true);
        graph.setVerticalScrollBarEnabled(true);
//        graph.getViewport().setXAxisBoundsManual(true);
//        graph.getViewport().setMinX(1);
        graph.setScaleX(1);
        graph.setMinimumWidth(20);


        graph.addSeries(series1);
        return root1;
    }
}
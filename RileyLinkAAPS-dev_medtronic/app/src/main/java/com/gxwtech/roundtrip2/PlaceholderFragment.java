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
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private com.gxwtech.roundtrip2.ui.main.PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(com.gxwtech.roundtrip2.ui.main.PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_graph, container, false);
        LineGraphSeries<DataPoint> series1;
        double x,y;
        double[] glucose1={1,2,3,4,2};
//        ArrayList<Double> glucose=new ArrayList<>();
//        SharedPreferences appSharedPrefs = PreferenceManager
//                .getDefaultSharedPreferences(getContext());
//        String json = appSharedPrefs.getString("MyObject", "");
//        JSONArray jsonArray=new JSONArray();
//        try {
//            jsonArray = new JSONArray(json);
//        }
//        catch (JSONException e){
//            Log.e("log_tag", "Error parsing data " + e.toString());
//
//        }for(int i=0;i<jsonArray.length();i++){
//            try{
//            glucose.add(Double.parseDouble(jsonArray.getJSONObject(i).get("glucoseLevel").toString()));
//        }catch (JSONException e){
//                Log.e("log_tag", "Error parsing data " + e.toString());
//
//            }
//        }

        x=0;
        GraphView graph= root.findViewById(R.id.graph);
        series1=new LineGraphSeries<>();
        int numDataPoints=glucose1.length;
//        int numDataPoints= glucose.size();
        for(int i=0; i<numDataPoints;i++){
            x= x+1;
//            y= glucose.get(i);
            y=glucose1[i];
            series1.appendData(new DataPoint(x,y),true,100);
        }
        StaticLabelsFormatter staticLabelsFormatter;
        staticLabelsFormatter = new StaticLabelsFormatter(graph);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.red));
        graph.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.red));
        graph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.red));
//        graph.getGridLabelRenderer().setHorizontalLabelsAngle(145);
        graph.getGridLabelRenderer().setTextSize(30f);
        graph.getGridLabelRenderer().setLabelsSpace(10);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.setTitle("Glucose Over Time");
        graph.setTitleColor(R.color.red);
        graph.setTitleTextSize(80);
        graph.animate();
        graph.setHorizontalScrollBarEnabled(true);
        graph.setScaleX(1);
        graph.setVerticalScrollBarEnabled(true);
        graph.setMinimumWidth(20);


        graph.addSeries(series1);
    return root;
    }
}
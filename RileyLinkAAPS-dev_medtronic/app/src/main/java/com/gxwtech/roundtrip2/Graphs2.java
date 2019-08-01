package com.gxwtech.roundtrip2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Graphs2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs2);
        LineGraphSeries<DataPoint> series1;
        double x,y;
        double[] glucose1={12,23,34,24,56};
//                ArrayList<Double> bolus=new ArrayList<>();
//        SharedPreferences appSharedPrefs = PreferenceManager
//                .getDefaultSharedPreferences(getContext());
//        String boluslist = appSharedPrefs.getString("MyBolusList", "");
//        String bolusList[]=boluslist.split(",");
//        for(int i=0;i<bolusList.length;i++){
//            bolus.add(Double.parseDouble(bolusList[i]));
//        }


        x=0;
        GraphView graph=findViewById(R.id.graph2);
        series1=new LineGraphSeries<>();
        int numDataPoints=glucose1.length;
//        int numDataPoints= bolus.size();
        for(int i=0; i<numDataPoints;i++){
            x= x+1;
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
        graph.setScaleX(1);
//        graph.getViewport().setScalable(true);
//        graph.getViewport().setScalableY(true);
//        graph.getViewport().setScrollable(true);
        graph.setVerticalScrollBarEnabled(true);
        graph.setMinimumWidth(20);


        graph.addSeries(series1);

        double x1,y1;
        double[] glucose2={109,120,113,129,102};
        ArrayList<Double> glucose=new ArrayList<>();
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String json = appSharedPrefs.getString("MyObject", "");
        JSONArray jsonArray=new JSONArray();
        try {
            jsonArray = new JSONArray(json);
        }
        catch (JSONException e){
            Log.e("log_tag", "Error parsing data " + e.toString());

        }for(int i=jsonArray.length()-80;i<jsonArray.length();i++){
            try{
            glucose.add(Double.parseDouble(jsonArray.getJSONObject(i).get("glucoseLevel").toString()));
        }catch (JSONException e){
                Log.e("log_tag", "Error parsing data " + e.toString());

            }
        }

        x1=0;
        GraphView graph1= findViewById(R.id.graph1);
        series1=new LineGraphSeries<>();
        int numDataPoints1=glucose.size();
//        int numDataPoints= glucose.size();
        for(int i=0; i<numDataPoints1;i++){
            x1= x1+1;
            y= glucose.get(i);
//            y1=glucose2[i];
            series1.appendData(new DataPoint(x1,y),true,80);
        }
        StaticLabelsFormatter staticLabelsFormatter1;
        staticLabelsFormatter1 = new StaticLabelsFormatter(graph1);
        graph1.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.black));
        graph1.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.black));
        graph1.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.red));
//        graph.getGridLabelRenderer().setHorizontalLabelsAngle(145);
        graph1.getGridLabelRenderer().setTextSize(50f);
        graph1.getGridLabelRenderer().setLabelsSpace(10);
        graph1.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter1);
        graph1.setTitle("Glucose Over Time(mg/min)");
        graph1.setTitleColor(R.color.red);
        graph1.setTitleTextSize(80);
        graph1.animate();
        graph1.setHorizontalScrollBarEnabled(true);
        graph1.setScaleX(1);
//        graph1.getViewport().setScalable(true);
//        graph1.getViewport().setScalableY(true);
//        graph1.getViewport().setScrollable(true);
        graph1.setVerticalScrollBarEnabled(true);
        graph1.setMinimumWidth(20);


        graph1.addSeries(series1);
    }

    public void fragment(View view) {
        Intent i1=new Intent(this,Graph.class);
        i1.putExtra("fragment",1);
        startActivity(i1);
    }
    public void fragment1(View view) {
        Intent i2=new Intent(this,Graph.class);
        i2.putExtra("fragment",2);
        startActivity(i2);
    }
}

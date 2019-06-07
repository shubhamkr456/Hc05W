package com.gxwtech.roundtrip2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gxwtech.roundtrip2.PlaceholderFragment;
import com.gxwtech.roundtrip2.PlaceholderFragment1;
import com.gxwtech.roundtrip2.ui.main.SectionsPagerAdapter;

public class Graph extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        sectionsPagerAdapter.addFragment(new PlaceholderFragment(),"Glucose");
        sectionsPagerAdapter.addFragment(new PlaceholderFragment1(),"Insulin");
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Glucose"));
        tabLayout.addTab(tabLayout.newTab().setText("Insulin"));
        tabLayout.setupWithViewPager(viewPager);


    }

}
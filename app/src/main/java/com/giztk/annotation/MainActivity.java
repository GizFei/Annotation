package com.giztk.annotation;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.giztk.util.StillViewPager;
import com.giztk.util.TabFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FragmentManager mFragmentManager;
    private TabLayout mTabLayout;
    private StillViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.viewpager);

        mFragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new TabFragmentPagerAdapter(mFragmentManager, this));
        mTabLayout.setupWithViewPager(mViewPager);

        initEvents();
        Log.d(TAG, "onCreate");
    }

    /**
     * 初始化各种事件
     */
    private void initEvents(){

    }
}

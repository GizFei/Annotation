package com.giztk.annotation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giztk.util.HttpUtil;
import com.giztk.util.StillViewPager;
import com.giztk.util.TabFragmentPagerAdapter;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FragmentManager mFragmentManager;
    private TabLayout mTabLayout;
    private StillViewPager mViewPager;

    private RequestQueue mRequestQueue;
    private String msg;

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
    private void initEvents() {
        mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                Logout();
                return true;
        }
        return false;
    }

    private void Logout() {
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getLogoutUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            msg = object.getString("msg");
                            Log.d(TAG, msg);
                            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(getApplicationContext(), "登出成功", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "登出失败", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", HttpUtil.getToken());
                Log.d(TAG, params.toString());
                return params;
            }
        };
        mRequestQueue.add(request);
    }
}

package com.giztk.annotation;

import android.animation.Animator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFragmentManager = getSupportFragmentManager();

        mFragmentManager.beginTransaction().
                add(R.id.container, ContentFragment.newInstance("这是一段测试文本.这是第二句。"))
                .commit();

        initEvents();
    }

    /**
     * 初始化各种事件
     */
    private void initEvents(){
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentFragment fragment = ContentFragment.newInstance("这是另一段测试文本.这是第二句。");
                mFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.container, fragment)
                        .commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_font_size) {
            showDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog(){
        final Dialog dialog = new Dialog(MainActivity.this);

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_font_size, null);
        final TextView title = view.findViewById(R.id.title);
        final DiscreteSeekBar slider = view.findViewById(R.id.slider);
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentFragment fragment = (ContentFragment) mFragmentManager.findFragmentById(R.id.container);
                if(fragment != null){
                    fragment.changeFontSize(slider.getProgress());
                }
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        slider.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                title.setTextSize(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.show();
    }
}

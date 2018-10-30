package com.giztk.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.giztk.annotation.ContentFragment;
import com.giztk.annotation.RelationFragment;

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private String[] titles = new String[]{ "实体标注", "关系标注" };

    public TabFragmentPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
        if(i == 0){
            return ContentFragment.newInstance();
        }else if(i == 1){
            return RelationFragment.newInstance();
        }else{
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}

package com.proj.abhi.mytermplanner.pageAdapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Abhi on 2/25/2018.
 */

public class CustomPageAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();
    private final LinkedHashMap<String,Integer> fragmentMap= new LinkedHashMap();
    private int counter =0;

    public CustomPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mFragments.set(position,(Fragment) super.instantiateItem(container, position));
        return mFragments.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentMap.put(title,counter);
        counter++;
        mFragments.add(fragment);
        mFragmentTitles.add(title);
    }

    public Fragment getFragmentByTitle(String title){
        int index = fragmentMap.get(title);
        return mFragments.get(index);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }
}
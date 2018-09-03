package com.example.buhalo.lazyir.view.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.view.fragment.PageFragment;


public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 3;
    private String[] tabTitles;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
      tabTitles = new String[] { context.getString(R.string.tab1), context.getString(R.string.tab2), context.getString(R.string.tab3)};
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


}

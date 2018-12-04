package com.olcow.shiniu.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class ViewPageAdapter extends FragmentPagerAdapter {

    private Context context;
    private List<Fragment> fragmentList;
    private List<String> listTitle;

    public ViewPageAdapter(FragmentManager fm, Context context, List<Fragment> fragmentList, List<String> listTitle) {
        super(fm);
        this.context = context;
        this.fragmentList = fragmentList;
        this.listTitle = listTitle;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return listTitle.size();
    }

    /**
     * //此方法用来显示tab上的名字
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return listTitle.get(position);
    }
}

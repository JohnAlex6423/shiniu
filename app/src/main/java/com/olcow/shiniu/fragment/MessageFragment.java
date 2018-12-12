package com.olcow.shiniu.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.SearchActivity;
import com.olcow.shiniu.adapter.ViewPageAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ConstraintLayout searchCon;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message,container, false);
        tabLayout = view.findViewById(R.id.message_tablayout);
        viewPager = view.findViewById(R.id.message_viewpager);
        searchCon = view.findViewById(R.id.message_search_con);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MessageMessageFragment());
        fragments.add(new MessageFriendsFragment());
        List<String> titles = new ArrayList<>();
        titles.add("消息");
        titles.add("关注");
        viewPager.setAdapter(new ViewPageAdapter(getFragmentManager(),getActivity(),fragments,titles));
        tabLayout.setupWithViewPager(viewPager);
        searchCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SearchActivity.class));
            }
        });
        return view;
    }

}

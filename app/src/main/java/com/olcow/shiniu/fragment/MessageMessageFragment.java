package com.olcow.shiniu.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olcow.shiniu.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageMessageFragment extends Fragment {


    public MessageMessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_message, container, false);
    }

}

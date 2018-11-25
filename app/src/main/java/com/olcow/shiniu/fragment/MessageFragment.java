package com.olcow.shiniu.fragment;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {


    private Button button;


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
        button = view.findViewById(R.id.test_okhttp);
        // Inflate the layout for this fragment
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteOpenHelper sqLiteOpenHelper = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1);
                SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getReadableDatabase();
                Cursor c = sqLiteDatabase.rawQuery("select session from account",null);
                if (c.moveToFirst()){
                    String session = c.getString(c.getColumnIndex("session"));
                    Log.e("shiniu.sqlite", "session="+session);
                }
            }
        });
        return view;
    }
}

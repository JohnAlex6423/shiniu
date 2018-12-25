package com.olcow.shiniu.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.EditUserinfoActivity;
import com.olcow.shiniu.activity.LoginActivity;
import com.olcow.shiniu.activity.SettingActivity;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {

    private boolean coolButton = true;

    ImageView myAvatarImage;

    TextView myNameText;
    TextView myIntroduction;

    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewMy = inflater.inflate(R.layout.fragment_my,container,false);
        myAvatarImage = viewMy.findViewById(R.id.my_avatar);
        myNameText = viewMy.findViewById(R.id.my_name);
        myIntroduction = viewMy.findViewById(R.id.my_introduction);
        SQLiteOpenHelper accountHelper = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1);
        final SQLiteDatabase sqLiteDatabase = accountHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select session,uid,permission from account",null);
        if (cursor.moveToFirst()){
            UserInfo userInfo = new UserInfo();
            Cursor userC = sqLiteDatabase.rawQuery("select *from userinfo",null);
            if (userC.moveToFirst()){
                userInfo.setAvatar(userC.getString(userC.getColumnIndex("avatar")));
                userInfo.setName(userC.getString(userC.getColumnIndex("name")));
                userInfo.setUid(userC.getInt(userC.getColumnIndex("uid")));
                userInfo.setIntroduction(userC.getString(userC.getColumnIndex("introduction")));
                LinearLayout myInfo = viewMy.findViewById(R.id.my_info);
                ImageView settings = viewMy.findViewById(R.id.my_setting);
                settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(),SettingActivity.class));
                    }
                });
                RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);
                Glide.with(this)
                        .load(userInfo.getAvatar())
                        .apply(requestOptions)
                        .into(myAvatarImage);
                myInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(),EditUserinfoActivity.class));
                    }
                });
                myNameText.setText(userInfo.getName());
                myIntroduction.setText(userInfo.getIntroduction());
            } else {
                viewMy = inflater.inflate(R.layout.fragment_my,container,false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取用户缓存失败,请关闭重试", Toast.LENGTH_SHORT).show();
                    }
                });
                myNameText.setText("缓存失败,请退出重试");
            }
            return viewMy;
        } else {
            View view = inflater.inflate(R.layout.fragment_my_nologin,container,false);
            Button loginButton = view.findViewById(R.id.my_nologin_login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(),LoginActivity.class));
                }
            });
            return view;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("changemy");
        localBroadcastManager.registerReceiver(myBroadcastReceiver,intentFilter);
    }
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SQLiteOpenHelper helper = new AccountDatabaseHelper(context,"olcowsso",null,1);
            SQLiteDatabase sqLiteDatabase = helper.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select avatar,name,introduction from userinfo",null);
            if (cursor.moveToFirst()){
                Glide.with(context)
                        .load(cursor.getString(cursor.getColumnIndex("avatar")))
                        .into(myAvatarImage);
                myNameText.setText(cursor.getString(cursor.getColumnIndex("name")));
                myIntroduction.setText(cursor.getString(cursor.getColumnIndex("introduction")));
            }
        }
    }
}

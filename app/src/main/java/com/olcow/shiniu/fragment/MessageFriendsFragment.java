package com.olcow.shiniu.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.FriendListAdapt;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFriendsFragment extends Fragment {

    private SwipeRefreshLayout friendPullRef;
    private RecyclerView friendsList;
    private TextView friendNobody;
    private RecyclerView.Adapter adapter;
    private List<UserInfo> userInfoList;
    private OkHttpClient okHttpClient;
    private SQLiteDatabase sqLiteDatabase;
    private String session;
    private MyBroadcastReceiver myBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    public MessageFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_friends,container,false);
        friendPullRef = view.findViewById(R.id.message_friend_pullref);
        friendPullRef.setColorSchemeColors(-745928);
        friendsList = view.findViewById(R.id.message_friend_recylist);
        friendNobody = view.findViewById(R.id.message_friend_nobody);
        userInfoList = new ArrayList<>();
        adapter = new FriendListAdapt(userInfoList);
        friendsList.setAdapter(adapter);
        friendsList.setItemAnimator(new DefaultItemAnimator());
        friendsList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        sqLiteDatabase =
                new AccountDatabaseHelper(getActivity(),"olcowsso",null,1)
                        .getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select session from account",null);
        if (cursor.moveToFirst()){
            session = cursor.getString(cursor.getColumnIndex("session"));
        } else {
            session = "sdf";
            Toast.makeText(getActivity(), "你还未登陆", Toast.LENGTH_SHORT).show();
        }
        friendPullRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFriend();
            }
        });
        getFriend();
        myBroadcastReceiver = new MyBroadcastReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("delfriend");
        localBroadcastManager.registerReceiver(myBroadcastReceiver,intentFilter);
        return view;
    }

    private void getFriend(){
        if (okHttpClient==null){
            okHttpClient = new OkHttpClient();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                friendPullRef.setRefreshing(true);
            }
        });
        okHttpClient.newCall(new Request.Builder()
                .url("http://39.96.40.12:1008/getfriends")
                .post(new FormBody.Builder()
                        .add("session",session)
                        .build())
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "网络不不通畅,请退出重试", Toast.LENGTH_SHORT).show();
                        friendPullRef.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                JSONObject jsonObject = JSON.parseObject(res);
                final String string = jsonObject.getString("info");
                if (string != null){
                    switch (string) {
                        case "no login":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    friendPullRef.setRefreshing(false);
                                    Toast.makeText(getActivity(), "你的登陆已过期!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "no activity":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    friendPullRef.setRefreshing(false);
                                    Toast.makeText(getActivity(), "你还没激活你的账号", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "no friend":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    friendPullRef.setRefreshing(false);
                                    userInfoList.clear();
                                    friendNobody.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                    }
                } else {
                    userInfoList.clear();
                    userInfoList.addAll(JSON.parseArray(jsonObject.getString("friend"),UserInfo.class));
                    if (!userInfoList.isEmpty()){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendNobody.setVisibility(View.GONE);
                                friendPullRef.setRefreshing(false);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }

    private void delfriend(int bUid){
        if (okHttpClient==null){
            okHttpClient = new OkHttpClient();
        }
        if (bUid!=0){
            okHttpClient.newCall(new Request.Builder()
                    .url("http://39.96.40.12:1008/delfriend")
                    .post(new FormBody.Builder()
                            .add("session",session)
                            .add("buid", String.valueOf(bUid))
                            .build())
                    .build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "网络加载失败,请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res  = response.body().string();
                    switch (res) {
                        case "no login":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "当前登陆失效", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "fail":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "删除失败,请重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "successful":
                            getFriend();
                            break;
                        default:
                            Toast.makeText(getActivity(), "服务器错误,请稍后重试", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "系统错误,请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            delfriend(intent.getIntExtra("userinfoid",0));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(myBroadcastReceiver);
    }
}
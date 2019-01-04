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

import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.NowMessageListAdapter;
import com.olcow.shiniu.entity.NowMessage;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;
import com.olcow.shiniu.until.TimeType;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageMessageFragment extends Fragment {

    private RecyclerView nowMessageReList;
    private NowMessageListAdapter nowMessageAdapter;
    private TextView noneMessageText;
    private TextView errorMessageText;
    private List<NowMessage> nowMessages;
    private SQLiteDatabase accSqLiteDatabase;
    private SQLiteDatabase chatSqliteDatabase;
    private List<UserInfo> recipientUserInfoList;
    private MyBroadcastReceiver myBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private UserInfo sendUserInfo;
    private SwipeRefreshLayout swipeRefreshLayout;
    public MessageMessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_message, container, false);
        errorMessageText = view.findViewById(R.id.message_message_error);
        noneMessageText = view.findViewById(R.id.message_message_none);
        nowMessageReList = view.findViewById(R.id.message_message_rec);
        swipeRefreshLayout = view.findViewById(R.id.message_message_swipe);
        nowMessageReList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        nowMessageReList.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMessage();
        if (myBroadcastReceiver == null){
            myBroadcastReceiver = new MyBroadcastReceiver();
        }
        if (localBroadcastManager==null){
            localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("mainmessagebadge");
        localBroadcastManager.registerReceiver(myBroadcastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myBroadcastReceiver!=null){
            localBroadcastManager.unregisterReceiver(myBroadcastReceiver);
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateMessage();
        }
    }

    private void updateMessage(){
        noneMessageText.setVisibility(View.GONE);
        errorMessageText.setVisibility(View.GONE);
        nowMessageReList.setVisibility(View.GONE);
        if (chatSqliteDatabase == null){
            chatSqliteDatabase = new ChatDatabaseHelper(getActivity(),"chatmessage",null,1).getReadableDatabase();
        }
        if (sendUserInfo == null){
            if (accSqLiteDatabase==null){
                accSqLiteDatabase = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1).getWritableDatabase();
            }
            Cursor c = accSqLiteDatabase.rawQuery("select * from userinfo",null);
            if (c.moveToFirst()){
                sendUserInfo = new UserInfo(c.getInt(c.getColumnIndex("uid")),
                        c.getString(c.getColumnIndex("name")),
                        c.getString(c.getColumnIndex("avatar")),
                        c.getString(c.getColumnIndex("introduction")));
            } else {
                errorMessageText.setVisibility(View.VISIBLE);
                return;
            }
        }
        Cursor cursor = chatSqliteDatabase.rawQuery("select *from nowmessage where myuid = "+sendUserInfo.getUid()+" order by date desc",null);
        if (cursor.moveToFirst()){
            nowMessages = new ArrayList<>();
            recipientUserInfoList = new ArrayList<>();
            UserInfo recipientUserInfo = new UserInfo(cursor.getInt(cursor.getColumnIndex("uid")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("avatar")),
                    cursor.getString(cursor.getColumnIndex("introduction")));
            recipientUserInfoList.add(recipientUserInfo);
            nowMessages.add(new NowMessage(recipientUserInfo.getName(),
                    cursor.getString(cursor.getColumnIndex("content")),
                    TimeType.getLastSendMessageTimeText(cursor.getLong(cursor.getColumnIndex("date"))),
                    recipientUserInfo.getAvatar(),cursor.getInt(cursor.getColumnIndex("count"))));
            while (cursor.moveToNext()){
                UserInfo recipientUserInfoWhile = new UserInfo(cursor.getInt(cursor.getColumnIndex("uid")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("avatar")),
                        cursor.getString(cursor.getColumnIndex("introduction")));
                recipientUserInfoList.add(recipientUserInfoWhile);
                nowMessages.add(new NowMessage(recipientUserInfoWhile.getName(),
                        cursor.getString(cursor.getColumnIndex("content")),
                        TimeType.getLastSendMessageTimeText(cursor.getLong(cursor.getColumnIndex("date"))),
                        recipientUserInfoWhile.getAvatar(),cursor.getInt(cursor.getColumnIndex("count"))));
            }
            nowMessageAdapter = new NowMessageListAdapter(nowMessages,recipientUserInfoList);
            nowMessageAdapter.addAreYouSureClickListener(new NowMessageListAdapter.OnItemAreYouSureClickListener() {
                @Override
                public void onSureClick(int userId) {
                    Toast.makeText(getActivity(), "你点击了确认", Toast.LENGTH_SHORT).show();
                    if (chatSqliteDatabase == null){
                        chatSqliteDatabase = new ChatDatabaseHelper(getActivity(),"chatmessge",null,1).getWritableDatabase();
                    }
                    chatSqliteDatabase.execSQL("delete from message where uid = " +userId+" and myuid = "+sendUserInfo.getUid());
                    chatSqliteDatabase.execSQL("delete from nowmessage where uid = "+userId +" and myuid = "+sendUserInfo.getUid());
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                }

                @Override
                public void onDelNowMessage(int userId) {
                    if (chatSqliteDatabase == null){
                        chatSqliteDatabase = new ChatDatabaseHelper(getActivity(),"chatmessge",null,1).getWritableDatabase();
                    }
                    chatSqliteDatabase.execSQL("delete from nowmessage where uid = "+userId +" and myuid = "+sendUserInfo.getUid());
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                }
            });
            nowMessageReList.setAdapter(nowMessageAdapter);
            nowMessageReList.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setColorSchemeColors(-745928);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(getActivity(), "都是自动更新的不需要刷新哦~", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }else {
            noneMessageText.setVisibility(View.VISIBLE);
        }
    }
}

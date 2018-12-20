package com.olcow.shiniu.fragment;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private RecyclerView.Adapter nowMessageAdapter;
    private TextView noneMessageText;
    private TextView errorMessageText;
    private List<NowMessage> nowMessages;
    private SQLiteDatabase accSqLiteDatabase;
    private SQLiteDatabase chatSqliteDatabase;
    private UserInfo sendUserinfo;
    private UserInfo recipientUserinfo;

    public MessageMessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_message, container, false);
        if (chatSqliteDatabase == null){
            chatSqliteDatabase = new ChatDatabaseHelper(getActivity(),"chatmessage",null,1).getReadableDatabase();
        }
        Cursor cursor = chatSqliteDatabase.rawQuery("select *from nowmessage order by date desc",null);
        if (cursor.moveToFirst()){
            nowMessages = new ArrayList<>();
            recipientUserinfo = new UserInfo(cursor.getInt(cursor.getColumnIndex("uid")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("avatar")),
                    cursor.getString(cursor.getColumnIndex("introduction")));
            nowMessages.add(new NowMessage(recipientUserinfo.getName(),
                    cursor.getString(cursor.getColumnIndex("content")),
                    TimeType.getLastSendMessageTimeText(cursor.getLong(cursor.getColumnIndex("date"))),
                    recipientUserinfo.getAvatar(),cursor.getInt(cursor.getColumnIndex("count"))));
            while (cursor.moveToNext()){
                nowMessages.add(new NowMessage(recipientUserinfo.getName(),
                        cursor.getString(cursor.getColumnIndex("content")),
                        TimeType.getLastSendMessageTimeText(cursor.getLong(cursor.getColumnIndex("date"))),
                        recipientUserinfo.getAvatar(),cursor.getInt(cursor.getColumnIndex("count"))));
            }
            if (accSqLiteDatabase == null){
                accSqLiteDatabase = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1).getWritableDatabase();
            }
            Cursor c = accSqLiteDatabase.rawQuery("select *from userinfo",null);
            if (c.moveToFirst()){
                sendUserinfo = new UserInfo(c.getInt(c.getColumnIndex("uid")),
                        c.getString(c.getColumnIndex("name")),
                        c.getString(c.getColumnIndex("avatar")),
                        c.getString(c.getColumnIndex("introduction")));
                nowMessageAdapter = new NowMessageListAdapter(nowMessages,sendUserinfo,recipientUserinfo);
                nowMessageReList = view.findViewById(R.id.message_message_rec);
                nowMessageReList.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
                nowMessageReList.setItemAnimator(new DefaultItemAnimator());
                nowMessageReList.setAdapter(nowMessageAdapter);
            }else {
                errorMessageText = view.findViewById(R.id.message_message_error);
                errorMessageText.setVisibility(View.VISIBLE);
            }
        }else {
            noneMessageText = view.findViewById(R.id.message_message_none);
            noneMessageText.setVisibility(View.VISIBLE);
        }
        return view;
    }

}

package com.olcow.shiniu.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.Message;
import com.olcow.shiniu.entity.MessagePro;
import com.olcow.shiniu.service.GetMessageService;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;
import com.olcow.shiniu.until.TimeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private SQLiteDatabase sqLiteDatabase;
    private SQLiteDatabase sqlMessage;
    private String session;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (sqLiteDatabase==null){
            sqLiteDatabase = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1).getReadableDatabase();
        }
        Cursor c = sqLiteDatabase.rawQuery("select session from account",null);
        if (c.moveToFirst()){
            session = c.getString(c.getColumnIndex("session"));
        }else {
            Toast.makeText(getActivity(), "当前登陆失效,请重新登陆", Toast.LENGTH_SHORT).show();
        }
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        Button button = view.findViewById(R.id.startactivitybutton);
        Button button1 = view.findViewById(R.id.home_test);
        Button button2 = view.findViewById(R.id.home_service_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newCall(new Request.Builder()
                        .url("http://39.96.40.12:1008/getmessage")
                        .post(new FormBody.Builder()
                                .add("session","bd5ce5a7a0bad521d1f40e7e59d31c42")
                                .build())
                        .build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("shiniu", "onFailure: ");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        JSONObject jsonObject = JSON.parseObject(res);
                        Set<String> keys = jsonObject.keySet();
                        if (keys.isEmpty()){
                            Log.e("shiniu", "空");
                        }else {
                            if (sqlMessage==null){
                                sqlMessage = new ChatDatabaseHelper(getActivity(),"chatmessage",null,1).getReadableDatabase();
                            }
                            for (String key:keys){
                                Log.e("shiniu", "onResponse: "+key);
                                for (MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                    Log.e("zhixing", messagePro.getContent());
                                    sqlMessage.execSQL("insert into message(uid,date,recipientorsend,content) values("+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
                                }
                            }
                        }
                    }
                });
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sqlMessage==null){
                    sqlMessage = new ChatDatabaseHelper(getActivity(),"chatmessage",null,1).getReadableDatabase();
                }
                Cursor c = sqlMessage.rawQuery("select *from message where uid=1023 order by date desc limit 0,20",null);
                List<Message> messages = new ArrayList<>();
                if (c.moveToLast()){
                    long cacheTime;
                    cacheTime = c.getLong(c.getColumnIndex("date"));
                    messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                    while (c.moveToPrevious()){
                        long cacheTimeNext = c.getLong(c.getColumnIndex("date"));
                        if (cacheTime - cacheTimeNext>600000){
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                        } else {
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.NOSHOWTIME));
                        }
                    }
                }
                for (Message message:messages){
                    Log.e("shiniu", "content:"+message.getContent()+"time:"+message.getTime()+"showtime:"+message.getShowTime()+"rors:"+message.getSendOrRecipient());
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), GetMessageService.class));
            }
        });
        return view;
    }
}
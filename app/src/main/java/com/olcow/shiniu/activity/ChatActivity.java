package com.olcow.shiniu.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.ChatAdapter;
import com.olcow.shiniu.entity.Message;
import com.olcow.shiniu.entity.MessagePro;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.service.GetMessageService;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;
import com.olcow.shiniu.until.NotificationUntil;
import com.olcow.shiniu.until.Softkeyboardlistener;
import com.olcow.shiniu.until.TimeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private EditText sendEdit;
    private Button sendButton;
    private Button sendButtonNone;
    private ConstraintLayout sendCon;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Message> messages;
    private UserInfo recipientUserInfo;
    private UserInfo sendUserInfo;
    private TextView errorText;
    private SQLiteDatabase chatSql;
    private SQLiteDatabase accSql;
    private String session;
    private Timer timer;
    private OkHttpClient okHttpClient;
    private NotificationUntil notificationUntil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recipientUserInfo = (UserInfo) getIntent().getSerializableExtra("recipientuserinfo");
        sendUserInfo = (UserInfo) getIntent().getSerializableExtra("senduserinfo");
        sendEdit = findViewById(R.id.chat_send_edit);
        sendButton = findViewById(R.id.chat_send_button);
        sendButtonNone = findViewById(R.id.chat_send_button_none);
        recyclerView = findViewById(R.id.chat_rec);
        sendCon = findViewById(R.id.chat_send_con);
        errorText = findViewById(R.id.chat_error);
        if (recipientUserInfo == null||sendUserInfo == null){
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        setTitle(recipientUserInfo.getName());
        messages = new ArrayList<>();
        if (accSql==null){
            accSql = new AccountDatabaseHelper(this,"olcowsso",null,1).getReadableDatabase();
        }
        Cursor cursor = accSql.rawQuery("select session from account",null);
        if (cursor.moveToFirst()){
            session = cursor.getString(cursor.getColumnIndex("session"));
        }else {
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        if (chatSql==null){
            chatSql = new ChatDatabaseHelper(ChatActivity.this,"chatmessage",null,1).getReadableDatabase();
        }
        Cursor c = chatSql.rawQuery("select *from message where uid="+recipientUserInfo.getUid()+" and myuid = "+sendUserInfo.getUid()+" order by date desc limit 0,20",null);
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
        chatSql.execSQL("update nowmessage set count = 0 where uid = "+recipientUserInfo.getUid()+" and myuid = " + sendUserInfo.getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter = new ChatAdapter(messages,sendUserInfo,recipientUserInfo);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messages.size()-1);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(sendEdit.getText().toString(),"12-17 周一 13:14",1,1));
                sendEdit.setText("");
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        });
        sendEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()<1){
                    sendButton.setVisibility(View.INVISIBLE);
                    sendButtonNone.setVisibility(View.VISIBLE);
                }else {
                    sendButtonNone.setVisibility(View.GONE);
                    sendButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Softkeyboardlistener.setListener(this, new Softkeyboardlistener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerView.scrollToPosition(messages.size()-1);
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:////主键id 必须这样写
                onBackPressed();//按返回图标直接回退上个界面
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_f49e38));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (session==null){
            if (accSql==null){
                accSql = new AccountDatabaseHelper(this,"olcowsso",null,1).getReadableDatabase();
            }
            Cursor cursor = accSql.rawQuery("select session from account",null);
            if (cursor.moveToFirst()){
                session = cursor.getString(cursor.getColumnIndex("session"));
            }else {
                return;
            }
        }
        startService(new Intent(this,GetMessageService.class).putExtra("senduserinfo",sendUserInfo).putExtra("session",session));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer==null){
            timer = new Timer();
        }
        if (okHttpClient==null){
            okHttpClient = new OkHttpClient();
        }
        if (notificationUntil==null){
            notificationUntil = new NotificationUntil(ChatActivity.this);
        }
        if (session==null){
            if (accSql==null){
                accSql = new AccountDatabaseHelper(this,"olcowsso",null,1).getReadableDatabase();
            }
            Cursor cursor = accSql.rawQuery("select session from account",null);
            if (cursor.moveToFirst()){
                session = cursor.getString(cursor.getColumnIndex("session"));
            }else {
                sendCon.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
        }
        stopService(new Intent(this,GetMessageService.class));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sendUserInfo ==null||session == null){
                    Log.e("shiniu", "空");
                }else {
                    okHttpClient.newCall(new Request.Builder()
                            .url("http://39.96.40.12:1008/getmessage")
                            .post(new FormBody.Builder()
                                    .add("session",session)
                                    .build())
                            .build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu", "onFailure: ");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            final JSONObject jsonObject;
                            Set<String> keys;
                            try {
                                jsonObject = JSON.parseObject(res);
                                keys = jsonObject.keySet();
                            }catch (Exception e){
                                Log.e("shiniu", "getmessageerror:"+e.getMessage());
                                timer.cancel();
                                return;
                            }
                            if (keys.isEmpty()){
                                Log.e("shiniu", "空");
                            }else {
                                for (final String key:keys){
                                    Log.e("shiniu", "onResponse: "+key);
                                    if (key.equals("error")){
                                        timer.cancel();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ChatActivity.this, "当前登陆异常,请退出重试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }
                                    if (recipientUserInfo.getUid()==Integer.parseInt(key)){
                                        for (final MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                            chatSql.execSQL("insert into message(myuid,uid,date,recipientorsend,content) values("+sendUserInfo.getUid()+","+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
                                            messages.add(new Message(messagePro.getContent(),TimeType.getMessageTimeText(messagePro.getDate()),Message.RECIPIENT,Message.ISSHOWTIME));
                                        }
                                    } else {
                                        for (final MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                            chatSql.execSQL("insert into message(myuid,uid,date,recipientorsend,content) values("+sendUserInfo.getUid()+","+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
                                            okHttpClient.newCall(new Request.Builder()
                                                    .url("http://39.96.40.12:1008/getuserinfobyuid?uid="+key)
                                                    .build()).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    Log.e("shiniuokhttp", "getuserinfobyuiderror");
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String res = response.body().string();
                                                    JSONObject userinfoJson;
                                                    UserInfo reUserinfo;
                                                    try {
                                                        userinfoJson = JSON.parseObject(res);
                                                        reUserinfo = new UserInfo(Integer.parseInt(key),userinfoJson.getString("name"),userinfoJson.getString("avatar"),userinfoJson.getString("introduction"));
                                                    }catch (Exception e){
                                                        Log.e("shiniu", "getmessageerror:"+e.getMessage());
                                                        timer.cancel();
                                                        return;
                                                    }
                                                    Cursor c = chatSql.rawQuery("select *from nowmessage where uid = "+key+" and myuid = "+sendUserInfo.getUid(),null);
                                                    if (c.moveToFirst()){
                                                        chatSql.execSQL("update nowmessage set " +
                                                                "name='"+reUserinfo.getName()+
                                                                "',introduction='"+reUserinfo.getIntroduction()+
                                                                "',avatar='"+reUserinfo.getAvatar()+
                                                                "',date="+messagePro.getDate()+
                                                                ",content='"+messagePro.getContent()+
                                                                "',count=count+1 where uid = "+key+" and myuid = "+sendUserInfo.getUid());
                                                        notificationUntil.sendNotification(reUserinfo.getName()+"("+(c.getInt(c.getColumnIndex("count"))+1)+"条新消息)",messagePro.getContent(),PendingIntent.getActivity(ChatActivity.this,0,new Intent(ChatActivity.this,ChatActivity.class).putExtra("senduserinfo",sendUserInfo).putExtra("recipientuserinfo",reUserinfo),PendingIntent.FLAG_UPDATE_CURRENT));
                                                        LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                                                    }else {
                                                        chatSql.execSQL("insert into nowmessage values("+
                                                                sendUserInfo.getUid()+","+
                                                                key+ ",'"+
                                                                reUserinfo.getName()+ "','"+
                                                                reUserinfo.getIntroduction()+"','"+
                                                                reUserinfo.getAvatar()+"',"+
                                                                messagePro.getDate()+",'"+
                                                                messagePro.getContent()+"',1)");
                                                        notificationUntil.sendNotification(reUserinfo.getName(),messagePro.getContent(),PendingIntent.getActivity(ChatActivity.this,0,new Intent(ChatActivity.this,ChatActivity.class).putExtra("senduserinfo",sendUserInfo).putExtra("recipientuserinfo",reUserinfo),PendingIntent.FLAG_UPDATE_CURRENT));
                                                        LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                                                    }
                                                    Cursor cursor = chatSql.rawQuery("select *from nowmessage where uid = "+key,null);
                                                    if (cursor.moveToFirst()){
                                                        Log.e("shiniu", "uid:"+ cursor.getInt(cursor.getColumnIndex("uid"))+"name:"+cursor.getString(cursor.getColumnIndex("name"))+"intr:"+cursor.getString(cursor.getColumnIndex("introduction"))+"date:"+cursor.getLong(cursor.getColumnIndex("date"))+"content:"+cursor.getString(cursor.getColumnIndex("content"))+"count:"+cursor.getInt(cursor.getColumnIndex("count")));
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    });
                    Log.e("shiniu", "session:"+session+" uid:"+sendUserInfo.getUid()+" name:"+sendUserInfo.getName()+" introduction:"+sendUserInfo.getIntroduction()+" avatar:"+sendUserInfo.getAvatar());
                }
            }
        }, 0, 5000);
    }
}

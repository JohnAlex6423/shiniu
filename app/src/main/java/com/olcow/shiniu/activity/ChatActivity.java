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
import java.util.Date;
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
    private UserInfo sendUserInfo;
    private UserInfo recipientUserInfo;
    private TextView errorText;
    private SQLiteDatabase chatSql;
    private SQLiteDatabase accSql;
    private String session;
    private Timer timer;
    private OkHttpClient okHttpClient;
    private NotificationUntil notificationUntil;
    private long coolTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        stopService(new Intent(this, GetMessageService.class));
        recipientUserInfo = (UserInfo) getIntent().getSerializableExtra("recipientuserinfo");
        sendEdit = findViewById(R.id.chat_send_edit);
        sendButton = findViewById(R.id.chat_send_button);
        sendButtonNone = findViewById(R.id.chat_send_button_none);
        recyclerView = findViewById(R.id.chat_rec);
        sendCon = findViewById(R.id.chat_send_con);
        errorText = findViewById(R.id.chat_error);
        if (!getSession()){
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        if (!getSendUserInfo()){
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        if (recipientUserInfo == null) {
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
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
        Softkeyboardlistener.setListener(ChatActivity.this, new Softkeyboardlistener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerView.scrollToPosition(messages.size()-1);
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        okHttpClient.newCall(new Request.Builder()
                .url("http://39.96.40.12:1008/getuserinfobyuid?uid=" + recipientUserInfo.getUid())
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(recipientUserInfo.getName());
                    }
                });
                if (sendUserInfo==null){
                    if (!getSendUserInfo()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendCon.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                }
                if (session == null){
                    if (!getSession()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendCon.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                }
                messages = new ArrayList<>();
                if (chatSql==null){
                    chatSql = new ChatDatabaseHelper(ChatActivity.this,"chatmessage",null,1).getReadableDatabase();
                }
                Cursor chatC = chatSql.rawQuery("select *from message where uid="+recipientUserInfo.getUid()+" and myuid = "+sendUserInfo.getUid()+" order by date desc limit 0,20",null);
                if (chatC.moveToLast()){
                    long cacheTime;
                    cacheTime = chatC.getLong(chatC.getColumnIndex("date"));
                    messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                    while (chatC.moveToPrevious()){
                        long cacheTimeNext = chatC.getLong(chatC.getColumnIndex("date"));
                        if (cacheTime - cacheTimeNext>600000){
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                        } else {
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.NOSHOWTIME));
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false));
                        adapter = new ChatAdapter(messages, sendUserInfo, recipientUserInfo);
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
                chatSql.execSQL("update nowmessage set count = 0 where uid = "+recipientUserInfo.getUid()+" and myuid = " + sendUserInfo.getUid());
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                JSONObject userinfoJson;
                UserInfo cacheUserInfo = recipientUserInfo;
                try {
                    userinfoJson = JSON.parseObject(res);
                    recipientUserInfo = new UserInfo(recipientUserInfo.getUid(), userinfoJson.getString("name"), userinfoJson.getString("avatar"), userinfoJson.getString("introduction"));
                } catch (Exception e) {
                    recipientUserInfo = cacheUserInfo;
                    Log.e("shiniu", "get recipientUserinfo ERROR:"+e.getMessage());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(recipientUserInfo.getName());
                    }
                });
                if (sendUserInfo==null){
                    if (!getSendUserInfo()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendCon.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                }
                if (session == null){
                    if (!getSession()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendCon.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                }
                messages = new ArrayList<>();
                if (chatSql==null){
                    chatSql = new ChatDatabaseHelper(ChatActivity.this,"chatmessage",null,1).getReadableDatabase();
                }
                Cursor chatC = chatSql.rawQuery("select *from message where uid="+recipientUserInfo.getUid()+" and myuid = "+sendUserInfo.getUid()+" order by date desc limit 0,20",null);
                if (chatC.moveToLast()){
                    long cacheTime;
                    cacheTime = chatC.getLong(chatC.getColumnIndex("date"));
                    messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                    while (chatC.moveToPrevious()){
                        long cacheTimeNext = chatC.getLong(chatC.getColumnIndex("date"));
                        if (cacheTime - cacheTimeNext>600000){
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                        } else {
                            cacheTime = cacheTimeNext;
                            messages.add(new Message(chatC.getString(chatC.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),chatC.getInt(chatC.getColumnIndex("recipientorsend")),Message.NOSHOWTIME));
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false));
                        adapter = new ChatAdapter(messages, sendUserInfo, recipientUserInfo);
                        recyclerView.setAdapter(adapter);
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
                chatSql.execSQL("update nowmessage set count = 0 where uid = "+recipientUserInfo.getUid()+" and myuid = " + sendUserInfo.getUid());
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage();
                    }
                });
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
            if (!getSession()){
                return;
            }
        }
        if (timer!=null){
            timer.cancel();
            timer = null;
        }
        if (sendUserInfo == null){
            if (!getSendUserInfo()){
                return;
            }
        }
        startService(new Intent(this,GetMessageService.class).putExtra("senduserinfo",sendUserInfo).putExtra("session",session));
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        if (okHttpClient==null){
            okHttpClient = new OkHttpClient();
        }
        if (notificationUntil==null){
            notificationUntil = new NotificationUntil(ChatActivity.this);
        }
        if (session==null){
            if (!getSession()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
        }
        stopService(new Intent(this,GetMessageService.class));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (session==null){
                    if (!getSession()){
                        timer.cancel();
                        timer = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, "当前环境异常!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return;
                }
                if (sendUserInfo == null){
                    if(!getSendUserInfo()){
                        timer.cancel();
                        timer = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, "当前环境异常!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return;
                }
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
                            timer = null;
                            return;
                        }
                        if (keys.isEmpty()){
                            Log.e("shiniu", "空");
                        }else {
                            for (final String key:keys){
                                Log.e("shiniu", "onResponse: "+key);
                                if (key.equals("error")){
                                    timer.cancel();
                                    timer=null;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ChatActivity.this, "当前登陆异常,请退出重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                if (recipientUserInfo.getUid()==Integer.parseInt(key)){
                                    for (MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                        chatSql.execSQL("insert into message(myuid,uid,date,recipientorsend,content) values("+sendUserInfo.getUid()+","+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
                                        if (coolTime==0){
                                            messages.add(new Message(messagePro.getContent(),TimeType.getMessageTimeText(messagePro.getDate()),Message.RECIPIENT,Message.ISSHOWTIME));
                                            coolTime = new Date().getTime();
                                        }else {
                                            long cacheTime = new Date().getTime();
                                            if (coolTime-cacheTime<600000){
                                                messages.add(new Message(messagePro.getContent(),TimeType.getMessageTimeText(messagePro.getDate()),Message.RECIPIENT,Message.NOSHOWTIME));
                                                coolTime = cacheTime;
                                            }else {
                                                messages.add(new Message(messagePro.getContent(),TimeType.getMessageTimeText(messagePro.getDate()),Message.RECIPIENT,Message.ISSHOWTIME));
                                                coolTime = cacheTime;
                                            }
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyItemInserted(messages.size()-1);
                                                recyclerView.scrollToPosition(messages.size()-1);
                                            }
                                        });
                                        Cursor c = chatSql.rawQuery("select *from nowmessage where uid = "+key+" and myuid = "+sendUserInfo.getUid(),null);
                                        if (c.moveToFirst()){
                                            chatSql.execSQL("update nowmessage set " +
                                                    "name='"+recipientUserInfo.getName()+
                                                    "',introduction='"+recipientUserInfo.getIntroduction()+
                                                    "',avatar='"+recipientUserInfo.getAvatar()+
                                                    "',date="+messagePro.getDate()+
                                                    ",content='"+messagePro.getContent()+
                                                    "',count=count+1 where uid = "+key+" and myuid = "+sendUserInfo.getUid());
                                        }else {
                                            chatSql.execSQL("insert into nowmessage values("+
                                                    sendUserInfo.getUid()+","+
                                                    key+ ",'"+
                                                    recipientUserInfo.getName()+ "','"+
                                                    recipientUserInfo.getIntroduction()+"','"+
                                                    recipientUserInfo.getAvatar()+"',"+
                                                    messagePro.getDate()+",'"+
                                                    messagePro.getContent()+"',0)");
                                        }
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
                                                    timer = null;
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
                                                            "',0 where uid = "+key+" and myuid = "+sendUserInfo.getUid());
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
                                                            messagePro.getContent()+"',0)");
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
            }
        }, 5000, 5000);
    }

    private void sendMessage(){
        final String content = sendEdit.getText().toString();
        if (coolTime == 0){
            coolTime = new Date().getTime();
            messages.add(new Message(content,TimeType.getMessageTimeText(coolTime),Message.SEND,Message.ISSHOWTIME));
        }else {
            long cacheTime = new Date().getTime();
            if (coolTime-cacheTime<600000){
                messages.add(new Message(content,TimeType.getMessageTimeText(cacheTime),Message.SEND,Message.NOSHOWTIME));
                coolTime = cacheTime;
            }else {
                messages.add(new Message(content,TimeType.getMessageTimeText(cacheTime),Message.SEND,Message.ISSHOWTIME));
                coolTime = cacheTime;
            }
        }
        sendEdit.setText("");
        adapter.notifyItemInserted(messages.size()-1);
        recyclerView.scrollToPosition(messages.size()-1);
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        Log.e("shiniu", "sendMessage: "+session);
        okHttpClient.newCall(new Request.Builder()
                .url("http://39.96.40.12:1008/sendmessage")
                .post(new FormBody.Builder()
                        .add("session",session)
                        .add("buid",String.valueOf(recipientUserInfo.getUid()))
                        .add("message",content)
                        .build())
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, "发送消息失败，请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                if (res.equals("no login")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatActivity.this, "您的登陆已经失效，请退出后重新登陆", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if (res.length()==13){
                    if (chatSql == null){
                        chatSql = new ChatDatabaseHelper(ChatActivity.this,"chatmessage",null,1).getWritableDatabase();
                    }
                    chatSql.execSQL("insert into message(myuid,uid,date,recipientorsend,content) values("+sendUserInfo.getUid()+","+recipientUserInfo.getUid()+","+res+",1,'"+content+"')");
                    Cursor c = chatSql.rawQuery("select * from nowmessage where uid = "+recipientUserInfo.getUid()+" and myuid = "+sendUserInfo.getUid(),null);
                    if (c.moveToFirst()){
                        chatSql.execSQL("update nowmessage set " +
                                "name='"+recipientUserInfo.getName()+
                                "',introduction='"+recipientUserInfo.getIntroduction()+
                                "',avatar='"+recipientUserInfo.getAvatar()+
                                "',date="+res+
                                ",content='"+content+
                                "',count=count+1 where uid = "+sendUserInfo.getUid()+" and myuid = "+sendUserInfo.getUid());
                    }else {
                        chatSql.execSQL("insert into nowmessage values("+
                                sendUserInfo.getUid()+","+
                                sendUserInfo.getUid()+ ",'"+
                                recipientUserInfo.getName()+ "','"+
                                recipientUserInfo.getIntroduction()+"','"+
                                recipientUserInfo.getAvatar()+"',"+
                                res+",'"+
                                content+"',0)");
                    }
                }else {
                    Toast.makeText(ChatActivity.this, "发送失败，请退出重试", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean getSession(){
        if (accSql == null){
            accSql = new AccountDatabaseHelper(this,"olcowsso",null,1).getWritableDatabase();
        }
        Cursor aa = accSql.rawQuery("select *from account",null);
        if (aa.moveToFirst()){
            session = aa.getString(aa.getColumnIndex("session"));
            return true;
        }else {
            return false;
        }
    }

    private boolean getSendUserInfo(){
        Cursor cursor = accSql.rawQuery("select *from userinfo", null);
        if (cursor.moveToFirst()) {
            sendUserInfo = new UserInfo(cursor.getInt(cursor.getColumnIndex("uid")), cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("avatar")), cursor.getString(cursor.getColumnIndex("introduction")));
            return true;
        } else {
            return false;
        }
    }
}

package com.olcow.shiniu.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.activity.ChatActivity;
import com.olcow.shiniu.entity.MessagePro;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;
import com.olcow.shiniu.until.NotificationUntil;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetMessageService extends Service {

    private Timer timer;
    private OkHttpClient okHttpClient;
    private NotificationUntil notificationUntil;
    private UserInfo sendUserInfo;
    private String session;
    private SQLiteDatabase sqlMessage;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        if (okHttpClient==null){
            okHttpClient = new OkHttpClient();
        }
        if (notificationUntil == null) {
            notificationUntil = new NotificationUntil(this);
        }
        if (sqlMessage == null){
            sqlMessage = new ChatDatabaseHelper(this,"chatmessage",null,1).getReadableDatabase();
        }
        if (context == null){
            context = this;
        }
        sendUserInfo = null;
        session = null;
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("userinfo",new UserInfo());
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        timer = new Timer();
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
                            final JSONObject jsonObject = JSON.parseObject(res);
                            Set<String> keys = jsonObject.keySet();
                            if (keys.isEmpty()){
                                Log.e("shiniu", "空");
                            }else {
                                for (final String key:keys){
                                    Log.e("shiniu", "onResponse: "+key);
                                    for (final MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                        sqlMessage.execSQL("insert into message(uid,date,recipientorsend,content) values("+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
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
                                                JSONObject userinfoJson = JSON.parseObject(res);
                                                UserInfo reUserinfo = new UserInfo(Integer.parseInt(key),userinfoJson.getString("name"),userinfoJson.getString("avatar"),userinfoJson.getString("introduction"));
                                                sqlMessage.execSQL("delete from nowmessage where uid = "+key);
                                                sqlMessage.execSQL("insert into nowmessage values("+
                                                        key+ ",'"+
                                                        reUserinfo.getName()+ "','"+
                                                        reUserinfo.getIntroduction()+"','"+
                                                        reUserinfo.getAvatar()+"',"+
                                                        messagePro.getDate()+",'"+
                                                        messagePro.getContent()+"')");
                                                Cursor cursor = sqlMessage.rawQuery("select *from nowmessage where uid = "+key,null);
                                                if (cursor.moveToFirst()){
                                                    Log.e("shiniu", "uid:"+ cursor.getInt(cursor.getColumnIndex("uid"))+"name:"+cursor.getString(cursor.getColumnIndex("name"))+"intr:"+cursor.getString(cursor.getColumnIndex("introduction"))+"date:"+cursor.getLong(cursor.getColumnIndex("date"))+"content:"+cursor.getString(cursor.getColumnIndex("content")));
                                                }
                                                notificationUntil.sendNotification(reUserinfo.getName(),messagePro.getContent(),PendingIntent.getActivity(context,0,new Intent(context,ChatActivity.class).putExtra("senduserinfo",sendUserInfo).putExtra("recipientuserinfo",reUserinfo),PendingIntent.FLAG_UPDATE_CURRENT));
                                            }
                                        });
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendUserInfo = (UserInfo) intent.getSerializableExtra("senduserinfo");
        session = intent.getStringExtra("session");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

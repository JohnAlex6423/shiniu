package com.olcow.shiniu.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
    private int sendUid;
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
        session = null;
        sendUid = 0;
        if (timer == null){
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!(sendUid ==0||session == null)){
                    okHttpClient.newCall(new Request.Builder()
                            .url("http://39.96.40.12:1008/getmessage")
                            .post(new FormBody.Builder()
                                    .add("session",session)
                                    .build())
                            .build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
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
                                Log.e("shiniu", "getmessageerror:"+res);
                                timer.cancel();
                                return;
                            }
                            if (!keys.isEmpty()){
                                for (final String key:keys){
                                    if (key.equals("error")){
                                        Log.e("shiniu", "getmessageservice get keys error");
                                        timer.cancel();
                                        return;
                                    }
                                    for (final MessagePro messagePro : JSON.parseArray(jsonObject.getString(key),MessagePro.class)){
                                        sqlMessage.execSQL("insert into message(myuid,uid,date,recipientorsend,content) values("+sendUid+","+key+","+messagePro.getDate()+",0,'"+messagePro.getContent()+"')");
                                        okHttpClient.newCall(new Request.Builder()
                                                .url("http://39.96.40.12:1008/getuserinfobyuid?uid="+key)
                                                .build()).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Log.e("shiniuokhttp", "getuserinfobyuiderror");
                                                timer.cancel();
                                                timer = null;
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
                                                Cursor c = sqlMessage.rawQuery("select *from nowmessage where uid = "+key+" and myuid = "+sendUid,null);
                                                if (c.moveToFirst()){
                                                    sqlMessage.execSQL("update nowmessage set " +
                                                            "name='"+reUserinfo.getName()+
                                                            "',introduction='"+reUserinfo.getIntroduction()+
                                                            "',avatar='"+reUserinfo.getAvatar()+
                                                            "',date="+messagePro.getDate()+
                                                            ",content='"+messagePro.getContent()+
                                                            "',count=count+1 where uid = "+key+" and myuid = "+sendUid);
                                                    notificationUntil.sendNotification(reUserinfo.getName()+"("+(c.getInt(c.getColumnIndex("count"))+1)+"条新消息)",messagePro.getContent(),PendingIntent.getActivity(context,0,new Intent(context,ChatActivity.class).putExtra("recipientuserinfo",reUserinfo),PendingIntent.FLAG_UPDATE_CURRENT));
                                                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                                                }else {
                                                    sqlMessage.execSQL("insert into nowmessage values("+
                                                            sendUid+","+
                                                            key+ ",'"+
                                                            reUserinfo.getName()+ "','"+
                                                            reUserinfo.getIntroduction()+"','"+
                                                            reUserinfo.getAvatar()+"',"+
                                                            messagePro.getDate()+",'"+
                                                            messagePro.getContent()+"',1)");
                                                    notificationUntil.sendNotification(reUserinfo.getName(),messagePro.getContent(),PendingIntent.getActivity(context,0,new Intent(context,ChatActivity.class).putExtra("recipientuserinfo",reUserinfo),PendingIntent.FLAG_UPDATE_CURRENT));
                                                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction("mainmessagebadge"));
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }, 0, 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendUid = intent.getIntExtra("senduserinfouid",0);
        session = intent.getStringExtra("session");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }
}

package com.olcow.shiniu.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity {

    private TextView logoutText;
    private LinearLayout deleteMessageCon;

    private String session = "";
    private int uid = 0;

    private SQLiteDatabase accSql;
    private SQLiteDatabase chatSql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        logoutText= findViewById(R.id.setting_logout_text);
        deleteMessageCon = findViewById(R.id.settings_chat_delete_message);
        if (accSql == null){
            accSql = new AccountDatabaseHelper(SettingActivity.this,"olcowsso",null,1).getWritableDatabase();
        }
        Cursor c = accSql.rawQuery("select session,uid from account",null);
        if (c.moveToFirst()){
            session = c.getString(c.getColumnIndex("session"));
            uid = c.getInt(c.getColumnIndex("uid"));
        }else {
            Toast.makeText(this, "当前环境异常，请退出后重试", Toast.LENGTH_SHORT).show();
        }
        deleteMessageCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("确定清空聊天记录？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (uid==0){
                                    if (accSql == null){
                                        accSql = new AccountDatabaseHelper(SettingActivity.this,"olcowsso",null,1).getWritableDatabase();
                                    }
                                    Cursor c = accSql.rawQuery("select uid from account",null);
                                    if (c.moveToFirst()){
                                        uid = c.getInt(c.getColumnIndex("uid"));
                                    }else {
                                        Toast.makeText(SettingActivity.this, "当前环境异常，请退出后重试", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if (chatSql == null){
                                    chatSql = new ChatDatabaseHelper(SettingActivity.this,"chatmessage",null,1).getWritableDatabase();
                                }
                                chatSql.execSQL("delete from message");
                                chatSql.execSQL("delete from nowmessage");
                                Toast.makeText(SettingActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("确认注销吗？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("注销", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (session.equals("")){
                                    if (accSql == null){
                                        accSql = new AccountDatabaseHelper(SettingActivity.this,"olcowsso",null,1).getWritableDatabase();
                                    }
                                    Cursor c = accSql.rawQuery("select session from account",null);
                                    logoutText= findViewById(R.id.setting_logout_text);
                                    if (c.moveToFirst()){
                                        session = c.getString(c.getColumnIndex("session"));
                                    }else {
                                        Toast.makeText(SettingActivity.this, "退出失败,请退出重试", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url("http://39.96.40.12:7703/logout")
                                        .post(new FormBody.Builder().add("session",session).build())
                                        .build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        accSql.execSQL("delete from account");
                                        accSql.execSQL("delete from userinfo");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                Toast.makeText(SettingActivity.this, "退出登陆成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        accSql.execSQL("delete from account");
                                        accSql.execSQL("delete from userinfo");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                Toast.makeText(SettingActivity.this, "退出登陆成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .create();
                alertDialog.show();
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
}

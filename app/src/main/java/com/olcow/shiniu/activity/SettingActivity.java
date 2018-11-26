package com.olcow.shiniu.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity {

    private TextView logoutText;

    private String session = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SQLiteOpenHelper helper = new AccountDatabaseHelper(SettingActivity.this,"olcowsso",null,1);
        final SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("select session from account",null);
        logoutText= findViewById(R.id.setting_logout_text);
        if (c.moveToFirst()){
            session = c.getString(c.getColumnIndex("session"));
        }else {
            Log.e("shiniu", "onCreate: ");
            logoutText.setVisibility(View.GONE);
        }
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://39.96.40.12:7703/logout?session="+session)
                        .get()
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SettingActivity.this, "网络异常,退登失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        if (res.equals("successful")){
                            sqLiteDatabase.execSQL("delete from account");
                            sqLiteDatabase.execSQL("delete from userinfo");
                            Toast.makeText(SettingActivity.this, "已退出登陆", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SettingActivity.this, "服务器异常,请稍后再试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
}

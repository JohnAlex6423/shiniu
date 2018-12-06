package com.olcow.shiniu.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditUserinfoActivity extends AppCompatActivity {
    private MaterialEditText editName;
    private MaterialEditText editIntroduction;
    private Button saveEditUserInfoButton;
    private ProgressBar saveEditProgressBar;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper helper;
    private Boolean coolbtton = true;
    private UserInfo userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("编辑个人资料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);
        editIntroduction = findViewById(R.id.edit_edit_introduction);
        editName = findViewById(R.id.edit_edit_name);
        saveEditUserInfoButton = findViewById(R.id.edit_userinfo_save_button);
        saveEditProgressBar = findViewById(R.id.edit_save_progressbar);
        helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
        sqLiteDatabase = helper.getWritableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("select *from userinfo",null);
        if (c.moveToFirst()){
            editName.setText(c.getString(c.getColumnIndex("name")));
            editIntroduction.setText(c.getString(c.getColumnIndex("introduction")));
            userInfo = new UserInfo();
            userInfo.setUid(c.getInt(c.getColumnIndex("uid")));
            userInfo.setName(c.getString(c.getColumnIndex("name")));
            userInfo.setAvatar(c.getString(c.getColumnIndex("avatar")));
            userInfo.setIntroduction(c.getString(c.getColumnIndex("introduction")));
        }
        editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    Toast.makeText(EditUserinfoActivity.this, "不可以换行哦~", Toast.LENGTH_SHORT).show();
                    return true;
                }else{
                    return false;
                }
            }
        });
        editIntroduction.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    Toast.makeText(EditUserinfoActivity.this, "不可以换行哦~", Toast.LENGTH_SHORT).show();
                    return true;
                }else{
                    return false;
                }
            }
        });
        saveEditUserInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText().toString().length()>12){
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,10f,0f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,-10f,0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).after(animatorY);
                    animatorSet.setDuration(50);
                    animatorSet.start();
                }else if (editIntroduction.getText().toString().length()>32){
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,10f,0f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,-10f,0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).after(animatorY);
                    animatorSet.setDuration(50);
                    animatorSet.start();
                } else {
                    saveEditUserInfoButton.setVisibility(View.INVISIBLE);
                    saveEditProgressBar.setVisibility(View.VISIBLE);
                    Cursor cursor= sqLiteDatabase.rawQuery("select session from account",null);
                    if (cursor.moveToFirst()){
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://39.96.40.12:7703/changeuserinfobysession?session="+cursor.getString(cursor.getColumnIndex("session"))+"&name="+editName.getText().toString()+"&introduction="+editIntroduction.getText().toString())
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EditUserinfoActivity.this, "网络异常请稍后再试", Toast.LENGTH_SHORT).show();
                                        saveEditUserInfoButton.setVisibility(View.VISIBLE);
                                        saveEditProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String res = response.body().string();
                                if (res.equals("successful")){
                                    sqLiteDatabase.execSQL("update userinfo set name ='"+editName.getText().toString()+"',introduction='"+editIntroduction.getText().toString()+"'");
                                    finish();
                                    Intent intent = new Intent();
                                    intent.setAction("changemy");
                                    LocalBroadcastManager.getInstance(EditUserinfoActivity.this).sendBroadcast(intent);
                                }else {
                                    saveEditUserInfoButton.setVisibility(View.VISIBLE);
                                    saveEditProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(EditUserinfoActivity.this, "当前状态异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditUserinfoActivity.this, "你还没登陆", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userinfoedit,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:////主键id 必须这样写
                onBackPressed();//按返回图标直接回退上个界面
                break;
            case R.id.menu_userinfoedit:
                if (coolbtton){
                    if (editName.getText().toString().length()>12){
                        ObjectAnimator animatorX = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,10f,0f);
                        ObjectAnimator animatorY = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,-10f,0f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(animatorX).after(animatorY);
                        animatorSet.setDuration(50);
                        animatorSet.start();
                    }else if (editIntroduction.getText().toString().length()>32){
                        ObjectAnimator animatorX = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,10f,0f);
                        ObjectAnimator animatorY = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,-10f,0f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(animatorX).after(animatorY);
                        animatorSet.setDuration(50);
                        animatorSet.start();
                    } else {
                        coolbtton = false;
                        item.setIcon(R.drawable.ic_autorenew_white_24dp);
                        saveEditUserInfoButton.setVisibility(View.INVISIBLE);
                        saveEditProgressBar.setVisibility(View.VISIBLE);
                        helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
                        sqLiteDatabase = helper.getWritableDatabase();
                        Cursor cursor= sqLiteDatabase.rawQuery("select session from account",null);
                        if (cursor.moveToFirst()){
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://39.96.40.12:7703/changeuserinfobysession?session="+cursor.getString(cursor.getColumnIndex("session"))+"&name="+editName.getText().toString()+"&introduction="+editIntroduction.getText().toString())
                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(EditUserinfoActivity.this, "网络异常请稍后再试", Toast.LENGTH_SHORT).show();
                                            saveEditUserInfoButton.setVisibility(View.VISIBLE);
                                            saveEditProgressBar.setVisibility(View.INVISIBLE);
                                            coolbtton = true;
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String res = response.body().string();
                                    if (res.equals("successful")){
                                        sqLiteDatabase.execSQL("update userinfo set name ='"+editName.getText().toString()+"',introduction='"+editIntroduction.getText().toString()+"'");
                                        finish();
                                        Intent intent = new Intent();
                                        intent.setAction("changemy");
                                        LocalBroadcastManager.getInstance(EditUserinfoActivity.this).sendBroadcast(intent);
                                    }else {
                                        saveEditUserInfoButton.setVisibility(View.VISIBLE);
                                        saveEditProgressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(EditUserinfoActivity.this, "当前状态异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditUserinfoActivity.this, "你还没登陆", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(this, "你点击的太快了!", Toast.LENGTH_SHORT).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}

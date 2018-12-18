package com.olcow.shiniu.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.myview.CircleImageView;
import com.olcow.shiniu.myview.MyScrollView;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;

import jp.wasabeef.glide.transformations.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserinfoActivity extends Activity {

    private ImageView bgImg;
    private Toolbar toolbar;
    private MyScrollView myScrollView;
    private float scale;
    private LinearLayout userinfoAdd;
    private ImageView userinfoSend;

    private TextView name;
    private TextView introduction;
    private CircleImageView avatar;

    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        userInfo = (UserInfo) getIntent().getSerializableExtra("userinfo");
        if (userInfo==null){
            Toast.makeText(this, "当前页面异常,请退出重试!", Toast.LENGTH_SHORT).show();
            scale = Resources.getSystem().getDisplayMetrics().density;
            toolbar = findViewById(R.id.userinfo_toolbar);
            toolbar.getBackground().setAlpha(0);
            bgImg = findViewById(R.id.userinfo_bg);
            name = findViewById(R.id.userinfo_name);
            introduction = findViewById(R.id.userinfo_introduction);
            myScrollView = findViewById(R.id.userinfo_scrollview);
            userinfoAdd = findViewById(R.id.userinfo_add);
            userinfoSend = findViewById(R.id.userinfo_send);
            name.setText("当前页面异常,请退出重试!");
            introduction.setText("你可能是个肮脏的黑客!");
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_loading);
            Glide.with(this)
                    .load(R.drawable.olcowlog_ye_touxiang)
                    .apply(requestOptions.bitmapTransform(
                            new MultiTransformation<>(
                                    new BlurTransformation(25,4),
                                    new ColorFilterTransformation(838860800)))
                            .placeholder(R.drawable.olcowlog_loading))
                    .into(bgImg);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            myScrollView.setOnScrollListener(new MyScrollView.OnScrollListener() {
                @Override
                public void onScroll(int scrollY) {
                    if (scrollY/scale + 0.5f<300-toolbar.getHeight()/scale + 0.5f){
                        int i = Float.valueOf(((scrollY/scale + 0.5f)/(300-toolbar.getHeight()/scale + 0.5f))*255).intValue();
                        toolbar.getBackground().setAlpha(Math.max(i,0));
                        if ((float)i/255>0.5){
                            toolbar.setTitle("加载失败,请退出重试!");
                        }else {
                            toolbar.setTitle("");
                        }
                    } else {
                        toolbar.getBackground().setAlpha(255);
                    }
                }
            });
        } else {
            scale = Resources.getSystem().getDisplayMetrics().density;
            toolbar = findViewById(R.id.userinfo_toolbar);
            toolbar.getBackground().setAlpha(0);
            bgImg = findViewById(R.id.userinfo_bg);
            name = findViewById(R.id.userinfo_name);
            introduction = findViewById(R.id.userinfo_introduction);
            myScrollView = findViewById(R.id.userinfo_scrollview);
            avatar = findViewById(R.id.userinfo_avatar);
            userinfoAdd = findViewById(R.id.userinfo_add);
            name.setText(userInfo.getName());
            introduction.setText(userInfo.getIntroduction());
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_loading);
            Glide.with(this)
                    .load(userInfo.getAvatar())
                    .apply(requestOptions.bitmapTransform(
                            new MultiTransformation<>(
                                    new BlurTransformation(25,4),
                                    new ColorFilterTransformation(838860800))))
                    .into(bgImg);
            Glide.with(this)
                    .load(userInfo.getAvatar())
                    .apply(requestOptions)
                    .into(avatar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            myScrollView.setOnScrollListener(new MyScrollView.OnScrollListener() {
                @Override
                public void onScroll(int scrollY) {
                    if (scrollY/scale + 0.5f<300-toolbar.getHeight()/scale + 0.5f){
                        int i = Float.valueOf(((scrollY/scale + 0.5f)/(300-toolbar.getHeight()/scale + 0.5f))*255).intValue();
                        toolbar.getBackground().setAlpha(Math.max(i,0));
                        if ((float)i/255>0.5){
                            toolbar.setTitle(userInfo.getName());
                        }else {
                            toolbar.setTitle("");
                        }
                    } else {
                        toolbar.getBackground().setAlpha(255);
                    }
                }
            });
            userinfoAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteOpenHelper helper = new AccountDatabaseHelper(UserinfoActivity.this,"olcowsso",null,1);
                    SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                    Cursor cursor = sqLiteDatabase.rawQuery("select session from account",null);
                    if (cursor.moveToFirst()){
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://39.96.40.12:1008/addfriend")
                                .post(new FormBody.Builder()
                                        .add("session",cursor.getString(cursor.getColumnIndex("session")))
                                        .add("buid",String.valueOf(userInfo.getUid())).build())
                                .build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UserinfoActivity.this, "网络错误，请重试", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String res = response.body().string();
                                Log.e("是牛", res);
                                switch (res) {
                                    case "no login":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserinfoActivity.this, "登陆过期，请重新登陆!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    case "no activity":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserinfoActivity.this, "您还未激活，若已激活请点击->我的->刷新状态", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    case "already add":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserinfoActivity.this, "您已关注过此人", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    case "successful":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserinfoActivity.this, "关注成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    default:
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(UserinfoActivity.this, "当前环境异常，请退出重试!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                }
                            }
                        });
                    } else {
                        Toast.makeText(UserinfoActivity.this, "您未登录,不能关注", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.getBackground().setAlpha(255);
                    }
                });
            }
        },200);
        finish();
    }
}
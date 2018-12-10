package com.olcow.shiniu.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.myview.CircleImageView;
import com.olcow.shiniu.myview.MyScrollView;

import jp.wasabeef.glide.transformations.*;

public class UserinfoActivity extends Activity {

    private ImageView bgImg;
    private Toolbar toolbar;
    private MyScrollView myScrollView;
    private float scale;

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
                    finish();
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
                    finish();
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
        }
    }
}
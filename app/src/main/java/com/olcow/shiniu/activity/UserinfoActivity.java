package com.olcow.shiniu.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;

import jp.wasabeef.glide.transformations.*;

public class UserinfoActivity extends Activity {

    private ImageView bgImg;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        toolbar = findViewById(R.id.userinfo_toolbar);
        bgImg = findViewById(R.id.userinfo_bg);
        int i = getIntent().getIntExtra("uid",0);
        Log.e("shiniu", String.valueOf(i));
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
    }
}
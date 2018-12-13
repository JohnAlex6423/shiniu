package com.olcow.shiniu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.fragment.FriendcFragment;
import com.olcow.shiniu.fragment.HomeFragment;
import com.olcow.shiniu.fragment.MessageFragment;
import com.olcow.shiniu.fragment.MyFragment;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String change;
    private String session;

    private Fragment fragementHome;
    private Fragment fragementMessage;
    private Fragment fragementFriendc;
    private Fragment fragementMy;

    private ImageView homeImage;
    private ImageView messageImage;
    private ImageView friendcImage;
    private ImageView myImage;

    private TextView homeText;
    private TextView messageText;
    private TextView friendcText;
    private TextView myText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myText = findViewById(R.id.mytext);
        SQLiteOpenHelper accountHelper=new AccountDatabaseHelper(MainActivity.this,"olcowsso",null,1);
        final SQLiteDatabase sqLiteDatabase = accountHelper.getReadableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("select session from account",null);
        if (c.moveToFirst()){
            session = c.getString(c.getColumnIndex("session"));
            final OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://39.96.40.12:7703/islogin")
                    .post(new FormBody.Builder().add("session",session).build())
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "网络异常!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    switch (res) {
                        case "no login":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "您的登陆已过期!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            sqLiteDatabase.execSQL("delete from account");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myText.setText("未登录");
                                }
                            });
                            break;
                        case "redis error":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "服务器异常,请反馈", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        default:
                            JSONObject jsonObject = JSON.parseObject(res);
                            if (jsonObject.getInteger("permission") != 0) {
                                Request request1 = new Request.Builder()
                                        .url("http://39.96.40.12:7703/getuserinfobysession")
                                        .post(new FormBody.Builder().add("session", session).build())
                                        .build();
                                Call call1 = okHttpClient.newCall(request1);
                                call1.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String userInfo = response.body().string();
                                        switch (userInfo) {
                                            case "null":
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(MainActivity.this, "账号还未激活,请尽快激活", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            case "not exist session":
                                            case "redis error":
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(MainActivity.this, "服务器异常,请稍后再试", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            default:
                                                JSONObject jsonObject1 = JSON.parseObject(userInfo);
                                                try {
                                                    sqLiteDatabase.execSQL("delete from userinfo");
                                                    sqLiteDatabase.execSQL("insert into userinfo(uid,name,avatar,introduction) values(" + jsonObject1.getInteger("uid") + ",'" + jsonObject1.getString("name") + "','" + jsonObject1.getString("avatar") + "','" + jsonObject1.getString("introduction") + "')");
                                                } catch (Exception e) {
                                                    Log.e("shiniu.okhttp", e.getMessage());
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(MainActivity.this, "缓存异常", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                break;
                                        }
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "账号还未激活,如已激活,请去我的->刷新我的用户状态", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            break;
                    }
                }
            });
        }else {
            myText.setText("未登录");
            session = "";
        }
        fragementHome = new HomeFragment();
        fragementMessage = new MessageFragment();
        fragementFriendc = new FriendcFragment();
        fragementMy = new MyFragment();
        ConstraintLayout homeL = findViewById(R.id.homel);
        ConstraintLayout messageL = findViewById(R.id.messagel);
        ConstraintLayout plusL = findViewById(R.id.plusl);
        ConstraintLayout friendcL = findViewById(R.id.friendcl);
        ConstraintLayout myL = findViewById(R.id.myl);

        homeImage = findViewById(R.id.homeimage);
        messageImage = findViewById(R.id.messageimage);
        ImageView plusImage = findViewById(R.id.plusimage);
        friendcImage = findViewById(R.id.friendcimage);
        myImage = findViewById(R.id.myimage);

        homeText = findViewById(R.id.hometext);
        messageText = findViewById(R.id.messagetext);
        friendcText = findViewById(R.id.friendctext);

        homeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_home_ye));
        homeText.setTextColor(Color.parseColor("#F49E38"));
        change = "home";
        getSupportFragmentManager().beginTransaction().add(R.id.fragment,fragementHome,change).commit();

        homeL.setOnClickListener(this);
        messageL.setOnClickListener(this);
        plusL.setOnClickListener(this);
        friendcL.setOnClickListener(this);
        myL.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().beginTransaction().addToBackStack(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.homel:
                if (!change.equals("home")){
                    clean();
                    homeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_home_ye));
                    homeText.setTextColor(Color.parseColor("#F49E38"));
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(homeImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(homeImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(change);
                    Fragment fragmentc = getSupportFragmentManager().findFragmentByTag("home");
                    if (fragmentc!=null){
                        getSupportFragmentManager().beginTransaction().hide(fragment).show(fragmentc).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().hide(fragment).add(R.id.fragment,fragementHome,"home").commit();
                    }
                    change = "home";
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(homeImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(homeImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    return;
                }
                break;
            case R.id.messagel:
                if (!change.equals("message")){
                    clean();
                    messageImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_message_ye));
                    messageText.setTextColor(Color.parseColor("#F49E38"));
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(messageImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(messageImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(change);
                    Fragment fragmentc = getSupportFragmentManager().findFragmentByTag("message");
                    if (fragmentc!=null){
                        getSupportFragmentManager().beginTransaction().hide(fragment).show(fragmentc).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().hide(fragment).add(R.id.fragment,fragementMessage,"message").commit();
                    }
                    change = "message";
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(messageImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(messageImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    return;
                }
                break;
            case R.id.friendcl:
                if (!change.equals("friendc")){
                    clean();
                    friendcImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_friendc_ye));
                    friendcText.setTextColor(Color.parseColor("#F49E38"));
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(friendcImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(friendcImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(change);
                    Fragment fragmentc = getSupportFragmentManager().findFragmentByTag("friendc");
                    if (fragmentc!=null){
                        getSupportFragmentManager().beginTransaction().hide(fragment).show(fragmentc).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().hide(fragment).add(R.id.fragment,fragementFriendc,"friendc").commit();
                    }
                    change = "friendc";
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(friendcImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(friendcImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    return;
                }
                break;
            case R.id.myl:
                if (!change.equals("my")){
                    clean();
                    myImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_my_ye));
                    myText.setTextColor(Color.parseColor("#F49E38"));
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(myImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(myImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(change);
                    Fragment fragmentc = getSupportFragmentManager().findFragmentByTag("my");
                    if (fragmentc!=null){
                        getSupportFragmentManager().beginTransaction().hide(fragment).show(fragmentc).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().hide(fragment).add(R.id.fragment,fragementMy,"my").commit();
                    }
                    change = "my";
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(myImage,"scaleX",1f,1.3f,1f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(myImage,"scaleY",1f,1.3f,1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).with(animatorY);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    return;
                }
                break;
        }
    }

    public void clean(){
        switch (change){
            case "home":
                homeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_home));
                homeText.setTextColor(Color.parseColor("#8A8A8A"));
                break;
            case "message":
                messageImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_message));
                messageText.setTextColor(Color.parseColor("#8A8A8A"));
                break;
            case "friendc":
                friendcImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_friendc));
                friendcText.setTextColor(Color.parseColor("#8A8A8A"));
                break;
            case "my":
                myImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_my));
                myText.setTextColor(Color.parseColor("#8A8A8A"));
        }
    }
}

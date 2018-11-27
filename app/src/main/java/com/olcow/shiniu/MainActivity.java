package com.olcow.shiniu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.fragment.FriendcFragment;
import com.olcow.shiniu.fragment.HomeFragment;
import com.olcow.shiniu.fragment.MessageFragment;
import com.olcow.shiniu.fragment.MyFragment;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
                    .url("http://39.96.40.12:7703/islogin?session="+session)
                    .get()
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
                    if (res.equals("no login")){
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
                    } else if (res.equals("redis error")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "服务器异常,请反馈", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        JSONObject jsonObject = JSON.parseObject(res);
                        if (jsonObject.getInteger("permission")!=0){
                            Request request1 = new Request.Builder()
                                    .url("http://39.96.40.12:7703/getuserinfobysession?session="+session)
                                    .get()
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
                                    if (userInfo.equals("null")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "账号还未激活,请尽快激活", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else if(userInfo.equals("not exist session")||userInfo.equals("redis error")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "服务器异常,请稍后再试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else{
                                        JSONObject jsonObject1=JSON.parseObject(userInfo);
                                        try {
                                            sqLiteDatabase.execSQL("delete from userinfo");
                                            sqLiteDatabase.execSQL("insert into userinfo(uid,name,avatar,introduction) values("+jsonObject1.getInteger("uid")+",'"+jsonObject1.getString("name")+"','"+jsonObject1.getString("avatar")+"','"+jsonObject1.getString("introduction")+"')");
                                        }catch (Exception e){
                                            Log.e("shiniu.okhttp", e.getMessage());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "缓存异常", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
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
    public void onResume(){
        super.onResume();
        SQLiteOpenHelper accountHelper=new AccountDatabaseHelper(MainActivity.this,"olcowsso",null,1);
        final SQLiteDatabase sqLiteDatabase = accountHelper.getReadableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("select session,permission from account",null);
        if (c.moveToFirst()){
            if (!session.equals(c.getString(c.getColumnIndex("session")))){
                session=c.getString(c.getColumnIndex("session"));
                int permission = c.getInt(c.getColumnIndex("permission"));
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("my");
                if (fragment!=null){
                    change = "home";
                    myImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_my));
                    myText.setTextColor(Color.parseColor("#8A8A8A"));
                    homeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_home_ye));
                    homeText.setTextColor(Color.parseColor("#F49E38"));
                    myText.setText("我的");
                    getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(change)).commit();
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    if (permission!=0){
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://39.96.40.12:7703/getuserinfobysession?session="+session)
                                .get()
                                .build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("shiniu.okhttp", "获取用户网络请求错误");
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
                                JSONObject jsonObject1=JSON.parseObject(userInfo);
                                try {
                                    sqLiteDatabase.execSQL("delete from userinfo");
                                    sqLiteDatabase.execSQL("insert into userinfo(uid,name,avatar,introduction) values("+jsonObject1.getInteger("uid")+",'"+jsonObject1.getString("name")+"','"+jsonObject1.getString("avatar")+"','"+jsonObject1.getString("introduction")+"')");
                                }catch (Exception e){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "缓存异常", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                        });
                    }
                } else if (permission!=0){
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://39.96.40.12:7703/getuserinfobysession?session="+session)
                            .get()
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu.okhttp", "获取用户网络请求错误");
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
                            JSONObject jsonObject1=JSON.parseObject(userInfo);
                            try {
                                sqLiteDatabase.execSQL("delete from userinfo");
                                sqLiteDatabase.execSQL("insert into userinfo(uid,name,avatar,introduction) values("+jsonObject1.getInteger("uid")+",'"+jsonObject1.getString("name")+"','"+jsonObject1.getString("avatar")+"','"+jsonObject1.getString("introduction")+"')");
                            }catch (Exception e){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "缓存异常", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    });
                }
            }
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("my");
            if (fragment!=null) {
                change = "home";
                myImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_my));
                myText.setTextColor(Color.parseColor("#8A8A8A"));
                homeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_olcowlog_home_ye));
                homeText.setTextColor(Color.parseColor("#F49E38"));
                myText.setText("我的");
                getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(change)).commit();
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 3:
                if (data==null){
                    Log.e("shiniuf", "onActivityResult: 执行了null");
                    return;
                } else {
                    final Uri resultUri = UCrop.getOutput(data);
                    File file = new File(getCacheDir(),"cropcache.jpeg");
                    File file1 = new File(getCacheDir(),"cropcache.jpeg");
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();
                    RequestBody fileBody = RequestBody.create(MediaType.parse("file"), file);
                    RequestBody fileBody1 = RequestBody.create(MediaType.parse("file"), file1);
                    final RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file",file.getName(),fileBody)
                            .addFormDataPart("file",file1.getName(),fileBody1)
                            .build();
                    final Request request = new Request.Builder()
                            .url("http://123.206.93.200:8080/upload")
                            .post(requestBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(MainActivity.this, "网络异常,请稍后再试", Toast.LENGTH_SHORT).show();
                            Log.e("shiniu", "onFailure: 失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            String imgfile = JSONArray.parseArray(res).get(0).toString();
                            final String imgsrc ="'http://123.206.93.200/uploadimg/" +imgfile+"'";
                            Request request1 = new Request.Builder()
                                    .url("http://39.96.40.12:7703/changeuserinfobysession?session="+session+"&avatar=http://123.206.93.200/uploadimg/"+imgfile)
                                    .get()
                                    .build();
                            client.newCall(request1).enqueue(new Callback() {
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
                                    String res = response.body().string();
                                    if (res.equals("successful")){
                                        SQLiteOpenHelper helper = new AccountDatabaseHelper(MainActivity.this,"olcowsso",null,1);
                                        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                                        sqLiteDatabase.execSQL("update userinfo set avatar="+imgsrc);
                                        Intent intent = new Intent();
                                        intent.setAction("changemyavatar");
                                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
//                                        startActivity(new Intent(MainActivity.this,MainActivity.class));
//                                        finish();
                                    } else if (res.equals("session error")){
                                        SQLiteOpenHelper helper = new AccountDatabaseHelper(MainActivity.this,"olcowsso",null,1);
                                        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                                        sqLiteDatabase.execSQL("delete from account");
                                        sqLiteDatabase.execSQL("delete from userinfo");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "登陆失效,请重新登陆", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        startActivity(new Intent(MainActivity.this,MainActivity.class));
                                        finish();
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "服务器异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "头像已上传", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                break;
            default:
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

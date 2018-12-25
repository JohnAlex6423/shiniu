package com.olcow.shiniu.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.alibaba.fastjson.JSONArray;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.myview.CircleImageView;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditUserinfoActivity extends AppCompatActivity {
    private MaterialEditText editName;
    private MaterialEditText editIntroduction;
    private TextView editNid;
    private CircleImageView avatarImg;
    private Button saveEditUserInfoButton;
    private ProgressBar saveEditProgressBar;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper helper;
    private Boolean coolbtton = true;
    private String avatar;
    private String session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("编辑个人资料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);
        editIntroduction = findViewById(R.id.edit_edit_introduction);
        editName = findViewById(R.id.edit_edit_name);
        editNid = findViewById(R.id.edit_nid);
        saveEditUserInfoButton = findViewById(R.id.edit_userinfo_save_button);
        saveEditProgressBar = findViewById(R.id.edit_save_progressbar);
        avatarImg = findViewById(R.id.edit_edit_avatar);
        helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
        sqLiteDatabase = helper.getWritableDatabase();
        Cursor sessionC = sqLiteDatabase.rawQuery("select session from account",null);
        if (sessionC.moveToFirst()){
            session = sessionC.getString(sessionC.getColumnIndex("session"));
        }else {
            sqLiteDatabase.execSQL("delete from account");
            sqLiteDatabase.execSQL("delete from userinfo");
            Toast.makeText(this, "当前环境异常，请重新登陆", Toast.LENGTH_SHORT).show();
            finish();
        }
        Cursor c = sqLiteDatabase.rawQuery("select *from userinfo",null);
        if (c.moveToFirst()){
            editNid.setText("NID:"+c.getInt(c.getColumnIndex("uid")));
            editName.setText(c.getString(c.getColumnIndex("name")));
            editIntroduction.setText(c.getString(c.getColumnIndex("introduction")));
            avatar = c.getString(c.getColumnIndex("avatar"));
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);
            Glide.with(this)
                    .load(avatar)
                    .apply(requestOptions)
                    .into(avatarImg);
            avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK,null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                    startActivityForResult(intent,2);
                }
            });
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
                                    .url("http://39.96.40.12:7703/changeuserinfobysession")
                                    .post(new FormBody.Builder()
                                            .add("session",cursor.getString(cursor.getColumnIndex("session")))
                                            .add("name",editName.getText().toString())
                                            .add("introduction",editIntroduction.getText().toString())
                                            .build())
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
        }else {
            sqLiteDatabase.execSQL("delete from account");
            sqLiteDatabase.execSQL("delete from userinfo");
            Toast.makeText(this, "当前环境异常，请重新登陆", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 2:
                Uri uri;
                if (data == null){
                    return;
                }
                uri = data.getData();
                UCrop.of(uri, Uri.fromFile(new File(this.getCacheDir(),"cropcache.jpeg")))
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(500, 500)
                        .start(this,3);
                break;
            case 3:
                if (data==null){
                    return;
                } else {
//                    final Uri resultUri = UCrop.getOutput(data);
                    File file = new File(getCacheDir(),"cropcache.jpeg");
                    final OkHttpClient client = new OkHttpClient.Builder()
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();
                    RequestBody fileBody = RequestBody.create(MediaType.parse("file"), file);
                    final RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file",file.getName(),fileBody)
                            .addFormDataPart("oldavatar",avatar.substring(35))
                            .build();
                    Log.e("shiniu", avatar.substring(35));
                    final Request request = new Request.Builder()
                            .url("http://123.206.93.200:8080/uploadavatar")
                            .post(requestBody)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditUserinfoActivity.this, "网络异常,请稍后再试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            final String imgfile = JSONArray.parseArray(res).get(0).toString();
                            final String imgsrc ="http://123.206.93.200/uploadavatar/" +imgfile;
                            Request request1 = new Request.Builder()
                                    .url("http://39.96.40.12:7703/changeuserinfobysession")
                                    .post(new FormBody.Builder()
                                            .add("session",session)
                                            .add("avatar","http://123.206.93.200/uploadavatar/"+imgfile)
                                            .build())
                                    .build();
                            client.newCall(request1).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(EditUserinfoActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String res = response.body().string();
                                    if (res.equals("successful")){
                                        SQLiteOpenHelper helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
                                        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                                        sqLiteDatabase.execSQL("update userinfo set avatar='"+imgsrc+"'");
                                        avatar = imgsrc;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateAvatar();
                                                Toast.makeText(EditUserinfoActivity.this, "头像已上传", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if (res.equals("session error")){
                                        SQLiteOpenHelper helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
                                        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                                        sqLiteDatabase.execSQL("delete from account");
                                        sqLiteDatabase.execSQL("delete from userinfo");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(EditUserinfoActivity.this, "登陆失效,请重新登陆", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        startActivity(new Intent(EditUserinfoActivity.this,MainActivity.class));
                                        finish();
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(EditUserinfoActivity.this, "服务器异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userinfoedit,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent();
        intent.setAction("changemy");
        LocalBroadcastManager.getInstance(EditUserinfoActivity.this).sendBroadcast(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:////主键id 必须这样写
                onBackPressed();//按返回图标直接回退上个界面
                break;
            case R.id.menu_userinfoedit:
                if (coolbtton){
                    if (editName.getText().toString().length()>12 ||editName.getText().toString().length()<1){
                        ObjectAnimator animatorX = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,10f,0f);
                        ObjectAnimator animatorY = ObjectAnimator.ofFloat(saveEditUserInfoButton,"translationX",0f,-10f,0f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(animatorX).after(animatorY);
                        animatorSet.setDuration(50);
                        animatorSet.start();
                    }else if (editIntroduction.getText().toString().length()>32||editName.getText().toString().length()<1){
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
                                    .url("http://39.96.40.12:7703/changeuserinfobysession")
                                    .post(new FormBody.Builder()
                                            .add("session",cursor.getString(cursor.getColumnIndex("session")))
                                            .add("name",editName.getText().toString())
                                            .add("introduction",editIntroduction.getText().toString())
                                            .build())
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
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                saveEditUserInfoButton.setVisibility(View.VISIBLE);
                                                saveEditProgressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(EditUserinfoActivity.this, "当前状态异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
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

    public void updateAvatar(){
        SQLiteOpenHelper helper = new AccountDatabaseHelper(EditUserinfoActivity.this,"olcowsso",null,1);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("select avatar from userinfo",null);
        if (c.moveToFirst()){
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);
            Glide.with(this)
                    .load(c.getString(c.getColumnIndex("avatar")))
                    .apply(requestOptions)
                    .into(avatarImg);
        }
    }
}

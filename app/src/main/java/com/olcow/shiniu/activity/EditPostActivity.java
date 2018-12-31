package com.olcow.shiniu.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;


public class EditPostActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;
    private TextView clearText;
    private TextView sendPostText;
    private TextView nameText;
    private String session;
    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private ImageView img4;
    private ImageView img5;
    private ImageView img6;
    private ImageView addImg;
    private int imgCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        if (sqLiteDatabase == null){
            sqLiteDatabase = new AccountDatabaseHelper(this,"olcowsso",null,1).getReadableDatabase();
        }
        initPostImg();
        clearText = findViewById(R.id.edit_post_clear);
        sendPostText = findViewById(R.id.edit_post_send_text);
        nameText = findViewById(R.id.edit_post_name_text);
        addImg = findViewById(R.id.edit_post_addimg);
        Cursor c = sqLiteDatabase.rawQuery("select name from userinfo",null);
        String name;
        if (c.moveToFirst()){
            name = c.getString(c.getColumnIndex("name"));
        } else {
            name = "你还未登陆";
        }
        nameText.setText(name);
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent,imgCount);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (data == null ){
                    return;
                }
                Uri uri1 = data.getData();
                UCrop.of(uri1,Uri.fromFile(new File(getCacheDir(),"sendpostimg1.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,7);
                break;
            case 2:
                if (data == null ){
                    return;
                }
                Uri uri2 = data.getData();
                Glide.with(this).load(uri2).into(img2);
                img2.setVisibility(View.VISIBLE);
                UCrop.of(uri2,Uri.fromFile(new File(getCacheDir(),"sendpostimg2.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,8);
                break;
            case 3:
                if (data == null ){
                    return;
                }
                Uri uri3 = data.getData();
                Glide.with(this).load(uri3).into(img3);
                img3.setVisibility(View.VISIBLE);
                UCrop.of(uri3,Uri.fromFile(new File(getCacheDir(),"sendpostimg3.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,9);
                break;
            case 4:
                if (data == null ){
                    return;
                }
                Uri uri4 = data.getData();
                Glide.with(this).load(uri4).into(img4);
                img4.setVisibility(View.VISIBLE);
                UCrop.of(uri4,Uri.fromFile(new File(getCacheDir(),"sendpostimg4.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,10);
                break;
            case 5:
                if (data == null ){
                    return;
                }
                Uri uri5 = data.getData();
                Glide.with(this).load(uri5).into(img5);
                img5.setVisibility(View.VISIBLE);
                UCrop.of(uri5,Uri.fromFile(new File(getCacheDir(),"sendpostimg5.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,11);
                break;
            case 6:
                if (data == null ){
                    return;
                }
                Uri uri6 = data.getData();
                Glide.with(this).load(uri6).into(img6);
                img6.setVisibility(View.VISIBLE);
                UCrop.of(uri6,Uri.fromFile(new File(getCacheDir(),"sendpostimg6.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,12);
                break;
            case 7:
                Uri uri7 = data.getData();
                Glide.with(this).load(uri7).into(img1);
                img1.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void initPostImg(){
        float scale = Resources.getSystem().getDisplayMetrics().density;
        img1 = findViewById(R.id.edit_post_img1);
        img2 = findViewById(R.id.edit_post_img2);
        img3 = findViewById(R.id.edit_post_img3);
        img4 = findViewById(R.id.edit_post_img4);
        img5 = findViewById(R.id.edit_post_img5);
        img6 = findViewById(R.id.edit_post_img6);
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int imgWidth = (width - (int) (20*scale + 0.5f)) /3;
        ViewGroup.LayoutParams params = img1.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
        params = img2.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
        params = img3.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
        params = img4.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
        params = img5.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
        params = img6.getLayoutParams();
        params.width = imgWidth;
        params.height = imgWidth;
    }

    private void getSession(){
        if (sqLiteDatabase == null){
            sqLiteDatabase = new AccountDatabaseHelper(this,"olcowsso",null,1).getReadableDatabase();
        }
        Cursor c = sqLiteDatabase.rawQuery("select session from account",null);
        if (c.moveToFirst()){
            session = c.getString(c.getColumnIndex("session"));
        }else {
            session = "error";
        }
    }
}

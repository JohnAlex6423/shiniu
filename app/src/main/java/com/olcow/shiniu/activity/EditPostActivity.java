package com.olcow.shiniu.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class EditPostActivity extends AppCompatActivity implements View.OnClickListener {

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
    private TextView img1Clean;
    private TextView img1Edit;
    private TextView img2Clean;
    private TextView img2Edit;
    private TextView img3Clean;
    private TextView img3Edit;
    private TextView img4Clean;
    private TextView img4Edit;
    private TextView img5Clean;
    private TextView img5Edit;
    private TextView img6Clean;
    private TextView img6Edit;
    private ImageView addImg;
    private int imgCount = 1;
    private int cropIndex;
    private OkHttpClient okHttpClient;
    private MaterialEditText postEdit;
    private ProgressBar progressBar;

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
        postEdit = findViewById(R.id.edit_post_edit);
        progressBar = findViewById(R.id.edit_post_progress);
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
        sendPostText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgCount<2){
                    if (postEdit.getText().toString().length()==0){
                        Toast.makeText(EditPostActivity.this, "发表内容不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    if (okHttpClient==null){
                        okHttpClient = new OkHttpClient();
                    }
                    if (session == null){
                        getSession();
                    }
                    if (session.equals("error")){
                        Toast.makeText(EditPostActivity.this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    okHttpClient.newCall(new Request.Builder()
                            .url("http://39.96.40.12:7678/post/addpost")
                            .post(new FormBody.Builder()
                                    .add("session",session)
                                    .add("content",postEdit.getText().toString())
                                    .add("imgs","").build())
                            .build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditPostActivity.this, "网络链接失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String res = response.body().string();
                            switch (res) {
                                case "successful":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(EditPostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    finish();
                                    break;
                                case "no login":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(EditPostActivity.this, "当前登陆失效，请重新登陆", Toast.LENGTH_SHORT).show();
                                            sqLiteDatabase.execSQL("delete from account", null);
                                            sqLiteDatabase.execSQL("delete from userinfo", null);
                                            Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    });
                                    break;
                                case "error":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(EditPostActivity.this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                default:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("shiniu", "run: "+res);
                                            Toast.makeText(EditPostActivity.this, "环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                        }
                    });
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    for (int i = 1;i<imgCount;i++){
                        File file = new File(getCacheDir(),"sendpostimg"+i+".jpeg");
                        mBuilder.addFormDataPart("file",file.getName(),RequestBody.create(MediaType.parse("file"),file));
                    }
                    if (okHttpClient == null){
                        okHttpClient = new OkHttpClient();
                    }
                    okHttpClient.newCall(new Request.Builder()
                            .url("http://123.206.93.200:8080/upload")
                            .post(mBuilder.build())
                            .build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditPostActivity.this, "网络异常，请重试", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            if (!res.substring(0, 1).equals("[")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EditPostActivity.this, "上传图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                                return;
                            }
                            if (okHttpClient==null){
                                okHttpClient = new OkHttpClient();
                            }
                            if (session == null){
                                getSession();
                            }
                            if (session.equals("error")){
                                Toast.makeText(EditPostActivity.this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                                return;
                            }
                            okHttpClient.newCall(new Request.Builder()
                                    .url("http://39.96.40.12:7678/post/addpost")
                                    .post(new FormBody.Builder()
                                            .add("session",session)
                                            .add("content",postEdit.getText().toString())
                                            .add("imgs",res).build())
                                    .build()).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(EditPostActivity.this, "网络链接失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final String res = response.body().string();
                                    switch (res) {
                                        case "successful":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(EditPostActivity.this, "发表成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            finish();
                                            break;
                                        case "no login":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(EditPostActivity.this, "当前登陆失效，请重新登陆", Toast.LENGTH_SHORT).show();
                                                    sqLiteDatabase.execSQL("delete from account", null);
                                                    sqLiteDatabase.execSQL("delete from userinfo", null);
                                                    Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                            break;
                                        case "error":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(EditPostActivity.this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            break;
                                            default:
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.e("shiniu", "run: "+res);
                                                        Toast.makeText(EditPostActivity.this, "环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    });
                }
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
                UCrop.of(uri2,Uri.fromFile(new File(getCacheDir(),"sendpostimg2.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,8);
                break;
            case 3:
                if (data == null ){
                    return;
                }
                Uri uri3 = data.getData();
                UCrop.of(uri3,Uri.fromFile(new File(getCacheDir(),"sendpostimg3.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,9);
                break;
            case 4:
                if (data == null ){
                    return;
                }
                Uri uri4 = data.getData();
                UCrop.of(uri4,Uri.fromFile(new File(getCacheDir(),"sendpostimg4.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,10);
                break;
            case 5:
                if (data == null ){
                    return;
                }
                Uri uri5 = data.getData();
                UCrop.of(uri5,Uri.fromFile(new File(getCacheDir(),"sendpostimg5.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,11);
                break;
            case 6:
                if (data == null ){
                    return;
                }
                Uri uri6 = data.getData();
                UCrop.of(uri6,Uri.fromFile(new File(getCacheDir(),"sendpostimg6.jpeg")))
                        .withMaxResultSize(1000,1000)
                        .start(this,12);
                break;
            case 7:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg1.jpeg");
                    if (bitmap != null ){
                        img1.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img1.setVisibility(View.VISIBLE);
                    img1Clean.setVisibility(View.VISIBLE);
                    img1Edit.setVisibility(View.VISIBLE);
                    img2.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 8:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg2.jpeg");
                    if (bitmap != null ){
                        img2.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img2Clean.setVisibility(View.VISIBLE);
                    img2Edit.setVisibility(View.VISIBLE);
                    img3.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 9:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg3.jpeg");
                    if (bitmap != null ){
                        img3.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img3Clean.setVisibility(View.VISIBLE);
                    img3Edit.setVisibility(View.VISIBLE);
                    img4.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 10:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg4.jpeg");
                    if (bitmap != null ){
                        img4.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img4Clean.setVisibility(View.VISIBLE);
                    img4Edit.setVisibility(View.VISIBLE);
                    img5.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 11:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg5.jpeg");
                    if (bitmap != null ){
                        img5.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img5Clean.setVisibility(View.VISIBLE);
                    img5Edit.setVisibility(View.VISIBLE);
                    img6.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 12:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg6.jpeg");
                    if (bitmap != null ){
                        img6.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    img6Clean.setVisibility(View.VISIBLE);
                    img6Edit.setVisibility(View.VISIBLE);
                    imgCount+=1;
                }
                break;
            case 20:
                if (resultCode == -1){
                    Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg"+cropIndex+".jpeg");
                    if (bitmap != null ){
                        getImg(cropIndex).setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
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
        img1Clean = findViewById(R.id.edit_post_img1_clean);
        img1Edit = findViewById(R.id.edit_post_img1_edit);
        img2Clean = findViewById(R.id.edit_post_img2_clean);
        img2Edit = findViewById(R.id.edit_post_img2_edit);
        img3Clean = findViewById(R.id.edit_post_img3_clean);
        img3Edit = findViewById(R.id.edit_post_img3_edit);
        img4Clean = findViewById(R.id.edit_post_img4_clean);
        img4Edit = findViewById(R.id.edit_post_img4_edit);
        img5Clean = findViewById(R.id.edit_post_img5_clean);
        img5Edit = findViewById(R.id.edit_post_img5_edit);
        img6Clean = findViewById(R.id.edit_post_img6_clean);
        img6Edit = findViewById(R.id.edit_post_img6_edit);
        img1Clean.setOnClickListener(this);
        img2Clean.setOnClickListener(this);
        img3Clean.setOnClickListener(this);
        img4Clean.setOnClickListener(this);
        img5Clean.setOnClickListener(this);
        img6Clean.setOnClickListener(this);
        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        img4.setOnClickListener(this);
        img5.setOnClickListener(this);
        img6.setOnClickListener(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int imgWidth = (point.x - (int) (20*scale + 0.5f)) /3;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit_post_img1_clean:
                if (imgCount-1>1){
                    File file = new File(getCacheDir(),"sendpostimg1.jpeg");
                    if (file.exists()){
                        if (!file.delete()){
                            Toast.makeText(this, "当前环境异常，请退出重试1", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    for (int i = 2;i<=imgCount-1;i++){
                        File cacheFile = new File(getCacheDir(),"sendpostimg"+i+".jpeg");
                        cacheFile.renameTo(new File(getCacheDir(),"sendpostimg"+(i-1)+".jpeg"));
                    }
                    imgCount-=1;
                    if (imgCount!=6){
                        getImg(imgCount+1).setVisibility(View.INVISIBLE);
                        getImgClean(imgCount+1).setVisibility(View.GONE);
                        getImgEdit(imgCount+1).setVisibility(View.GONE);
                        getImg(imgCount).setImageBitmap(getVectorBitmap());
                        getImgClean(imgCount).setVisibility(View.GONE);
                        getImgEdit(imgCount).setVisibility(View.GONE);
                    }else {
                        getImgClean(imgCount).setVisibility(View.GONE);
                        getImgEdit(imgCount).setVisibility(View.GONE);
                        getImg(imgCount).setImageBitmap(getVectorBitmap());
                    }
                    for (int i = 1;i<=imgCount-1;i++){
                        Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg"+i+".jpeg");
                        if (bitmap != null ){
                            ImageView img = getImg(i);
                            if (img!=null){
                                img.setImageBitmap(bitmap);
                            }else {
                                Toast.makeText(this, "当前环境异常，请退出重试2", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }else{
                            Toast.makeText(this, "当前环境异常，请退出重试3", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }else {
                    File file = new File(getCacheDir(),"sendpostimg1.jpeg");
                    if (!file.exists()){
                        img1.setVisibility(View.INVISIBLE);
                        img2.setVisibility(View.INVISIBLE);
                        imgCount-=1;
                    }else {
                        if (file.delete()){
                            img1.setVisibility(View.INVISIBLE);
                            img1Edit.setVisibility(View.GONE);
                            img1Clean.setVisibility(View.GONE);
                            img2.setVisibility(View.INVISIBLE);
                            imgCount-=1;
                        }else {
                            Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                            imgCount-=1;
                        }
                    }
                }
                break;
            case R.id.edit_post_img2_clean:
                delImg(2);
                break;
            case R.id.edit_post_img3_clean:
                delImg(3);
                break;
            case R.id.edit_post_img4_clean:
                delImg(4);
                break;
            case R.id.edit_post_img5_clean:
                delImg(5);
                break;
            case R.id.edit_post_img6_clean:
                File file = new File(getCacheDir(),"sendpostimg6.jpeg");
                if (!file.exists()){
                    img6.setImageBitmap(getVectorBitmap());
                    img6.setVisibility(View.GONE);
                    img6.setVisibility(View.GONE);
                    imgCount-=1;
                }else {
                    if (file.delete()){
                        img6.setImageBitmap(getVectorBitmap());
                        img6.setVisibility(View.GONE);
                        img6.setVisibility(View.GONE);
                        imgCount-=1;
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                        imgCount-=1;
                    }
                }
                break;
            case R.id.edit_post_img1:
                imgCrop(1);
                break;
            case R.id.edit_post_img2:
                imgCrop(2);
                break;
            case R.id.edit_post_img3:
                imgCrop(3);
                break;
            case R.id.edit_post_img4:
                imgCrop(4);
                break;
            case R.id.edit_post_img5:
                imgCrop(5);
                break;
            case R.id.edit_post_img6:
                imgCrop(6);
                break;
        }
    }
    private Bitmap getLocalBitmap(String url){
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    private Bitmap getVectorBitmap(){
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = this.getDrawable(R.drawable.ic_img_plus);
            if (vectorDrawable!=null){
                bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getMinimumHeight(),Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
                vectorDrawable.draw(canvas);
            }
        }else {
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_img_plus);
        }
        return bitmap;
    }
    private ImageView getImg(int index){
        switch (index){
            case 1:
                return img1;
            case 2:
                return img2;
            case 3:
                return img3;
            case 4:
                return img4;
            case 5:
                return img5;
            case 6:
                return img6;
            default:
                return null;
        }
    }

    private TextView getImgClean(int index){
        switch (index){
            case 1:
                return img1Clean;
            case 2:
                return img2Clean;
            case 3:
                return img3Clean;
            case 4:
                return img4Clean;
            case 5:
                return img5Clean;
            case 6:
                return img6Clean;
            default:
                return null;
        }
    }

    private TextView getImgEdit(int index){
        switch (index){
            case 1:
                return img1Edit;
            case 2:
                return img2Edit;
            case 3:
                return img3Edit;
            case 4:
                return img4Edit;
            case 5:
                return img5Edit;
            case 6:
                return img6Edit;
            default:
                return null;
        }
    }

    private void delImg(int index){
        if (imgCount-index>1){
            File file = new File(getCacheDir(),"sendpostimg"+index+".jpeg");
            if (file.exists()){
                if (!file.delete()){
                    Toast.makeText(this, "当前环境异常，请退出重试1", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            for (int i = index+1;i<=imgCount-1;i++){
                File cacheFile = new File(getCacheDir(),"sendpostimg"+i+".jpeg");
                cacheFile.renameTo(new File(getCacheDir(),"sendpostimg"+(i-1)+".jpeg"));
            }
            imgCount-=1;
            if (imgCount!=6){
                getImg(imgCount+1).setVisibility(View.INVISIBLE);
                getImgClean(imgCount+1).setVisibility(View.GONE);
                getImgEdit(imgCount+1).setVisibility(View.GONE);
                getImg(imgCount).setImageBitmap(getVectorBitmap());
                getImgClean(imgCount).setVisibility(View.GONE);
                getImgEdit(imgCount).setVisibility(View.GONE);
            }else {
                getImgClean(imgCount).setVisibility(View.GONE);
                getImgEdit(imgCount).setVisibility(View.GONE);
                getImg(imgCount).setImageBitmap(getVectorBitmap());
            }
            for (int i = index;i<=imgCount-1;i++){
                Bitmap bitmap = getLocalBitmap(getCacheDir()+"/sendpostimg"+i+".jpeg");
                if (bitmap != null ){
                    ImageView img = getImg(i);
                    if (img!=null){
                        img.setImageBitmap(bitmap);
                    }else {
                        Toast.makeText(this, "当前环境异常，请退出重试2", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    Toast.makeText(this, "当前环境异常，请退出重试3", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }else {
            File file = new File(getCacheDir(),"sendpostimg"+index+".jpeg");
            if (!file.exists()){
                getImg(index).setImageBitmap(getVectorBitmap());
                getImgClean(index).setVisibility(View.GONE);
                getImgEdit(index).setVisibility(View.GONE);
                getImg(index+1).setVisibility(View.INVISIBLE);
                imgCount-=1;
            }else {
                if (file.delete()){
                    getImg(index).setImageBitmap(getVectorBitmap());
                    getImgClean(index).setVisibility(View.GONE);
                    getImgEdit(index).setVisibility(View.GONE);
                    getImg(index+1).setVisibility(View.INVISIBLE);
                    imgCount-=1;
                }else {
                    Toast.makeText(this, "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                    imgCount-=1;
                }
            }
        }
    }

    private void imgCrop(int index){
        if (index==imgCount){
            Intent intent = new Intent(Intent.ACTION_PICK,null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            startActivityForResult(intent,imgCount);
        }else {
            cropIndex = index;
            UCrop.of(Uri.fromFile(new File(getCacheDir(),"sendpostimg"+index+".jpeg")),Uri.fromFile(new File(getCacheDir(),"sendpostimg"+index+".jpeg")))
                    .withMaxResultSize(1000,1000)
                    .start(this,20);
        }
    }
}

package com.olcow.shiniu.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.MainActivity;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.EditUserinfoActivity;
import com.olcow.shiniu.activity.LoginActivity;
import com.olcow.shiniu.activity.SettingActivity;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {

    private boolean coolButton = true;

    ImageView myAvatarImage;

    TextView myNameText;
    TextView myIntroduction;

    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SQLiteOpenHelper accountHelper = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1);
        final SQLiteDatabase sqLiteDatabase = accountHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select session,uid,permission from account",null);
        if (cursor.moveToFirst()){
            final String session = cursor.getString(cursor.getColumnIndex("session"));
            if (cursor.getInt(cursor.getColumnIndex("permission"))==0){
                View view = inflater.inflate(R.layout.fragment_my_noactivity,container,false);
                Button refreshActivityButton = view.findViewById(R.id.refresh_activity_button);
                final LinearLayout sendActivityText = view.findViewById(R.id.send_activity_text);
                refreshActivityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final OkHttpClient okHttpClient = new OkHttpClient();
                        final Request request = new Request.Builder()
                                .url("http://39.96.40.12:7703/updatesessionbypermission?session="+session)
                                .get()
                                .build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "网络异常,请稍后再试", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String res = response.body().string();
                                switch (res) {
                                    case "no login":
                                        sqLiteDatabase.execSQL("delete from account");
                                        sqLiteDatabase.execSQL("delete from userinfo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "登陆失效", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    case "is activity":
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "您已激活", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getActivity(), MainActivity.class));
                                                getActivity().finish();
                                            }
                                        });
                                        break;
                                    case "no activity":
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "您还未激活", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                    case "update successful":
                                        Request request1 = new Request.Builder()
                                                .url("http://39.96.40.12:7703/islogin?session=" + session)
                                                .build();
                                        Call call1 = okHttpClient.newCall(request1);
                                        call1.enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getActivity(), "网络异常请重试", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                String res = response.body().string();
                                                if (res.equals("no login") || res.equals("redis error")) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getActivity(), "服务器异常,请重试", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    JSONObject jsonObject = JSON.parseObject(res);
                                                    sqLiteDatabase.execSQL("delete from account");
                                                    sqLiteDatabase.execSQL("insert into account(session,uid,username,email,permission) values('" + session + "'," + jsonObject.getInteger("uid") + ",'" + jsonObject.getString("username") + "','" + jsonObject.getString("email") + "'," + jsonObject.getInteger("permission") + ")");
                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                    getActivity().finish();
                                                }
                                            }
                                        });
                                        break;
                                    default:
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "服务器异常,请重试", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        break;
                                }
                            }
                        });
                    }
                });
                sendActivityText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (coolButton){
                            OkHttpClient okHttpClient = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://39.96.40.12:7703/sendactivityemail?session="+session)
                                    .get()
                                    .build();
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "网络异常,请检查网络", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String res = response.body().string();
                                    switch (res) {
                                        case "account is activity":
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "账号已被激活", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                                    getActivity().finish();
                                                }
                                            });
                                            break;
                                        case "no login":
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "账号已过期请重新登陆", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                            break;
                                        case "successful":
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "激活邮件已发送,请去邮箱查看", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            break;
                                    }
                                }
                            });
                        }else {
                            coolButton = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "您点击的太快了,请退出重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                return view;
            } else {
                View view;
                UserInfo userInfo = new UserInfo();
                Cursor userC = sqLiteDatabase.rawQuery("select *from userinfo",null);
                if (userC.moveToFirst()){
                     view = inflater.inflate(R.layout.fragment_my,container,false);
                     userInfo.setAvatar(userC.getString(userC.getColumnIndex("avatar")));
                     userInfo.setName(userC.getString(userC.getColumnIndex("name")));
                     userInfo.setUid(userC.getInt(userC.getColumnIndex("uid")));
                     userInfo.setIntroduction(userC.getString(userC.getColumnIndex("introduction")));
                     myAvatarImage = view.findViewById(R.id.my_avatar);
                     ImageView settings = view.findViewById(R.id.my_setting);
                     Button editUserInfoButton = view.findViewById(R.id.my_change_user_info);
                     editUserInfoButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             startActivity(new Intent(getActivity(),EditUserinfoActivity.class));
                         }
                     });
                     settings.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             startActivity(new Intent(getActivity(),SettingActivity.class));
                         }
                     });
                    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);
                    Glide.with(this)
                        .load(userInfo.getAvatar())
                        .apply(requestOptions)
                        .into(myAvatarImage);
                    myAvatarImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK,null);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                            startActivityForResult(intent,2);
                        }
                    });
                    myNameText = view.findViewById(R.id.my_name);
                    myIntroduction = view.findViewById(R.id.my_introduction);
                    if (!userInfo.getName().equals("defaultusername")){
                        myNameText.setText(userInfo.getName());
                        myIntroduction.setText(userInfo.getIntroduction());
                    } else {
                        myNameText.setText("默认昵称");
                        myIntroduction.setText("这个人并没有简介~");
                    }
                } else {
                    view = inflater.inflate(R.layout.fragment_my,container,false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "获取用户缓存失败,请关闭重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                    TextView myNameText = view.findViewById(R.id.my_name);
                    myNameText.setText("缓存失败,请退出重试");
                }
                return view;
            }
        } else {
            View view = inflater.inflate(R.layout.fragment_my_nologin,container,false);
            Button loginButton = view.findViewById(R.id.my_nologin_login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(),LoginActivity.class));
                }
            });
            return view;
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
                    UCrop.of(uri, Uri.fromFile(new File(getActivity().getCacheDir(),"cropcache.jpeg")))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(500, 500)
                            .start(getActivity(),3);
                    break;
                default:
                    break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("changemy");
        localBroadcastManager.registerReceiver(myBroadcastReceiver,intentFilter);
    }
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SQLiteOpenHelper helper = new AccountDatabaseHelper(getActivity(),"olcowsso",null,1);
            SQLiteDatabase sqLiteDatabase = helper.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select avatar,name,introduction from userinfo",null);
            if (cursor.moveToFirst()){
                Glide.with(getActivity())
                        .load(cursor.getString(cursor.getColumnIndex("avatar")))
                        .into(myAvatarImage);
                myNameText.setText(cursor.getString(cursor.getColumnIndex("name")));
                myIntroduction.setText(cursor.getString(cursor.getColumnIndex("introduction")));
            }
        }
    }
}

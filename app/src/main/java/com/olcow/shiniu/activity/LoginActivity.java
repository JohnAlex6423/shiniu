package com.olcow.shiniu.activity;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.R;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private ConstraintLayout con;

    private MaterialEditText usernameEditText;
    private MaterialEditText regUsernameEditText;
    private MaterialEditText regPasswordEditText;
    private MaterialEditText regEmailEditText;
    private MaterialEditText passwordEditText;

    private LinearLayout loginText;
    private LinearLayout regText;

    private View regLineBlack;
    private View loginLineBlack;

    private CheckBox loginEye;
    private CheckBox regEye;

    private Button loginButton;
    private Button regButton;

    private ProgressBar loginProgressBar;
    private ProgressBar regProgressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_login);
        con = findViewById(R.id.login_con);
        LayoutTransition transition = new LayoutTransition();
        con.setLayoutTransition(transition);
        usernameEditText = findViewById(R.id.usernameedit);
        passwordEditText = findViewById(R.id.passwordedit);
        loginProgressBar = findViewById(R.id.login_progress);
        loginText = findViewById(R.id.login_text);
        loginLineBlack = findViewById(R.id.login_line_black);
        regUsernameEditText = findViewById(R.id.reg_usernameedit);
        regPasswordEditText = findViewById(R.id.reg_passwordedit);
        regEmailEditText = findViewById(R.id.reg_emailedit);
        regText = findViewById(R.id.reg_text);
        regLineBlack = findViewById(R.id.reg_line_black);
        regProgressBar = findViewById(R.id.reg_progress);
        regButton = findViewById(R.id.reg_button);
        regEye = findViewById(R.id.reg_eyepassword);

        loginEye = findViewById(R.id.eyepassword);
        loginButton = findViewById(R.id.login_button);
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    loginEye.setVisibility(View.VISIBLE);
                } else {
                    loginEye.setVisibility(View.GONE);
                }
            }
        });
        regPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    regEye.setVisibility(View.VISIBLE);
                } else {
                    regEye.setVisibility(View.GONE);
                }
            }
        });
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameEditText.validateWith(new RegexpValidator("输入了非字符,请输入字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{0,100}$"));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        regUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                regUsernameEditText.validateWith(new RegexpValidator("输入了非字符,请输入字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{0,100}$"));
            }
        });
        usernameEditText.addValidator(new RegexpValidator("非法账号,请输入6-16位字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{6,16}$"));
        regUsernameEditText.addValidator(new RegexpValidator("非法账号,请输入6-16位字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{6,16}$"));
        passwordEditText.addValidator(new RegexpValidator("非法密码,请输入6-16位字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{6,16}$"));
        regPasswordEditText.addValidator(new RegexpValidator("非法密码,请输入6-16位字母,数字或英文字符","^[0-9a-zA-Z`~!@#$%^&*()\\-_=+|\\\\\\]}{\\[\"':;?/>.<,]{6,16}$"));
        regEmailEditText.addValidator(new RegexpValidator("非法邮箱","^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$"));
        //登陆按钮
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameEditText.validate()&passwordEditText.validate()){
                    loginButton.setVisibility(View.GONE);
                    loginProgressBar.setVisibility(View.VISIBLE);
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username",usernameEditText.getText().toString())
                            .add("password",passwordEditText.getText().toString())
                            .build();
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url("http://39.96.40.12:7703/login")
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu.okhttp", "登陆网络请求失败"+e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginProgressBar.setVisibility(View.INVISIBLE);
                                    loginButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, "登陆网路错误,请重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String res = response.body().string();
                            if (res.equals("username not exist")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginButton.setVisibility(View.VISIBLE);
                                        usernameEditText.setError("用户不存在");
                                    }
                                });
                            } else if (res.equals("password error")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginButton.setVisibility(View.VISIBLE);
                                        passwordEditText.setError("密码错误");
                                    }
                                });
                            } else if (res.equals("not logged in")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginButton.setVisibility(View.VISIBLE);
                                        Toast.makeText(LoginActivity.this, "登陆失败,网络错误，请重试", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (res.length()==32){
                                        Request request1 = new Request.Builder()
                                                .url("http://39.96.40.12:7703/islogin?session="+res)
                                                .get()
                                                .build();
                                        Call call1 = okHttpClient.newCall(request1);
                                        call1.enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Log.e("shiniu.okhttp", "onFailure: "+ e.getMessage());
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                                        loginButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(LoginActivity.this, "登陆失败,网络错误，请重试", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                final String resAccountInfo = response.body().string();
                                                if (resAccountInfo.equals("no login")||resAccountInfo.equals("redis error")){
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            loginProgressBar.setVisibility(View.INVISIBLE);
                                                            loginButton.setVisibility(View.VISIBLE);
                                                            Toast.makeText(LoginActivity.this, "服务器出错,请稍后重试", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }else {
                                                    JSONObject jsonObject = JSON.parseObject(resAccountInfo);
                                                    SQLiteOpenHelper sqLiteOpenHelper = new AccountDatabaseHelper(LoginActivity.this,"olcowsso",null,1);
                                                    SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
                                                    try {
                                                        sqLiteDatabase.execSQL("delete from account");
                                                        sqLiteDatabase.execSQL("insert into account(session,uid,username,email,permission) values('"+res+"',"+jsonObject.getInteger("uid")+",'"+jsonObject.getString("username")+"','"+jsonObject.getString("email")+"',"+jsonObject.getInteger("permission")+")");
                                                    } catch (Exception e){
                                                        Log.e("shiniu.sqlite", "onClick: 插入数据库失败 error message:" + e.getMessage());
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                loginProgressBar.setVisibility(View.INVISIBLE);
                                                                loginButton.setVisibility(View.VISIBLE);
                                                                Toast.makeText(LoginActivity.this, "登陆失败,缓存错误,请重试", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        return;
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    finish();
                                                }
                                            }
                                        });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loginProgressBar.setVisibility(View.INVISIBLE);
                                        loginButton.setVisibility(View.VISIBLE);
                                        Toast.makeText(LoginActivity.this, "登陆失败,网络错误，请重试", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(loginButton,"translationX",0f,10f,0f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(loginButton,"translationX",0f,-10f,0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).after(animatorY);
                    animatorSet.setDuration(50);
                    animatorSet.start();
                }
            }
        });
        //注册按钮
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regUsernameEditText.validate()&regPasswordEditText.validate()&regEmailEditText.validate()){
                    regButton.setVisibility(View.GONE);
                    regProgressBar.setVisibility(View.VISIBLE);
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username",regUsernameEditText.getText().toString())
                            .add("password",regPasswordEditText.getText().toString())
                            .add("email",regEmailEditText.getText().toString())
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://39.96.40.12:7703/reg")
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu.okhttp", "注册网络请求失败"+e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    regProgressBar.setVisibility(View.INVISIBLE);
                                    regButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(LoginActivity.this, "注册网路错误,请重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            switch (res) {
                                case "successful":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginActivity.this, "注册成功,正在自动登陆", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("username",regUsernameEditText.getText().toString())
                                            .add("password",regPasswordEditText.getText().toString())
                                            .build();
                                    final OkHttpClient okHttpClient = new OkHttpClient();
                                    final Request request = new Request.Builder()
                                            .url("http://39.96.40.12:7703/login")
                                            .post(requestBody)
                                            .build();
                                    Call call1 = okHttpClient.newCall(request);
                                    call1.enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.e("shiniu.okhttp", "登陆网络请求失败"+e.getMessage());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    regProgressBar.setVisibility(View.INVISIBLE);
                                                    regButton.setVisibility(View.VISIBLE);
                                                    Toast.makeText(LoginActivity.this, "自动登陆失败,网络异常,请手动登陆", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            final String res = response.body().string();
                                            if (res.equals("username not exist")){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        regProgressBar.setVisibility(View.INVISIBLE);
                                                        regButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(LoginActivity.this, "自动登陆失败,请手动登陆", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else if (res.equals("password error")){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        regProgressBar.setVisibility(View.INVISIBLE);
                                                        regButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(LoginActivity.this, "自动登陆失败,请手动登陆", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else if (res.equals("not logged in")){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        regProgressBar.setVisibility(View.INVISIBLE);
                                                        regButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(LoginActivity.this, "自动登陆失败,请手动登陆", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else if (res.length()==32){
                                                Request request1 = new Request.Builder()
                                                        .url("http://39.96.40.12:7703/islogin?session="+res)
                                                        .get()
                                                        .build();
                                                Call call1 = okHttpClient.newCall(request1);
                                                call1.enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        Log.e("shiniu.okhttp", "onFailure: "+ e.getMessage());
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                regProgressBar.setVisibility(View.INVISIBLE);
                                                                regButton.setVisibility(View.VISIBLE);
                                                                Toast.makeText(LoginActivity.this, "自动登陆失败,请手动登陆", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String resAccountInfo = response.body().string();
                                                        if (resAccountInfo.equals("no login")||resAccountInfo.equals("redis error")){
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    regProgressBar.setVisibility(View.INVISIBLE);
                                                                    regButton.setVisibility(View.VISIBLE);
                                                                    Toast.makeText(LoginActivity.this, "服务器出错,请稍后重试", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else {
                                                            JSONObject jsonObject = JSON.parseObject(resAccountInfo);
                                                            SQLiteOpenHelper sqLiteOpenHelper = new AccountDatabaseHelper(LoginActivity.this,"olcowsso",null,1);
                                                            SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
                                                            try {
                                                                sqLiteDatabase.execSQL("delete from account");
                                                                sqLiteDatabase.execSQL("insert into account(session,uid,username,email,permission) values('"+res+"',"+jsonObject.getInteger("uid")+",'"+jsonObject.getString("username")+"','"+jsonObject.getString("email")+"',"+jsonObject.getInteger("permission")+")");
                                                            } catch (Exception e){
                                                                Log.e("shiniu.sqlite", "onClick: 插入数据库失败 error message:" + e.getMessage());
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        regProgressBar.setVisibility(View.INVISIBLE);
                                                                        regButton.setVisibility(View.VISIBLE);
                                                                        Toast.makeText(LoginActivity.this, "自动登陆失败,缓存错误,请手动登陆", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                                return;
                                                            }
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(LoginActivity.this, "自动登陆成功", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                            finish();
                                                        }
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        regProgressBar.setVisibility(View.INVISIBLE);
                                                        regButton.setVisibility(View.VISIBLE);
                                                        Toast.makeText(LoginActivity.this, "自动登陆失败,网络错误，请手动登陆", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    break;
                                case "username is exist":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            regProgressBar.setVisibility(View.INVISIBLE);
                                            regButton.setVisibility(View.VISIBLE);
                                            regUsernameEditText.setError("用户名已被注册");
                                        }
                                    });
                                    break;
                                case "email is exist":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            regProgressBar.setVisibility(View.INVISIBLE);
                                            regButton.setVisibility(View.VISIBLE);
                                            regEmailEditText.setError("邮箱已被注册");
                                        }
                                    });
                                    break;
                                default:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    regProgressBar.setVisibility(View.INVISIBLE);
                                                    regButton.setVisibility(View.VISIBLE);
                                                    Toast.makeText(LoginActivity.this, "注册失败，网络错误", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                    break;
                            }
                        }
                    });
                }else {
                    ObjectAnimator animatorX = ObjectAnimator.ofFloat(regButton,"translationX",0f,10f,0f);
                    ObjectAnimator animatorY = ObjectAnimator.ofFloat(regButton,"translationX",0f,-10f,0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(animatorX).after(animatorY);
                    animatorSet.setDuration(50);
                    animatorSet.start();
                }
            }
        });
        loginEye.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        regEye.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    regPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    regPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        regText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameEditText.setVisibility(View.GONE);
                passwordEditText.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
                loginLineBlack.setVisibility(View.GONE);
                regText.setVisibility(View.GONE);
                loginProgressBar.setVisibility(View.INVISIBLE);
                loginText.setVisibility(View.VISIBLE);
                regUsernameEditText.setVisibility(View.VISIBLE);
                regPasswordEditText.setVisibility(View.VISIBLE);
                regEmailEditText.setVisibility(View.VISIBLE);
                regLineBlack.setVisibility(View.VISIBLE);
                regLineBlack.setVisibility(View.VISIBLE);
                regButton.setVisibility(View.VISIBLE);
            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginText.setVisibility(View.GONE);
                regUsernameEditText.setVisibility(View.GONE);
                regPasswordEditText.setVisibility(View.GONE);
                regEmailEditText.setVisibility(View.GONE);
                regLineBlack.setVisibility(View.GONE);
                regLineBlack.setVisibility(View.GONE);
                regButton.setVisibility(View.GONE);
                regProgressBar.setVisibility(View.INVISIBLE);
                usernameEditText.setVisibility(View.VISIBLE);
                passwordEditText.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                loginLineBlack.setVisibility(View.VISIBLE);
                regText.setVisibility(View.VISIBLE);
            }
        });
    }
}

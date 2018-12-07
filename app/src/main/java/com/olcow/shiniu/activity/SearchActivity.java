package com.olcow.shiniu.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.SearchResultAdapt;
import com.olcow.shiniu.entity.UserInfo;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;

    private MaterialEditText searchEdit;
    private TextView searchClear;
    private RecyclerView resultList;
    private TextView searchNoneText;

    private ImageView searchEditRef;

    private List<UserInfo> userInfoList;

    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEdit = findViewById(R.id.search_edit);
        searchClear = findViewById(R.id.search_clear);
        searchEditRef = findViewById(R.id.search_edit_ref);
        resultList = findViewById(R.id.search_recyclerView);
        searchNoneText = findViewById(R.id.search_result_none);
        resultList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        userInfoList = new ArrayList<>();
        adapter = new SearchResultAdapt(userInfoList);
        resultList.setAdapter(adapter);
        resultList.setItemAnimator(new DefaultItemAnimator());
        resultList.addItemDecoration(new DividerItemDecoration(SearchActivity.this,DividerItemDecoration.VERTICAL));
        okHttpClient = new OkHttpClient();

        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId==EditorInfo.IME_ACTION_SEARCH){
                    Toast.makeText(SearchActivity.this, "点击了搜索", Toast.LENGTH_SHORT).show();
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUid(7538);
                    userInfo.setAvatar("http://123.206.93.200/uploadavatar/6304ee3e60e643658345edc413b6c99f.jpeg");
                    userInfo.setName("完全自杀手册");
                    userInfoList.add(userInfo);
                    adapter.notifyDataSetChanged();
                    return true;
                }else {
                    return false;
                }
            }
        });

        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>0){
                    searchEditRef.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.VISIBLE);
                    searchNoneText.setVisibility(View.GONE);
                    for (Call call:okHttpClient.dispatcher().queuedCalls()){
                        Log.e("shiniu.okhttp", "关闭了连接");
                        call.cancel();
                    }
                    final Request request = new Request.Builder()
                            .url("http://39.96.40.12:1008/searchuserinfo/bynicknameoruid?content="+s.toString())
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu.okhttp", "请求失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            JSONObject jsonObject = JSON.parseObject(res);
                            userInfoList = JSON.parseArray(jsonObject.getString("nickname"),UserInfo.class);
                            if (userInfoList.isEmpty()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        searchNoneText.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    searchEditRef.setVisibility(View.INVISIBLE);
                    searchEditRef.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchEditRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");
            }
        });
    }
}

package com.olcow.shiniu.activity;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

public class SearchDetailsActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;

    private MaterialEditText searchDetailsEdit;
    private ImageView searchDetailsClear;
    private RecyclerView detailsList;
    private TextView searchNoneText;
    private TextView searchNoInternetText;
    private TextView searchDetailsButton;
    private ProgressBar searchDetailsProgressBar;
    private ImageView searchEditRef;

    private ConstraintLayout searchDetailsByName;
    private ConstraintLayout searchDetailsByUid;
    private ImageView searchDetailsByUidAvatar;
    private TextView searchDetailsByUidName;
    private TextView searchDetailsByUidUid;

    private String searchContent;

    private List<UserInfo> userInfoList;
    private UserInfo userInfo;

    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_details);
        searchDetailsEdit = findViewById(R.id.search_details_edit);
        searchEditRef = findViewById(R.id.search_details_edit_ref);
        searchDetailsClear = findViewById(R.id.search_details_back_img);
        detailsList = findViewById(R.id.search_details_recyclerView);
        searchEditRef = findViewById(R.id.search_details_edit_ref);
        searchDetailsByName = findViewById(R.id.search_details_byname);
        searchDetailsByUid = findViewById(R.id.search_details_byuid);
        searchDetailsByUidAvatar = findViewById(R.id.search_details_byuid_avatar);
        searchDetailsByUidUid = findViewById(R.id.search_details_byuid_uid);
        searchDetailsByUidName = findViewById(R.id.search_details_byuid_name);
        searchDetailsButton = findViewById(R.id.search_details_search);
        detailsList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        userInfoList = new ArrayList<>();
        adapter = new SearchResultAdapt(userInfoList);
        detailsList.setAdapter(adapter);
        detailsList.setItemAnimator(new DefaultItemAnimator());
        detailsList.addItemDecoration(new DividerItemDecoration(SearchDetailsActivity.this,DividerItemDecoration.VERTICAL));
        searchNoneText = findViewById(R.id.search_details_none);
        searchNoInternetText = findViewById(R.id.search_details_nointernet);
        searchDetailsProgressBar = findViewById(R.id.search_details_progressbar);
        okHttpClient = new OkHttpClient();
        searchContent = getIntent().getStringExtra("searchContent");
        searchDetailsEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId==EditorInfo.IME_ACTION_SEARCH){
                    if (!searchDetailsEdit.getText().toString().equals("")){
                        search(searchDetailsEdit.getText().toString());
                    } else {
                        Toast.makeText(SearchDetailsActivity.this, "搜索不能为空!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }else {
                    return false;
                }
            }
        });
        searchDetailsByUid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfo==null){
                    Toast.makeText(SearchDetailsActivity.this, "环境异常,请退出重试!", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(SearchDetailsActivity.this,UserinfoActivity.class);
                    intent.putExtra("userinfo",userInfo);
                    startActivity(intent);
                }
            }
        });
        searchDetailsClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (searchContent == null){
            searchNoneText.setVisibility(View.VISIBLE);
        } else {
            searchDetailsEdit.setText(searchContent);
            search(searchContent);
        }
        searchDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchDetailsEdit.getText().toString().equals("")){
                    Toast.makeText(SearchDetailsActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    Log.e("shiniu", searchDetailsEdit.getText().toString());
                    search(searchDetailsEdit.getText().toString());
                }
            }
        });
        searchEditRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDetailsEdit.setText("");
            }
        });
        searchDetailsEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>0){
                    searchEditRef.setVisibility(View.VISIBLE);
                }else {
                    searchEditRef.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void search(String content){
        searchDetailsProgressBar.setVisibility(View.VISIBLE);
        searchDetailsByUid.setVisibility(View.GONE);
        searchDetailsByName.setVisibility(View.GONE);
        searchNoneText.setVisibility(View.GONE);
        searchNoInternetText.setVisibility(View.GONE);
        okHttpClient.newCall(new Request.Builder()
                .url("http://39.96.40.12:1008/searchpreinfo/bynicknameoruid?content="+content)
                .get()
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchDetailsProgressBar.setVisibility(View.GONE);
                        searchNoInternetText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                JSONObject jsonObject = JSON.parseObject(res);
                userInfoList.clear();
                userInfoList.addAll(JSON.parseArray(jsonObject.getString("nickname"),UserInfo.class));
                Log.e("shiniu", String.valueOf(jsonObject.get("uid")!=null));
                if (jsonObject.get("uid")!=null){
                    userInfo = jsonObject.getObject("uid",UserInfo.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchDetailsProgressBar.setVisibility(View.GONE);
                            searchDetailsByUid.setVisibility(View.VISIBLE);
                            Glide.with(SearchDetailsActivity.this)
                                    .load(userInfo.getAvatar())
                                    .apply(RequestOptions.placeholderOf(R.drawable.olcowlog_ye_touxiang))
                                    .into(searchDetailsByUidAvatar);
                            searchDetailsByUidUid.setText("Nid:"+userInfo.getUid());
                            searchDetailsByUidName.setText(userInfo.getName());
                        }
                    });
                }
                if (!userInfoList.isEmpty()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchDetailsProgressBar.setVisibility(View.GONE);
                            searchDetailsByName.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                if (userInfoList.isEmpty()&&jsonObject.get("uid")==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchDetailsByName.setVisibility(View.GONE);
                            searchNoneText.setVisibility(View.VISIBLE);
                            searchDetailsProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }
}

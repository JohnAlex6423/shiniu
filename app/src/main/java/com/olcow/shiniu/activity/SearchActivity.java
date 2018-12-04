package com.olcow.shiniu.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.olcow.shiniu.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;

    private EditText searchEdit;
    private TextView searchClear;

    private ImageView searchEditRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEdit = findViewById(R.id.search_edit);
        searchClear = findViewById(R.id.search_clear);
        searchEditRef = findViewById(R.id.search_edit_ref);
        okHttpClient = new OkHttpClient();
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId==EditorInfo.IME_ACTION_SEARCH){
                    Toast.makeText(SearchActivity.this, "点击了搜索", Toast.LENGTH_SHORT).show();
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
                    for (Call call:okHttpClient.dispatcher().queuedCalls()){
                        Log.e("shiniu.okhttp", "关闭了连接");
                        call.cancel();
                    }
                    Request request = new Request.Builder()
                            .url("http://baidu.com")
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("shiniu.okhttp", "请求失败");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.e("shiniu.okhttp",response.body().string());
                        }
                    });
                } else {
                    searchEditRef.setVisibility(View.INVISIBLE);
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

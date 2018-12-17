package com.olcow.shiniu.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.ChatAdapter;
import com.olcow.shiniu.entity.Message;
import com.olcow.shiniu.until.Softkeyboardlistener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText sendEdit;
    private Button sendButton;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Message> messages;
    private String sendAvatar;
    private String recipientAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("完全自杀手册");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sendEdit = findViewById(R.id.chat_send_edit);
        sendButton = findViewById(R.id.chat_send_button);
        recyclerView = findViewById(R.id.chat_rec);
        messages = new ArrayList<>();
        messages.add(new Message("我是接受者哦1","12-17 周一 13:14",103,0,0));
        messages.add(new Message("我是发送者哦2","12-17 周一 13:14",103,1,1));
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter = new ChatAdapter(messages,"http://123.206.93.200/images/olcowlog_avatar.png","http://123.206.93.200/images/olcowlog_avatar.png");
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messages.size()-1);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(sendEdit.getText().toString(),"12-17 周一 13:14",103,1,1));
                sendEdit.setText("");
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        });
        Softkeyboardlistener.setListener(this, new Softkeyboardlistener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerView.scrollToPosition(messages.size()-1);
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy<0){
                    Log.e("shiniu", "onScrolled: ");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:////主键id 必须这样写
                onBackPressed();//按返回图标直接回退上个界面
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

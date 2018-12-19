package com.olcow.shiniu.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.olcow.shiniu.R;
import com.olcow.shiniu.adapter.ChatAdapter;
import com.olcow.shiniu.entity.Message;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.ChatDatabaseHelper;
import com.olcow.shiniu.until.Softkeyboardlistener;
import com.olcow.shiniu.until.TimeType;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText sendEdit;
    private Button sendButton;
    private Button sendButtonNone;
    private ConstraintLayout sendCon;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Message> messages;
    private UserInfo recipientUserInfo;
    private UserInfo sendUserInfo;
    private TextView errorText;
    private SQLiteDatabase sqlMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recipientUserInfo = (UserInfo) getIntent().getSerializableExtra("recipientuserinfo");
        sendUserInfo = (UserInfo) getIntent().getSerializableExtra("senduserinfo");
        sendEdit = findViewById(R.id.chat_send_edit);
        sendButton = findViewById(R.id.chat_send_button);
        sendButtonNone = findViewById(R.id.chat_send_button_none);
        recyclerView = findViewById(R.id.chat_rec);
        sendCon = findViewById(R.id.chat_send_con);
        errorText = findViewById(R.id.chat_error);
        Log.e("shiniu", String.valueOf(recipientUserInfo==null)+"send"+String.valueOf(sendUserInfo==null));
        if (recipientUserInfo == null||sendUserInfo == null){
            sendCon.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        setTitle(recipientUserInfo.getName());
        messages = new ArrayList<>();
        if (sqlMessage==null){
            sqlMessage = new ChatDatabaseHelper(ChatActivity.this,"chatmessage",null,1).getReadableDatabase();
        }
        Cursor c = sqlMessage.rawQuery("select *from message where uid=1023 order by date desc limit 0,20",null);
        if (c.moveToLast()){
            long cacheTime;
            cacheTime = c.getLong(c.getColumnIndex("date"));
            messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
            while (c.moveToPrevious()){
                long cacheTimeNext = c.getLong(c.getColumnIndex("date"));
                if (cacheTime - cacheTimeNext>600000){
                    cacheTime = cacheTimeNext;
                    messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.ISSHOWTIME));
                } else {
                    cacheTime = cacheTimeNext;
                    messages.add(new Message(c.getString(c.getColumnIndex("content")),TimeType.getMessageTimeText(cacheTime),c.getInt(c.getColumnIndex("recipientorsend")),Message.NOSHOWTIME));
                }
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter = new ChatAdapter(messages,sendUserInfo,recipientUserInfo);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messages.size()-1);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(sendEdit.getText().toString(),"12-17 周一 13:14",1,1));
                sendEdit.setText("");
                adapter.notifyItemInserted(messages.size()-1);
                recyclerView.scrollToPosition(messages.size()-1);
            }
        });
        sendEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()<1){
                    sendButton.setVisibility(View.INVISIBLE);
                    sendButtonNone.setVisibility(View.VISIBLE);
                }else {
                    sendButtonNone.setVisibility(View.GONE);
                    sendButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    @Override
    protected void onRestart() {
        super.onRestart();
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_f49e38));
    }
}

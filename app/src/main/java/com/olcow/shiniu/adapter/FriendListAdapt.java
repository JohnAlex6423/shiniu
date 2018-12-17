package com.olcow.shiniu.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.EditUserinfoActivity;
import com.olcow.shiniu.entity.UserInfo;
import com.olcow.shiniu.sqlite.AccountDatabaseHelper;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FriendListAdapt extends RecyclerView.Adapter<FriendListAdapt.ViewHolder> {

    private List<UserInfo> userInfoList;
    private RequestOptions requestOptions;
    private OkHttpClient okHttpClient;
    private SQLiteDatabase sqLiteDatabase;
    private String session;


    public FriendListAdapt(List<UserInfo> userInfoList){
        this.userInfoList = userInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FriendListAdapt.ViewHolder(LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.recy_friends,viewGroup,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.nameText.setText(userInfoList.get(i).getName());
        viewHolder.introductionText.setText(userInfoList.get(i).getIntroduction());
        if (requestOptions == null){
            requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);
        }
        Glide.with(viewHolder.itemView.getContext())
                .load(userInfoList.get(i).getAvatar())
                .apply(requestOptions)
                .into(viewHolder.avatarImg);
        viewHolder.friendcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        viewHolder.moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setItems(new String[]{"取关"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    Intent intent = new Intent();
                                    intent.setAction("delfriend");
                                    intent.putExtra("userinfoid",userInfoList.get(i).getUid());
                                    LocalBroadcastManager.getInstance(
                                            viewHolder.itemView.getContext())
                                            .sendBroadcast(intent);
                                }
                            }
                        }).create();
                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView introductionText;
        ImageView avatarImg;
        ImageView moreImg;
        ConstraintLayout friendcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.recy_friends_name);
            introductionText = itemView.findViewById(R.id.recy_friends_introduction);
            avatarImg = itemView.findViewById(R.id.recy_friends_avatar);
            moreImg = itemView.findViewById(R.id.recy_friends_more);
            friendcon = itemView.findViewById(R.id.recy_friends);

        }
    }
}

package com.olcow.shiniu.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import com.olcow.shiniu.activity.ChatActivity;
import com.olcow.shiniu.entity.UserInfo;

import java.util.List;

public class FriendListAdapt extends RecyclerView.Adapter<FriendListAdapt.ViewHolder> {

    private List<UserInfo> userInfoList;
    private RequestOptions requestOptions;
    private UserInfo sendUserInfo;


    public FriendListAdapt(List<UserInfo> userInfoList,UserInfo sendUserInfo){
        this.userInfoList = userInfoList;
        this.sendUserInfo = sendUserInfo;
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
        viewHolder.friendCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendUserInfo!=null){
                    Intent intent = new Intent(viewHolder.itemView.getContext(),ChatActivity.class);
                    intent.putExtra("senduserinfo",sendUserInfo);
                    intent.putExtra("recipientuserinfo",userInfoList.get(i));
                    viewHolder.itemView.getContext().startActivity(intent);
                }else {
                    Toast.makeText(viewHolder.itemView.getContext(), "当前环境异常，请退出重试", Toast.LENGTH_SHORT).show();
                }
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
        ConstraintLayout friendCon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.recy_friends_name);
            introductionText = itemView.findViewById(R.id.recy_friends_introduction);
            avatarImg = itemView.findViewById(R.id.recy_friends_avatar);
            moreImg = itemView.findViewById(R.id.recy_friends_more);
            friendCon = itemView.findViewById(R.id.recy_friends);
        }
    }
}

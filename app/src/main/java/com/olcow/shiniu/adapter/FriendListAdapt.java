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
import android.widget.AdapterView;
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
    private OnItemAreYouSure onItemAreYouSure = null;


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
                        .setItems(new String[]{"删除聊天记录","取关"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        AlertDialog areYouSure = new AlertDialog.Builder(viewHolder.itemView.getContext())
                                                .setTitle("你确定要删除与此人的聊天记录吗？")
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (onItemAreYouSure!=null){
                                                            onItemAreYouSure.delMessageForUserId(userInfoList.get(i).getUid());
                                                        }else {
                                                            Toast.makeText(viewHolder.itemView.getContext(), "系统错误，请退出重试", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }).create();
                                        areYouSure.show();
                                }
                                if (which==1){
                                    AlertDialog areYouSure = new AlertDialog.Builder(viewHolder.itemView.getContext())
                                            .setTitle("你确定要取关吗？")
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .setPositiveButton("取关", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (onItemAreYouSure!=null){
                                                        onItemAreYouSure.onClanUnsubscribe(userInfoList.get(i).getUid());
                                                    }else {
                                                        Toast.makeText(viewHolder.itemView.getContext(), "系统错误，请退出重试", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }).create();
                                    areYouSure.show();
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

    public void addAreYouSureClickListener(OnItemAreYouSure onItemAreYouSure){
        this.onItemAreYouSure = onItemAreYouSure;
    }

    public interface OnItemAreYouSure{
        void delMessageForUserId(int userId);
        void onClanUnsubscribe(int userId);
    }
}

package com.olcow.shiniu.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.UserInfo;

import java.util.List;

public class FriendListAdapt extends RecyclerView.Adapter<FriendListAdapt.ViewHolder> {

    private List<UserInfo> userInfoList;
    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);


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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.nameText.setText(userInfoList.get(i).getName());
        viewHolder.introductionText.setText(userInfoList.get(i).getName());
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
            public void onClick(View v) {

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

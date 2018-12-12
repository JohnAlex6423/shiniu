package com.olcow.shiniu.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import com.olcow.shiniu.activity.UserinfoActivity;
import com.olcow.shiniu.entity.UserInfo;

import java.util.List;

public class SearchResultAdapt extends RecyclerView.Adapter<SearchResultAdapt.ViewHolder> {

    private List<UserInfo> userInfoList;
    private RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.olcowlog_ye_touxiang);

    public SearchResultAdapt(List<UserInfo> userInfoList){
        this.userInfoList = userInfoList;
    }

    @NonNull
    @Override
    public SearchResultAdapt.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        SearchResultAdapt.ViewHolder holder = new SearchResultAdapt.ViewHolder(LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.recy_search_result,viewGroup,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchResultAdapt.ViewHolder viewHolder, final int i) {
        viewHolder.nameText.setText(userInfoList.get(i).getName());
        viewHolder.uidText.setText("Nid:"+userInfoList.get(i).getUid());
        Glide.with(viewHolder.itemView.getContext())
                .load(userInfoList.get(i).getAvatar())
                .apply(requestOptions)
                .into(viewHolder.avatarImg);
        viewHolder.resultCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(viewHolder.itemView.getContext(),UserinfoActivity.class);
                intent.putExtra("userinfo",userInfoList.get(i));
                viewHolder.itemView.getContext().startActivity(intent);
            }
        });
        viewHolder.resultCon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(viewHolder.itemView.getContext(), "长按了,兄弟", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView uidText;
        ImageView avatarImg;
        TextView nameText;
        ConstraintLayout resultCon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            uidText = itemView.findViewById(R.id.search_result_uid);
            nameText = itemView.findViewById(R.id.search_result_name);
            avatarImg = itemView.findViewById(R.id.search_result_avatar);
            resultCon = itemView.findViewById(R.id.search_result_con);
        }
    }
}

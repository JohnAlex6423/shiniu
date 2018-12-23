package com.olcow.shiniu.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.ChatActivity;
import com.olcow.shiniu.entity.NowMessage;
import com.olcow.shiniu.entity.UserInfo;

import java.util.List;

public class NowMessageListAdapter extends RecyclerView.Adapter<NowMessageListAdapter.ViewHolder> {

    private List<NowMessage> nowMessages;
    private List<UserInfo> reUserInfoList;
    private OnItemAreYouSureClickListener onItemAreYouSureClickListener = null;

    public NowMessageListAdapter(List<NowMessage> nowMessages, List<UserInfo> reUserInfoList) {
        this.nowMessages = nowMessages;
        this.reUserInfoList = reUserInfoList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(
                viewGroup.getContext()).inflate(R.layout.recy_nowmessage,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        Glide.with(viewHolder.itemView.getContext())
                .load(nowMessages.get(i).getAvatar())
                .into(viewHolder.avatarImg);
        viewHolder.nameText.setText(nowMessages.get(i).getName());
        viewHolder.timeText.setText(nowMessages.get(i).getTime());
        viewHolder.contentText.setText(nowMessages.get(i).getContent());
        viewHolder.redBadgeText.setText(String.valueOf(nowMessages.get(i).getRedbadge()));
        if (nowMessages.get(i).getRedbadge()>0){
            viewHolder.redBadgeText.setText(String.valueOf(nowMessages.get(i).getRedbadge()));
            viewHolder.redBadgeText.setVisibility(View.VISIBLE);
        }
        viewHolder.nowMessageCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.getContext().startActivity(
                        new Intent(viewHolder.itemView.getContext(),ChatActivity.class)
                        .putExtra("recipientuserinfo",reUserInfoList.get(i))
                );
            }
        });
        viewHolder.nowMessageCon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setItems(new String[]{"从消息中删除","删除与此人的聊天记录"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    onItemAreYouSureClickListener.onDelNowMessage(reUserInfoList.get(i).getUid());
                                }else if (which==1){
                                    AlertDialog areyousure = new AlertDialog.Builder(viewHolder.itemView.getContext())
                                            .setTitle("你确定删除与此人的聊天记录？")
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (onItemAreYouSureClickListener!=null){
                                                        onItemAreYouSureClickListener.onSureClick(reUserInfoList.get(i).getUid());
                                                    }
                                                }
                                            }).create();
                                    areyousure.show();
                                }
                            }
                        }).create();
                alertDialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return nowMessages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView contentText;
        TextView timeText;
        ImageView avatarImg;
        TextView redBadgeText;
        ConstraintLayout nowMessageCon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.recy_nowmessage_name);
            contentText = itemView.findViewById(R.id.recy_nowmessage_content);
            timeText = itemView.findViewById(R.id.recy_nowmessage_time);
            avatarImg = itemView.findViewById(R.id.recy_nowmessage_avatar);
            nowMessageCon = itemView.findViewById(R.id.recy_nowmessage_con);
            redBadgeText = itemView.findViewById(R.id.recy_nowmessage_redbadge);
        }
    }

    public void addAreYouSureClickListener(OnItemAreYouSureClickListener onItemAreYouSureClickListener){
        this.onItemAreYouSureClickListener = onItemAreYouSureClickListener;
    }

    public interface OnItemAreYouSureClickListener{
        void onSureClick(int userId);
        void onDelNowMessage(int userId);
    }
}
package com.olcow.shiniu.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.olcow.shiniu.R;
import com.olcow.shiniu.activity.UserinfoActivity;
import com.olcow.shiniu.entity.Message;
import com.olcow.shiniu.entity.UserInfo;

import java.util.List;

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private List<Message> messages;
    private UserInfo sendUserInfo;
    private UserInfo recipientUserInfo;

    public ChatAdapter(List<Message> messages, UserInfo sendUserInfo, UserInfo recipientUserInfo) {
        this.messages = messages;
        this.sendUserInfo = sendUserInfo;
        this.recipientUserInfo = recipientUserInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.recy_chat,viewGroup,false));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        if (messages.get(i).getSendOrRecipient()==Message.RECIPIENT){
            if (messages.get(i).getShowTime()==0){
                viewHolder.timeText.setVisibility(View.VISIBLE);
                viewHolder.timeText.setText(messages.get(i).getTime());
            }
            Glide.with(viewHolder.itemView.getContext())
                    .load(recipientUserInfo.getAvatar())
                    .into(viewHolder.recipientAvatar);
            viewHolder.recipientText.setText(messages.get(i).getContent());
            viewHolder.recipientCon.setVisibility(View.VISIBLE);
            viewHolder.recipientAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(viewHolder.itemView.getContext(),UserinfoActivity.class);
                    intent.putExtra("userinfo",recipientUserInfo);
                    viewHolder.itemView.getContext().startActivity(intent);
                }
            });
        }
        if (messages.get(i).getSendOrRecipient()==Message.SEND){
            if (messages.get(i).getShowTime()==0){
                viewHolder.timeText.setVisibility(View.VISIBLE);
                viewHolder.timeText.setText(messages.get(i).getTime());
            }
            Glide.with(viewHolder.itemView.getContext())
                    .load(sendUserInfo.getAvatar())
                    .into(viewHolder.sendAvatar);
            viewHolder.sendText.setText(messages.get(i).getContent());
            viewHolder.sendCon.setVisibility(View.VISIBLE);
            viewHolder.sendAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(viewHolder.itemView.getContext(),UserinfoActivity.class);
                    intent.putExtra("userinfo",sendUserInfo);
                    viewHolder.itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()){
            onBindViewHolder(holder,position);
        }else {
            onBindViewHolder(holder,position);
            holder.sendErrorRedBadge.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView sendText;
        TextView recipientText;
        TextView timeText;
        TextView sendErrorRedBadge;
        LinearLayout recipientCon;
        LinearLayout sendCon;
        ImageView sendAvatar;
        ImageView recipientAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sendText = itemView.findViewById(R.id.chat_send_text);
            recipientText = itemView.findViewById(R.id.chat_recipient_text);
            sendErrorRedBadge = itemView.findViewById(R.id.chat_rec_message_send_fail);
            timeText = itemView.findViewById(R.id.chat_time);
            recipientCon = itemView.findViewById(R.id.chat_recipient_con);
            sendCon = itemView.findViewById(R.id.chat_sender_con);
            sendAvatar = itemView.findViewById(R.id.chat_sender_avatar);
            recipientAvatar = itemView.findViewById(R.id.chat_recipient_avatar);
        }
    }
}

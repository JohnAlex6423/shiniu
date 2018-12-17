package com.olcow.shiniu.adapter;

import android.icu.util.Calendar;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.olcow.shiniu.R;
import com.olcow.shiniu.entity.Message;

import org.w3c.dom.Text;

import java.util.List;

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    List<Message> messages;
    String sendAvatarUrl;
    String recipientAvatarUrl;

    public ChatAdapter(List<Message> messages,String sendAvatarUrl,String recipientAvatarUrl){
        this.messages = messages;
        this.sendAvatarUrl = sendAvatarUrl;
        this.recipientAvatarUrl = recipientAvatarUrl;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (messages.get(i).getSendOrRecipient()==Message.RECIPIENT){
            if (messages.get(i).getShowTime()==0){
                viewHolder.timeText.setVisibility(View.VISIBLE);
                viewHolder.timeText.setText(messages.get(i).getTime());
            }
            Glide.with(viewHolder.itemView.getContext())
                    .load(recipientAvatarUrl)
                    .into(viewHolder.recipientAvatar);
            viewHolder.recipientText.setText(messages.get(i).getContent());
            viewHolder.recipientCon.setVisibility(View.VISIBLE);
        }
        if (messages.get(i).getSendOrRecipient()==Message.SEND){
            if (messages.get(i).getShowTime()==0){
                viewHolder.timeText.setVisibility(View.VISIBLE);
                viewHolder.timeText.setText(messages.get(i).getTime());
            }
            Glide.with(viewHolder.itemView.getContext())
                    .load(sendAvatarUrl)
                    .into(viewHolder.sendAvatar);
            viewHolder.sendText.setText(messages.get(i).getContent());
            viewHolder.sendCon.setVisibility(View.VISIBLE);
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
        LinearLayout recipientCon;
        LinearLayout sendCon;
        ImageView sendAvatar;
        ImageView recipientAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sendText = itemView.findViewById(R.id.chat_send_text);
            recipientText = itemView.findViewById(R.id.chat_recipient_text);
            timeText = itemView.findViewById(R.id.chat_time);
            recipientCon = itemView.findViewById(R.id.chat_recipient_con);
            sendCon = itemView.findViewById(R.id.chat_sender_con);
            sendAvatar = itemView.findViewById(R.id.chat_sender_avatar);
            recipientAvatar = itemView.findViewById(R.id.chat_recipient_avatar);
        }
    }
}

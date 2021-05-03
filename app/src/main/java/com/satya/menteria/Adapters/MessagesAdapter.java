package com.satya.menteria.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.satya.menteria.Model.Message;
import com.satya.menteria.databinding.ItemReceiveBinding;
import com.satya.menteria.databinding.ItemSendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.satya.menteria.R.*;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SEND = 1;
    final int ITEM_RECEIVE = 2;
    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom){
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==ITEM_SEND)
        {
            View view = LayoutInflater.from(context).inflate(layout.item_send, parent, false);
            return new SendViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(layout.item_receive, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId()))
        {
            return ITEM_SEND;
        }
        else return ITEM_RECEIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if(holder.getClass() == SendViewHolder.class)
        {
            SendViewHolder viewHolder = (SendViewHolder) holder;
            if(message.getImageUrl()!=null)
            {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Log.v("Image", message.getImageUrl());
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(drawable.placeholderimage)
                        .into(viewHolder.binding.image);
            }
            else viewHolder.binding.message.setText(message.getMessage());

            viewHolder.binding.messageTime.setText(new SimpleDateFormat("HH:mm").format(message.getTimestamp()));


        }else{
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
            if(message.getImageUrl()!=null)
            {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(drawable.placeholderimage)
                        .into(viewHolder.binding.image);
            }
            else viewHolder.binding.message.setText(message.getMessage());
            viewHolder.binding.messageTime.setText(new SimpleDateFormat("HH:mm").format(message.getTimestamp()));

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder{
        ItemSendBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }




}

package com.satya.menteria.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.satya.menteria.Activites.ChatActivity;
import com.satya.menteria.Model.User;
import com.satya.menteria.R;
import com.satya.menteria.databinding.ItemMenteeBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MenteeListAdapter extends RecyclerView.Adapter<MenteeListAdapter.viewHolder>{

    private ArrayList<Pair<User,String>> menteeList;
    Context context;

    public MenteeListAdapter(Context context, ArrayList<Pair<User,String>> menteeList)
    {
        this.context = context;
        this.menteeList = menteeList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_mentee,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Pair<User,String> menteeInfo = menteeList.get(position);
        String menteeId = menteeInfo.second;
        User mentee = menteeInfo.first;
        holder.binding.menteeName.setText(mentee.getUsername());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("uid", menteeId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menteeList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemMenteeBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMenteeBinding.bind(itemView);
        }
    }

}

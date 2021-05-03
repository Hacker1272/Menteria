package com.satya.menteria.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.satya.menteria.Adapters.MenteeListAdapter;
import com.satya.menteria.Model.User;
import com.satya.menteria.R;
import com.satya.menteria.databinding.ActivityMainBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    String currentUid;
    String mentorLevelPool;
    String mentorId;
    ArrayList<Pair<User,String>> menteeList;
    MenteeListAdapter mAdapter;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        menteeList = new ArrayList<>();
        mAdapter = new MenteeListAdapter(this, menteeList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(mAdapter);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        currentUid = auth.getUid();



        database.getReference().child("users").child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            currentUser = snapshot.getValue(User.class);
                            binding.usernameBox.setText(currentUser.getUsername());
                            binding.codeforcesHandleBox.setText(currentUser.getCodeforcesHandle());
                            mentorId = currentUser.getMentor();
                            Glide.with(MainActivity.this).load(currentUser.getImageUrl())
                                    .placeholder(R.drawable.ic_baseline_account_circle_100)
                                    .into(binding.profileImage);

                            if(currentUser.getMentor().equals("NO_MENTOR_ASSIGNED"))
                            {
                                binding.mentorName.setText(currentUser.getMentor());
                                DecideMentorLevelPool(currentUser.getLevelPool());
                                mentorAllocation();
                            }else{
                                database.getReference().child("users").child(mentorId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists())
                                                {
                                                    User mentor = snapshot.getValue(User.class);
                                                    binding.mentorName.setText(mentor.getUsername());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference().child("mentees").child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            menteeList.clear();
                            for(DataSnapshot snapshot1 : snapshot.getChildren())
                            {
                                String menteeId = snapshot1.getKey();
                                User mentee = snapshot1.getValue(User.class);
                                menteeList.add(new Pair<>(mentee,menteeId));
                            }
                            Log.v("menteeListSize", String.valueOf(menteeList.size()));
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.mentorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mentorId.equals("NO_MENTOR_ASSIGNED")) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("uid", mentorId);
                    startActivity(intent);
                }

            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }

    private void DecideMentorLevelPool(String userLevelPool)
    {
        switch(userLevelPool)
        {
            case "level_5": mentorLevelPool = "level_4"; break;
            case "level_4": mentorLevelPool = "level_3"; break;
            case "level_3": mentorLevelPool = "level_2"; break;
            case "level_2": mentorLevelPool = "level_1"; break;
            default: mentorLevelPool = "mentor_of_mentors"; break;
        }
    }

    private void mentorAllocation()
    {
        database.getReference().child("levels")
                .child(mentorLevelPool)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String id = null;
                            Long temp = Long.MAX_VALUE;
                            for(DataSnapshot snapshot1:snapshot.getChildren())
                            {
                                HashMap<String,Object> count = (HashMap<String, Object>) snapshot1.getValue();

                                if((Long) count.get("MenteeCount") < temp)
                                {
                                    temp = (Long) count.get("MenteeCount");
                                    id = snapshot1.getKey();
                                }
                            }

                            mentorId = id;


                            //update the mentee count
                            HashMap<String,Object> menteeCount = new HashMap<>();
                            menteeCount.put("MenteeCount", temp+1);
                            database.getReference().child("levels").child(mentorLevelPool).child(mentorId)
                                    .updateChildren(menteeCount);

                            //update the mentorInfo in database
                            HashMap<String, Object> mentorInfo = new HashMap<>();
                            mentorInfo.put("mentor", id);
                            String finalId = id;
                            database.getReference().child("users")
                                    .child(currentUid).updateChildren(mentorInfo);

                            //update the menteeInfo in database
                            database.getReference().child("mentees").child(mentorId)
                                    .child(currentUid).setValue(currentUser);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
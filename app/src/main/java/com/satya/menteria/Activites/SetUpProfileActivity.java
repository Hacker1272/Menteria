package com.satya.menteria.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.satya.menteria.Model.User;
import com.satya.menteria.R;
import com.satya.menteria.databinding.ActivitySetUpProfileBinding;

import java.util.HashMap;

public class SetUpProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    private static final int IMAGE_SELECTOR_CODE = 233;
    ActivitySetUpProfileBinding binding;
    Uri selectedImageUri=null;
    String username;
    String codeforcesHandle;
    String codeforcesRating;
    String levelPool = "level_5";

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

//        binding.profileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setType("images/*");
//                startActivityForResult(intent, IMAGE_SELECTOR_CODE);;
//            }
//        });

        binding.setUpProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeforcesRating = binding.codeforcesRatingBox.getText().toString();
                codeforcesHandle = binding.codeforcesHandleBox.getText().toString();
                username = binding.usernameBox.getText().toString();

                if(!ValidateFeilds()) return;

                dialog.show();
                InsertUserDataIntoFirebase();
            }
        });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == IMAGE_SELECTOR_CODE)
//        {
//            if(data!=null)
//            {
//                if(data.getData()!=null)
//                {
//                    selectedImageUri = data.getData();
//                    Glide.with(this).load(selectedImageUri)
//                            .placeholder(R.drawable.ic_baseline_account_circle_100)
//                            .into(binding.profileImage);
//                    binding.profileImage.setImageURI(selectedImageUri);
//                }
//            }
//        }
//    }

    private boolean ValidateFeilds() {

        String error = "This field can not be left blank";
        username = username.trim();
        codeforcesHandle = codeforcesHandle.trim();
        codeforcesRating = codeforcesRating.trim();

        if(!DecideUserLevelPool())
        {
            binding.codeforcesHandleBox.setError("Invalid Rating");
            return false;
        }
        if(username.isEmpty())
        {
            binding.usernameBox.setError(error);
        }
        if(codeforcesHandle.isEmpty())
        {
            binding.codeforcesHandleBox.setError(error);
            return false;
        }
        if(codeforcesRating.isEmpty() || !DecideUserLevelPool())
        {
            binding.codeforcesRatingBox.setError(error);
            return false;
        }
        return true;
    }


    private void InsertUserDataIntoFirebase(){

        HashMap<String, Object> count = new HashMap<>();
        count.put("MenteeCount", 0);
        database.getReference().child("levels")
                .child(levelPool)
                .child(auth.getUid())
                .updateChildren(count)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });


        if(selectedImageUri!=null)
        {
            StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
            reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                User user = new User(username, codeforcesHandle, codeforcesRating, levelPool, uri.toString());
                                database.getReference()
                                        .child("users")
                                        .child(auth.getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                HeadToDashboard();
                                            }
                                        });
                            }
                        });
                    }
                }
            });
        }

        else{
            User user = new User(username, codeforcesHandle, codeforcesRating, levelPool, "No Image");
            database.getReference()
                    .child("users")
                    .child(auth.getUid())
                    .setValue(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            HeadToDashboard();
                        }
                    });
        }
    }

    private boolean DecideUserLevelPool() {
        int rating = Integer.parseInt(codeforcesRating);
        if(rating<0) return false;
        rating/=100;
        if(rating>=40) return false;
        else if(rating>=30) levelPool = "level_1";
        else if(rating>=20) levelPool = "level_2";
        else if(rating>=15) levelPool = "level_3";
        else if(rating>=10) levelPool = "level_4";
        else levelPool = "level_5";

        return true;
    }

    private void HeadToDashboard()
    {
        Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
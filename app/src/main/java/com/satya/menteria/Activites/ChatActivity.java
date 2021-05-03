package com.satya.menteria.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.satya.menteria.Adapters.MessagesAdapter;
import com.satya.menteria.Model.Message;
import com.satya.menteria.Model.User;
import com.satya.menteria.R;
import com.satya.menteria.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {


    ActivityChatBinding binding;

    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    String senderUid,receiverUid;
    ProgressDialog dialog;


    private static final int ATTACHMENT_SELECTOR_REQUEST_CODE = 123;

    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image...");
        dialog.setCancelable(false);

        receiverUid = getIntent().getStringExtra("uid");


        database.getReference().child("users").child(receiverUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String receiverName = snapshot.getValue(User.class).getUsername();
                            getSupportActionBar().setTitle(receiverName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerview.setLayoutManager(linearLayoutManager);
        binding.recyclerview.setAdapter(messagesAdapter);
        binding.recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                binding.recyclerview.scrollToPosition(messagesAdapter.getItemCount()-1);
            }
        });


        database.getReference().child("chats").child(senderRoom).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren())
                {
                    Message message = snapshot1.getValue(Message.class);
                    message.setMessageId(snapshot1.getKey());
                    messages.add(message);
                }
                messagesAdapter.notifyDataSetChanged();
                binding.recyclerview.scrollToPosition(messagesAdapter.getItemCount()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Adding On Click Listener on Attachment Button

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, ATTACHMENT_SELECTOR_REQUEST_CODE);
            }
        });

        //Adding On Click Listener on Send Button

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.recyclerview.scrollToPosition(messagesAdapter.getItemCount()-1);
                String messageText = binding.messageBox.getText().toString().trim();
                if(messageText.isEmpty())
                {
                    return;
                }
                binding.messageBox.setText("");

                Date date = new Date();

                Message message = new Message(messageText, senderUid, date.getTime());

                updateLastMessageDetails(message);
                sendMessage(message);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ATTACHMENT_SELECTOR_REQUEST_CODE)
        {
            if(data != null) {
                if (data.getData() != null)
                {
                    dialog.show();
                    Uri selectedImageUri = data.getData();
                    Date date = new Date();

                    StorageReference reference = storage.getReference().child("Chats").child("images").child(date.getTime() + "");
                    reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.dismiss();
                                        Message message = new Message("photo", senderUid, date.getTime());
                                        message.setImageUrl(uri.toString());
                                        updateLastMessageDetails(message);
                                        sendMessage(message);
                                    }
                                });

                            }else{
                                Toast.makeText(ChatActivity.this, "Error occurred while sending image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        }
    }

    private void updateLastMessageDetails(Message message)
    {
        HashMap<String, Object> hashMap = new HashMap<>();
        String lastmessage = message.getMessage();
        lastmessage = lastmessage.substring(0, Math.min(lastmessage.length(),24));
        hashMap.put("lastmsg", lastmessage);
        hashMap.put("lastmsgtime", message.getTimestamp());

        database.getReference().child("chats").child(senderRoom).updateChildren(hashMap);
        database.getReference().child("chats").child(receiverRoom).updateChildren(hashMap);
    }
    private void sendMessage(Message message)
    {
        String uniqueMessageId = database.getReference().push().getKey();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .child(uniqueMessageId)
                .setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(uniqueMessageId)
                                .setValue(message)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
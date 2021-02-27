package com.satya.menteria.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.satya.menteria.Model.User;

import java.util.Set;

public class SignInActivity extends AppCompatActivity {

    com.satya.menteria.databinding.ActivitySignInBinding binding;

    FirebaseAuth auth;

    String emailId;
    String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.satya.menteria.databinding.ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null)
        {
            HeadToDashboard();
        }

//        Get the users email and password
//        emaiolId = binding.emailBox.getText().toString();
//        password = binding.passwordBox.getText().toString();
//        repassword = binding.repasswordBox.getText().toString();


        binding.signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(!password.equals(repassword))
//                {
//                    binding.repasswordBox.setError("Password do not match");
//                    return;
//                }


                auth.createUserWithEmailAndPassword(emailId, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Intent intent = new Intent(SignInActivity.this, SetUpProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(SignInActivity.this, "Some error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private void HeadToDashboard()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
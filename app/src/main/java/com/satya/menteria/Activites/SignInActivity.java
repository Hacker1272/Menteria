package com.satya.menteria.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
    String repassword;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.satya.menteria.databinding.ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();


        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        //Get the users email and password



        binding.signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailId = binding.emailBox.getText().toString();
                password = binding.passwordBox.getText().toString();
                repassword = binding.repasswordBox.getText().toString();
                password.trim();
                repassword.trim();
                if(password.isEmpty())
                {
                    binding.passwordBox.setError("This feild can not be left blank");
                    return;
                }
                if(emailId.isEmpty())
                {
                    binding.emailBox.setError("This feild can not be left blank");
                    return;
                }
                if(repassword.isEmpty())

                {
                    binding.repasswordBox.setError("This feild can not be left blank");
                    return;
                }
                if(!password.equals(repassword))
                {
                    binding.repasswordBox.setError("Password do not match");
                    return;
                }

                dialog.show();
                auth.createUserWithEmailAndPassword(emailId, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
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



}
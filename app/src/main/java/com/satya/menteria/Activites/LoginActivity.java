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
import com.satya.menteria.R;
import com.satya.menteria.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    String email;
    String password;
    FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        email = binding.emailBox.getText().toString();
        password = binding.passwordBox.getText().toString();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying...");
        dialog.setCancelable(false);

        binding.loginbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                email = email.trim();
                password = password.trim();
                if(email.isEmpty())
                {
                    binding.emailBox.setError("This field can not be left blank");
                    return;
                }
                if(password.isEmpty())
                {
                    binding.passwordBox.setError("This field can not be left blank");
                    return;
                }
                dialog.show();

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
                                if(task.isSuccessful())
                                {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
            }
        });

    }
}
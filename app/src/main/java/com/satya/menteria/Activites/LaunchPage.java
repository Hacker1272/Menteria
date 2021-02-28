package com.satya.menteria.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.satya.menteria.R;
import com.satya.menteria.databinding.ActivityLaunchPageBinding;

public class LaunchPage extends AppCompatActivity {

    ActivityLaunchPageBinding binding;

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityLaunchPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null)
        {
            HeadToDashboard();
        }

        binding.loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchPage.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        binding.registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchPage.this, SignInActivity.class);
                startActivity(intent);
                finish();
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
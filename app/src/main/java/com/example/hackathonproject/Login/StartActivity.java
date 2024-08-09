package com.example.hackathonproject.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.MainActivity;
import com.example.hackathonproject.R;

public class StartActivity extends AppCompatActivity {
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_start);
            btnStart = findViewById(R.id.btnStart);

            Toast.makeText(this, "메인 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show();

            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(StartActivity.this, SignInPhoneNumActivity.class);
                startActivity(intent);
            });
        }
    }
}

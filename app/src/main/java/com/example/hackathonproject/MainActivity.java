package com.example.hackathonproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStart);

        Toast.makeText(this, "메인 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show();

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInPhoneNumActivity.class);
            startActivity(intent);
        });
    }
}

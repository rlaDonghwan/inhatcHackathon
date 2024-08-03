package com.example.hackathonproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 메인 화면이 나타날 때 간단한 메시지 표시
        Toast.makeText(this, "메인 화면이 표시되었습니다.", Toast.LENGTH_SHORT).show();
    }
}

package com.example.hackathonproject.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackathonproject.R;

public class FontSizeActivity extends Activity {
    private Button btnContinue;
    private ImageButton backButton;
    private Button decreaseFontButton;
    private Button increaseFontButton;
    private TextView sampleText;
    private int fontSize;  // 폰트 크기 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_size);

        // SharedPreferences에서 저장된 폰트 크기를 불러옴, 기본값은 25
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        fontSize = preferences.getInt("fontSize", 25);

        // 텍스트 뷰 및 버튼 초기화
        sampleText = findViewById(R.id.sample_text);
        decreaseFontButton = findViewById(R.id.decrease_font_button);
        increaseFontButton = findViewById(R.id.increase_font_button);

        // 불러온 폰트 크기를 텍스트 뷰에 설정
        sampleText.setTextSize(fontSize);

        // 폰트 크기 감소 버튼
        decreaseFontButton.setOnClickListener(v -> {
            if (fontSize > 15) {  // 최소 폰트 크기 제한
                fontSize -= 10;
                sampleText.setTextSize(fontSize);
                saveFontSize(fontSize);  // 폰트 크기 저장
            } else {
                Toast.makeText(FontSizeActivity.this, "더 이상 폰트 크기를 줄일 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 폰트 크기 증가 버튼
        increaseFontButton.setOnClickListener(v -> {
            if (fontSize < 35) {  // 최대 폰트 크기 제한
                fontSize += 10;
                sampleText.setTextSize(fontSize);
                saveFontSize(fontSize);  // 폰트 크기 저장
            } else {
                Toast.makeText(FontSizeActivity.this, "더 이상 폰트 크기를 늘릴 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // '확인' 버튼 초기화 및 클릭 이벤트
        btnContinue = findViewById(R.id.confirm_button);
        btnContinue.setOnClickListener(v -> {
            // 현재 폰트 크기를 저장
            saveFontSize(fontSize);

            // 확인 버튼 클릭 시 다음 화면으로 이동 (예: 로그인 화면)
            Intent intent = new Intent(FontSizeActivity.this, SignInPhoneNumActivity.class);
            startActivity(intent);
            finish();  // FontSizeActivity 종료
        });

        // 뒤로 가기 버튼 초기화 및 클릭 이벤트
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());  // 현재 액티비티 종료
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------

    private void saveFontSize(int fontSize) {
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fontSize", fontSize);
        editor.apply();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------
}

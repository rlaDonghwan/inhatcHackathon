package com.example.hackathonproject.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;
import com.example.hackathonproject.db.AuthManager;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CertificationActivity extends AppCompatActivity {

    private EditText schoolNameInput;
    private EditText companyNameInput;
    private Button schoolCertifyButton;
    private Button companyCertifyButton;
    private TextView schoolSearchLabel;
    private TextView companySearchLabel;
    private ArrayList<String> companyList;
    private ArrayList<String> schoolList;

    private String name;
    private String password;
    private String phoneNum;
    private String birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_certification);

        // View 초기화
        schoolNameInput = findViewById(R.id.school_name_input);
        companyNameInput = findViewById(R.id.company_name_input);
        schoolCertifyButton = findViewById(R.id.school_certify_button);
        companyCertifyButton = findViewById(R.id.company_certify_button);
        schoolSearchLabel = findViewById(R.id.school_search_label);
        companySearchLabel = findViewById(R.id.company_search_label);

        companyList = new ArrayList<>();
        schoolList = new ArrayList<>();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // 이전 Activity에서 전달된 데이터 받기
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        password = intent.getStringExtra("password");
        phoneNum = intent.getStringExtra("phoneNum");
        birthDate = intent.getStringExtra("birthDate");
        boolean isOrganization = intent.getBooleanExtra("isOrganization", false);
        boolean isSchool = intent.getBooleanExtra("isSchool", false);

        // 필드 활성화/비활성화 설정
        setupFieldActivation(isSchool, isOrganization);

        // 폰트 크기 설정
        applyFontSize();

        // 검색 버튼 추가 및 클릭 리스너 설정
        ImageButton schoolSearchButton = findViewById(R.id.school_search_button);
        ImageButton companySearchButton = findViewById(R.id.company_search_button);

        schoolSearchButton.setOnClickListener(v -> {
            String schoolName = schoolNameInput.getText().toString().trim();
            if (!schoolName.isEmpty()) {
                new SchoolCertificationTask().execute(schoolName);
            } else {
                Toast.makeText(CertificationActivity.this, "학교 이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        companySearchButton.setOnClickListener(v -> {
            String companyName = companyNameInput.getText().toString().trim();
            if (!companyName.isEmpty()) {
                new CompanyCertificationTask().execute(companyName);
            } else {
                Toast.makeText(CertificationActivity.this, "기관 이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 인증 버튼 클릭 이벤트 추가
        schoolCertifyButton.setOnClickListener(v -> verifyAndRegister("school", schoolNameInput.getText().toString(), null));
        companyCertifyButton.setOnClickListener(v -> verifyAndRegister("company", null, companyNameInput.getText().toString()));
    }

    private void setupFieldActivation(boolean isSchool, boolean isOrganization) {
        if (isSchool) {
            schoolNameInput.setEnabled(true);
            schoolCertifyButton.setEnabled(true);
            companyNameInput.setEnabled(false);
            companyCertifyButton.setEnabled(false);
        } else if (isOrganization) {
            schoolNameInput.setEnabled(false);
            schoolCertifyButton.setEnabled(false);
            companyNameInput.setEnabled(true);
            companyCertifyButton.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼을 누르면 SignUpActivity로 이동
        Intent intent = new Intent(CertificationActivity.this, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void verifyAndRegister(String type, String schoolName, String companyName) {
        String institutionName = type.equals("school") ? schoolName : companyName;
        if (institutionName == null || institutionName.isEmpty()) {
            Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 사용자 존재 여부 확인 및 학교 또는 기관 인증 작업 실행
        new VerifyTask().execute(type, institutionName, name, password, phoneNum, birthDate, schoolName, companyName);
    }

    // 폰트 크기 적용 메서드
    private void applyFontSize() {
        SharedPreferences preferences = getSharedPreferences("fontSizePrefs", MODE_PRIVATE);
        int savedFontSize = preferences.getInt("fontSize", 25);  // 기본값 25sp

        schoolNameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        companyNameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        schoolCertifyButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        companyCertifyButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        schoolSearchLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        companySearchLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
    }

    private class VerifyTask extends AsyncTask<Object, Void, Boolean> {

        private String type;
        private String inputName;
        private String schoolName;
        private String companyName;
        private String existingUserName;

        @Override
        protected Boolean doInBackground(Object... params) {
            type = (String) params[0];
            inputName = (String) params[1];
            schoolName = (String) params[6];
            companyName = (String) params[7];

            AuthManager authManager = new AuthManager();
            try {
                // 사용자 존재 여부 확인
                existingUserName = authManager.getUserNameByPhone(phoneNum);
                if (existingUserName != null) {
                    return false; // 이미 존재하는 사용자
                }

                String apiUrl;
                if (type.equals("school")) {
                    apiUrl = "https://www.career.go.kr/cnet/openapi/getOpenApi?apiKey=5b97e5ba11232b7661bc0c69df34e5bd&svcType=api&svcCode=SCHOOL&contentType=xml&gubun=univ_list&searchSchulNm=" + inputName;
                } else {
                    apiUrl = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2/getCorpOutline_V2?ServiceKey=z%2F5j97wfXdV4OfQ1Ze%2FszF79mEXa4NL2O0CVaRm5J7D4mxFJJvqbR%2BLrgvwAv%2FTExOmAxygPZu6HDNc9SezHTw%3D%3D&pageNo=1&numOfRows=20&resultType=xml&corpNm=" + inputName;
                }

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(connection.getInputStream());

                NodeList nodeList = type.equals("school") ? doc.getElementsByTagName("schoolName") : doc.getElementsByTagName("corpNm");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (inputName.equals(nodeList.item(i).getTextContent())) {
                        return true; // 인증 성공
                    }
                }

                return false; // 인증 실패

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isVerified) {
            if (existingUserName != null) {
                Toast.makeText(CertificationActivity.this, existingUserName + "님, 이미 존재하는 사용자입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isVerified) {
                // 인증 성공 메시지 표시 및 회원가입 완료 처리
                Toast.makeText(CertificationActivity.this, inputName + " 인증 완료! 회원가입을 완료합니다.", Toast.LENGTH_SHORT).show();

                // 회원가입 작업 실행
                new RegisterUserTask().execute(name, password, phoneNum, birthDate, type.equals("company"), companyName, schoolName);

            } else {
                Toast.makeText(CertificationActivity.this, inputName + " 인증 실패. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 학교 인증 처리
    private class SchoolCertificationTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String schoolName = params[0];
            try {
                String apiUrl = "https://www.career.go.kr/cnet/openapi/getOpenApi?apiKey=5b97e5ba11232b7661bc0c69df34e5bd&svcType=api&svcCode=SCHOOL&contentType=xml&gubun=univ_list&searchSchulNm=" + schoolName;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(connection.getInputStream());

                NodeList nodeList = doc.getElementsByTagName("schoolName");

                schoolList.clear();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    schoolList.add(nodeList.item(i).getTextContent());
                }

                return schoolList.size() > 0;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean hasResults) {
            if (hasResults) {
                showSchoolListDialog();
            } else {
                Toast.makeText(CertificationActivity.this, "학교 인증 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 기업 인증 처리
    private class CompanyCertificationTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String companyName = params[0];
            try {
                String apiUrl = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2/getCorpOutline_V2?ServiceKey=z%2F5j97wfXdV4OfQ1Ze%2FszF79mEXa4NL2O0CVaRm5J7D4mxFJJvqbR%2BLrgvwAv%2FTExOmAxygPZu6HDNc9SezHTw%3D%3D&pageNo=1&numOfRows=20&resultType=xml&corpNm=" + companyName;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(connection.getInputStream());

                NodeList nodeList = doc.getElementsByTagName("corpNm");

                companyList.clear();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    companyList.add(nodeList.item(i).getTextContent());
                }

                return companyList.size() > 0;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean hasResults) {
            if (hasResults) {
                showCompanyListDialog();
            } else {
                Toast.makeText(CertificationActivity.this, "기관 인증 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 학교 목록을 보여주는 다이얼로그
    private void showSchoolListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CertificationActivity.this);
        builder.setTitle("학교 선택");
        builder.setItems(schoolList.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedSchool = schoolList.get(which);
                schoolNameInput.setText(selectedSchool);
                schoolCertifyButton.setEnabled(true);
            }
        });
        builder.show();
    }

    // 기업 목록을 보여주는 다이얼로그
    private void showCompanyListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CertificationActivity.this);
        builder.setTitle("기관 선택");
        builder.setItems(companyList.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedCompany = companyList.get(which);
                companyNameInput.setText(selectedCompany);
                companyCertifyButton.setEnabled(true);
            }
        });
        builder.show();
    }

    // 회원가입 작업을 백그라운드에서 처리하는 AsyncTask 클래스
    private class RegisterUserTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            String name = (String) params[0];
            String password = (String) params[1];
            String phoneNum = (String) params[2];
            String birthDate = (String) params[3];
            boolean isOrganization = (boolean) params[4];
            String companyName = (String) params[5];
            String schoolName = (String) params[6];

            try {
                AuthManager authManager = new AuthManager();
                return authManager.registerUser(name, password, phoneNum, birthDate, isOrganization, companyName, schoolName);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(CertificationActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CertificationActivity.this, SignInPhoneNumActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CertificationActivity.this, "회원가입 실패: 이미 존재하는 사용자", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
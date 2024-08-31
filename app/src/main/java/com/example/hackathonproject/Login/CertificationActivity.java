package com.example.hackathonproject.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathonproject.R;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CertificationActivity extends AppCompatActivity {

    private EditText schoolNameInput;
    private EditText companyNameInput;
    private Button schoolCertifyButton;
    private Button companyCertifyButton;
    private ArrayList<String> companyList;
    private ArrayList<String> schoolList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_certification);

        // View 초기화
        schoolNameInput = findViewById(R.id.school_name_input);
        companyNameInput = findViewById(R.id.company_name_input);
        schoolCertifyButton = findViewById(R.id.school_certify_button);
        companyCertifyButton = findViewById(R.id.company_certify_button);

        companyList = new ArrayList<>();
        schoolList = new ArrayList<>();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // SignUpActivity에서 넘어온 Intent 데이터 받기
        boolean isSchoolChecked = getIntent().getBooleanExtra("isSchool", false);
        boolean isCompanyChecked = getIntent().getBooleanExtra("isOrganization", false);

        // 초기 상태 설정
        schoolNameInput.setEnabled(isSchoolChecked);
        schoolCertifyButton.setEnabled(isSchoolChecked);
        companyNameInput.setEnabled(isCompanyChecked);
        companyCertifyButton.setEnabled(isCompanyChecked);

        // 학교 인증 버튼 클릭 이벤트
        schoolCertifyButton.setOnClickListener(v -> {
            String schoolName = schoolNameInput.getText().toString().trim();
            if (!schoolName.isEmpty()) {
                new SchoolCertificationTask().execute(schoolName);
            } else {
                Toast.makeText(CertificationActivity.this, "학교 이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 기관 인증 버튼 클릭 이벤트
        companyCertifyButton.setOnClickListener(v -> {
            String companyName = companyNameInput.getText().toString().trim();
            if (!companyName.isEmpty()) {
                new CompanyCertificationTask().execute(companyName);
            } else {
                Toast.makeText(CertificationActivity.this, "기관 이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼을 누르면 SignUpActivity로 이동
        Intent intent = new Intent(CertificationActivity.this, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
                Toast.makeText(CertificationActivity.this, "선택된 학교: " + selectedSchool, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CertificationActivity.this, "선택된 기관: " + selectedCompany, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
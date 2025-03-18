package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Properties;

public class FindPwActivity extends AppCompatActivity {

    Button btnBack, btnIdCheck, btnSend;
    EditText etId;
    TextView tvEmail;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> idList = new ArrayList<>();
    ArrayList<String> emailList = new ArrayList<>();
    Boolean idCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pw);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnIdCheck = (Button) findViewById(R.id.btnIdCheck);
        btnSend = (Button) findViewById(R.id.btnSend);
        etId = (EditText) findViewById(R.id.etId);
        tvEmail = (TextView) findViewById(R.id.tvEmail);

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnIdCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "select * from member";
                Log.d("SQL", sql);
                Cursor cursor = sqlDB.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    String mail = cursor.getString(6);

                    idList.add(id);
                    emailList.add(mail);
                }
                cursor.close();

                if(idList.contains(etId.getText().toString())) {
                    int index = idList.indexOf(etId.getText().toString());
                    String email = emailList.get(index);
                    tvEmail.setText(email);
                    idCheck = true;
                } else {
                    Toast.makeText(getApplicationContext(), "일치하는 아이디가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idList.contains(etId.getText().toString()) && idCheck) {
                    // 이메일 보내기
                    int index = idList.indexOf(etId.getText().toString());
                    String email = emailList.get(index);
                    String userId = idList.get(index);
                    tvEmail.setText(email);
                    new GMailSender(FindPwActivity.this).sendEmailPW(email, userId);
                } else {
                    Toast.makeText(getApplicationContext(), "아이디 일치 여부를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

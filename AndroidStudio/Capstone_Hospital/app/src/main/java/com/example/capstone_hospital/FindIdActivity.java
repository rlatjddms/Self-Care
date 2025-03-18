package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FindIdActivity extends AppCompatActivity {

    Button btnBack, btnSend, btnFindId;
    EditText etName, etPhone, etEmail, etNum;
    TextView tvResult;
    LinearLayout resultLayout;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    ArrayList<String> idList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> phoneList = new ArrayList<>();
    GMailSender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnFindId = (Button) findViewById(R.id.btnFindId);
        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etNum = (EditText) findViewById(R.id.etNum);
        tvResult = (TextView) findViewById(R.id.tvResult);
        resultLayout = (LinearLayout) findViewById(R.id.resultLayout);

        resultLayout.setVisibility(View.INVISIBLE);

        sender = new GMailSender(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        myHelper = new MainActivity.MyDBHelper(getApplicationContext(), "capstone", null, 1);
        sqlDB = myHelper.getReadableDatabase();

        String sql = "select * from member";
        Log.d("SQL", sql);
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(2);
            String phone = cursor.getString(5);

            idList.add(id);
            nameList.add(name);
            phoneList.add(phone);
        }
        cursor.close();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputName = etName.getText().toString();
                String inputPhone = etPhone.getText().toString();

                boolean isMatch = false;

                for (int i = 0; i < nameList.size(); i++) {
                    if (nameList.get(i).equals(inputName) && phoneList.get(i).equals(inputPhone)) {
                        isMatch = true;
                        break;
                    }
                }

                if (!nameList.contains(inputName)) {
                    Toast.makeText(getApplicationContext(), "존재하지 않는 회원 이름입니다.", Toast.LENGTH_SHORT).show();
                } else if (!phoneList.contains(inputPhone)) {
                    Toast.makeText(getApplicationContext(), "존재하지 않는 회원 전화번호입니다.", Toast.LENGTH_SHORT).show();
                } else if (!isMatch) {
                    Toast.makeText(getApplicationContext(), "회원 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 이메일 보내기
                    sender.generateAuthenticationNum();
                    sender.sendEmailID(etEmail.getText().toString());
                }
            }
        });

        btnFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputName = etName.getText().toString();
                String inputPhone = etPhone.getText().toString();
                String inputNum = etNum.getText().toString();

                StringBuilder builder = new StringBuilder();
                boolean isMatch = false;

                for (int i = 0; i < nameList.size(); i++) {
                    if (nameList.get(i).equals(inputName) && phoneList.get(i).equals(inputPhone)) {
                        builder.append(idList.get(i)).append("\n");
                        isMatch = true;
                    }
                }

                if (!nameList.contains(inputName)) {
                    Toast.makeText(getApplicationContext(), "존재하지 않는 회원 이름입니다.", Toast.LENGTH_SHORT).show();
                } else if (!phoneList.contains(inputPhone)) {
                    Toast.makeText(getApplicationContext(), "존재하지 않는 회원 전화번호입니다.", Toast.LENGTH_SHORT).show();
                } else if (!isMatch) {
                    Toast.makeText(getApplicationContext(), "일치하는 회원 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                } else if (inputNum.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (!sender.getAuthenticationNum().equals(inputNum)) {
                    Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    resultLayout.setVisibility(View.VISIBLE);
                    tvResult.setText(builder.toString().trim());
                }
            }
        });
    }
}

class GMailSender extends Authenticator {
    private final String fromEmail = "MAIL"; // 보낼 이메일 주소
    private final String password = "PASSWORD"; // 비밀번호
    private final Context context;
    SQLiteDatabase sqlDB;
    MainActivity.MyDBHelper myHelper;
    private String authenticationNum = "";

    public GMailSender(Context context) {
        this.context = context;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(fromEmail, password);
    }

    public void generateAuthenticationNum() {
        authenticationNum = NumberGenerator.generateRandomPassword();
        Log.d("authenticationNum", "Generated authenticationNum: " + authenticationNum);
    }

    // 아이디 찾기 메일 전송
    public void sendEmailID(final String toEmail) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    authenticationNum = NumberGenerator.generateRandomPassword();

                    Properties props = new Properties();
                    props.setProperty("mail.transport.protocol", "smtp");
                    props.setProperty("mail.host", "smtp.gmail.com");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.fallback", "false");
                    props.setProperty("mail.smtp.quitwait", "false");

                    // 구글에서 지원하는 smtp 정보를 받아와 MimeMessage 객체에 전달
                    Session session = Session.getDefaultInstance(props, GMailSender.this);

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail, "[SelfCare] 질병 자가 진단 앱"));
                    message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
                    message.setSubject("인증번호 발송");
                    message.setContent(
                            "<h3>인증번호 발송</h3>" +
                                    "<p>인증번호는 <u>" + authenticationNum + "</u> 입니다.</p>" +
                                    "<p>타인에게 절대 알려주지 마세요.</p>",
                            "text/html; charset=utf-8"
                    );

                    Transport.send(message);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "인증번호가 성공적으로 발송되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "이메일 발송에 실패했습니다. 정확한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public String getAuthenticationNum() {
        return authenticationNum;
    }

    // 비밀번호 찾기 메일 전송
    public void sendEmailPW(final String toEmail, String userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String tempPassword = PasswordGenerator.generateRandomPassword();
                    updatePW(userId, tempPassword);

                    Properties props = new Properties();
                    props.setProperty("mail.transport.protocol", "smtp");
                    props.setProperty("mail.host", "smtp.gmail.com");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.fallback", "false");
                    props.setProperty("mail.smtp.quitwait", "false");

                    // 구글에서 지원하는 smtp 정보를 받아와 MimeMessage 객체에 전달
                    Session session = Session.getDefaultInstance(props, GMailSender.this);

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail, "[SelfCare] 질병 자가 진단 앱"));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                    message.setSubject("임시 비밀번호 발송");
                    message.setContent(
                            "<h3>임시 비밀번호 발송</h3>" +
                                    "<p>당신의 임시 비밀번호는 <u>" + tempPassword + "</u> 입니다.</p>" +
                                    "<p>로그인 후 비밀번호 재설정을 추천드립니다.</p>",
                            "text/html; charset=utf-8"
                    );

                    Transport.send(message);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "이메일이 성공적으로 발송되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "이메일 발송에 실패했습니다. 정확한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void updatePW(String userId, String tempPassword) {
        myHelper = new MainActivity.MyDBHelper(context, "capstone", null, 1);
        sqlDB = myHelper.getWritableDatabase();

        sqlDB.execSQL("UPDATE member SET pw = ? WHERE id = ?", new Object[]{tempPassword, userId});

        // 서버에 POST 요청 보내기
        new Thread(() -> {
            try {
                URL url = new URL("https://kse.calab.myds.me/update_member.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String columnName = "pw";
                String postData = "columnName=" + URLEncoder.encode(columnName, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&item=" + URLEncoder.encode(tempPassword, "UTF-8");

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("SERVER_RESPONSE", response.toString());
                } else {
                    Log.e("SERVER_ERROR", "Response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HTTP_ERROR", e.getMessage());
            }
        }).start();
    }
}

class NumberGenerator {
    private static final String CHARACTERS = "0123456789";
    private static final int PASSWORD_LENGTH = 6; // 원하는 비밀번호 길이

    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}

class PasswordGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 7; // 원하는 비밀번호 길이

    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}
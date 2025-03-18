package com.example.capstone_hospital;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SkinInfoActivity extends AppCompatActivity {

    Button btnBack;
    TextView tvDiseaseName;
    WebView webView;
    String skin_url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_info);

        btnBack = findViewById(R.id.btnBack);
        tvDiseaseName = findViewById(R.id.tvDiseaseName);
        webView = findViewById(R.id.webView);

        String label = getIntent().getStringExtra("label");
        tvDiseaseName.setText(label);
        skin_url = "https://m.amc.seoul.kr/asan/healthinfo/disease/diseaseList.do?searchKeyword=" + label;

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Jsoup로 URL 추출 작업 실행
        new SkinJsoup().execute();
    }

    private class SkinJsoup extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String detailUrl = ""; // 상세 URL을 저장할 변수
            try {
                // Jsoup로 HTML 문서 가져오기
                Document doc = Jsoup.connect(skin_url).get();

                // 특정 요소에서 링크 추출
                Elements urls = doc.select("strong.contTitle a"); // <strong class="contTitle"> 안의 <a> 태그 선택

                if (!urls.isEmpty()) {
                    // 첫 번째 링크를 가져옴
                    detailUrl = "https://m.amc.seoul.kr" + urls.first().attr("href"); // 절대 URL로 변경
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return detailUrl; // 링크 반환
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!result.isEmpty()) {
                Log.d("Extracted URL", result);
                // 추출한 링크를 WebView에 로드
                webView.loadUrl(result);
            } else {
                Log.d("Extracted URL", "No URL found");
            }
        }
    }
}

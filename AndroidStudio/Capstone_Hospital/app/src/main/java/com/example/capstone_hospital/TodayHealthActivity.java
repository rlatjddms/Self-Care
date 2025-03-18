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
import android.widget.FrameLayout;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TodayHealthActivity extends AppCompatActivity {

    Button btnBack;
    WebView webView;
    String health_url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_health);

        btnBack = (Button) findViewById(R.id.btnBack);
        webView = (WebView) findViewById(R.id.webView);

        health_url = getIntent().getStringExtra("url");
        Log.d("url", health_url);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        webView.loadUrl(health_url);
    }
}
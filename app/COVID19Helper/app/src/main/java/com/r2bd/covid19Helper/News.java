package com.r2bd.covid19Helper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class News extends AppCompatActivity {

    WebView webVwLocal;
    WebView webVwGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        //Setting the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        webVwGlobal = findViewById(R.id.webVwGlobal);
        webVwGlobal.setWebViewClient(new WebViewClient());
        webVwGlobal.loadUrl("https://news.google.com/search?q=COVID19");

    }
}

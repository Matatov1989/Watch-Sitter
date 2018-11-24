package com.sergeant_matatov.watchsitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class PrivacyPolicy extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_privacy_policy);

        mWebView = (WebView) findViewById(R.id.webView);

        mWebView.getSettings().setJavaScriptEnabled(true);  // enable support JavaScript
        mWebView.loadUrl("https://sites.google.com/view/watch-sitter/");   // link for url
    }

    //exit
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PrivacyPolicy.this, AboutProgram.class));
    }
}

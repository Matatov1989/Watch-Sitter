package com.sergeant_matatov.watchsitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_privacy_policy);
    }

    //выход в About
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PrivacyPolicy.this, AboutProgram.class));
    }
}

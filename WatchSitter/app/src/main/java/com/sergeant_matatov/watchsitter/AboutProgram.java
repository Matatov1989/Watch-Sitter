package com.sergeant_matatov.watchsitter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Yurka on 18.10.2016.
 */

public class AboutProgram extends AppCompatActivity {

    TextView textPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about_program);

        textPolicy = (TextView) findViewById(R.id.textPolicy);

        textPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textPolicy.setTextColor(Color.RED);
                startActivity(new Intent(AboutProgram.this, PrivacyPolicy.class));
            }
        });
    }

    //написать мне
    public void onClickDeveloper(View v) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        // Кому
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "Matatov1989@gmail.com" });
        // тема
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Calendar Clients");
        // текст
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        // отправка!
        startActivity(Intent.createChooser(emailIntent, "e-mail"));
    }

    //написать Некиту
    public void onClickDesigner(View v) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        // Кому
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "Docmat63@gmail.com" });
        // тема
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Calendar Clients");
        // текст
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        // отправка!
        startActivity(Intent.createChooser(emailIntent, "e-mail"));
    }

    //оставить отзыв
    public void onClickSendComment(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.sergeant_matatov.watchsitter&hl"));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AboutProgram.this, SysManagerActivity.class));
    }
}
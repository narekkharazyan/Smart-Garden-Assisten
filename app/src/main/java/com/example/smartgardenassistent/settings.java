package com.example.smartgardenassistent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button buttonOpenWebPage = findViewById(R.id.button_open_web_page);
        buttonOpenWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем страницу настроек WiFi
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://192.168.4.1"));
                startActivity(intent);
            }
        });
    }
}

package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu);
    }
    public void startNewActivity (View v){


        Intent intent = new Intent(this, SignUpORLogIn.class);
        startActivity(intent);
    }
    public void startNewActivity1 (View v){


        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

}
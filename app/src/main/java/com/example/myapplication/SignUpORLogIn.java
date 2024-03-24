package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SignUpORLogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_or_login);
    }

    public void startNewActivity2(View v) {
        Intent intent = new Intent(this, StartMenu.class);
        startActivity(intent);
    }

    public void startNewActivity3(View v) {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }
    public void startNewActivity5(View v) {
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }
}

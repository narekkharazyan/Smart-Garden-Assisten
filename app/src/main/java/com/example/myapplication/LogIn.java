package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    Button login;
    EditText emailEditText,passwordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);
        login = findViewById(R.id.login);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LogIn.this, "Please feel all areas", Toast.LENGTH_SHORT).show();
                }else{
                    signin(email,password);

                }
            }
        });
    }
    private void signin(String email,String password){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,task -> {
            if(task.isSuccessful()){
                homepage();
            }else{
                Toast.makeText(this, "Autetication Faild", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void startNewActivity6(View v) {
        Intent intent = new Intent(this, SignUpORLogIn.class);
        startActivity(intent);
    }
    public void homepage() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
    public void startNewActivity (View v){


        Intent intent = new Intent(this, ForgotPassword.class);
        startActivity(intent);
    }
}

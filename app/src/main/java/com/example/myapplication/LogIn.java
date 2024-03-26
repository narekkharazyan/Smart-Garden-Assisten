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
import com.google.firebase.firestore.FirebaseFirestore;

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
                FirebaseUser user = mAuth.getCurrentUser();
                homepage(user);
            }else{
                Toast.makeText(this, "Autetication Faild", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void startNewActivity6(View v) {
        Intent intent = new Intent(this, SignUpORLogIn.class);
        startActivity(intent);
    }
    public void homepage(FirebaseUser user) {
        try {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User userMetadata = documentSnapshot.toObject(User.class);
                            if (userMetadata != null) {
                                Intent intent = new Intent(LogIn.this, HomePage.class);
                                intent.putExtra("USER", userMetadata);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "User data is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "User document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Error opening login page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void startNewActivity (View v){


        Intent intent = new Intent(this, ForgotPassword.class);
        startActivity(intent);
    }
}

package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {

    Button signUp;
    EditText emailEditText;
    EditText fullnameEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        signUp = findViewById(R.id.create_button);
        fullnameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        if(savedInstanceState != null){
            fullnameEditText.setText(savedInstanceState.getString("fullname"));
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }
        signUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String fullname = fullnameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if any field is empty
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(SignUp.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if email is not valid
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
               /* db.collection("Users").whereEqualTo("email",email).get().addOnCanceledListener(task -> {
                    if (task.isSuccessfully)
                });*/
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, createUserTask -> {
                            if (createUserTask.isSuccessful()) {
                                // User created successfully
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    User newUser = new User(fullname, email, password);
                                    db.collection("users").document(firebaseUser.getUid()).set(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "User data added successfully!");
                                                // Clear input fields after successful signup
                                                fullnameEditText.setText("");
                                                emailEditText.setText("");
                                                passwordEditText.setText("");
                                                // Start the login activity
                                                login();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error adding user data to Firestore", e);
                                                Toast.makeText(SignUp.this, "Failed to add user data to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });

                                } else {
                                    // Firebase user is null
                                    Log.e(TAG, "FirebaseUser is null");
                                    Toast.makeText(SignUp.this, "Failed to create user. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Sign up task failed
                                Log.e(TAG, "Failed to create user", createUserTask.getException());
                                Toast.makeText(SignUp.this, "Failed to create user: " + createUserTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });

    }
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    public void startNewActivity4(View v) {
        Intent intent = new Intent(this, SignUpORLogIn.class);
        startActivity(intent);
    }
    public void login() {
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }
}
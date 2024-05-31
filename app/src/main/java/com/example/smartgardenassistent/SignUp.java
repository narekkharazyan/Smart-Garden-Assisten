package com.example.smartgardenassistent;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    Button signUp;
    EditText emailEditText;
    EditText fullnameEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        signUp = findViewById(R.id.create_button);
        fullnameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.password2);
        if(savedInstanceState != null){
            fullnameEditText.setText(savedInstanceState.getString("fullname"));
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
            passwordEditText.setText(savedInstanceState.getString("confirmPassword"));
        }
        signUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String fullname = fullnameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if any field is empty
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(SignUp.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if email is not valid
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if passwords do not match
                }


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = database.getReference("users");

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, createUserTask -> {
                            if (createUserTask.isSuccessful()) {
                                // User created successfully
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Log.d(TAG, "Email verification sent.");
                                            // Notify the user to check their email for verification
                                            Toast.makeText(SignUp.this, "Registration successful. Please check your email for verification.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.e(TAG, "sendEmailVerification", task2.getException());
                                            Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    User newUser = new User(fullname, email, password);
                                    usersRef.child(firebaseUser.getUid()).setValue(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "User data added successfully!");
                                                // Clear input fields after successful signup
                                                fullnameEditText.setText("");
                                                emailEditText.setText("");
                                                passwordEditText.setText("");
                                                confirmPasswordEditText.setText("");
                                                // Start the login activity
                                                login();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error adding user data to Realtime Database", e);
                                                Toast.makeText(SignUp.this, "Failed to add user data to Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
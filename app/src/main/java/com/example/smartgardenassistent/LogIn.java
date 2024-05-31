package com.example.smartgardenassistent;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && user.isEmailVerified()){
            ProgressDialog progressDialog = new ProgressDialog(LogIn.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        homepage(user);
                    } else {
                        Toast.makeText(LogIn.this, "No such user",
                                Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LogIn.this, "Error checking user role",
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }


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
                if(user != null && user.isEmailVerified()){
                    homepage(user);
                }else{
                    Toast.makeText(this, "Email isn't verified!, Please check your Email", Toast.LENGTH_SHORT).show();
                    user.sendEmailVerification().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Log.d(TAG, "Email verification sent.");
                            // Notify the user to check their email for verification
                            //Toast.makeText(SignUpPage.this, "Registration successful. Please check your email for verification.", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task2.getException());
                            //Toast.makeText(SignUpPage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


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
            FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userMetadata = dataSnapshot.getValue(User.class);
                                if (userMetadata != null) {
                                    Intent intent = new Intent(LogIn.this, HomePage.class);
                                    intent.putExtra("USER", userMetadata);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LogIn.this, "User data is null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LogIn.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LogIn.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(LogIn.this, "Error opening login page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void startNewActivity (View v){


        Intent intent = new Intent(this, ForgotPassword.class);
        startActivity(intent);
    }
}

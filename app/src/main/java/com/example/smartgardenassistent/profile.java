package com.example.smartgardenassistent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class profile extends AppCompatActivity {
    User user;
    TextView fullNameTextView, emailTextView, deleteTextView;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    Button logOut;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = getIntent().getParcelableExtra("USER");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        Toast.makeText(this, "" + user.getFullName(), Toast.LENGTH_SHORT).show();

        fullNameTextView = findViewById(R.id.full_name);
        emailTextView = findViewById(R.id.email);
        logOut = findViewById(R.id.logout);
        deleteTextView = findViewById(R.id.delete);
        imageView = findViewById(R.id.profilePhoto);
        String email = user.getEmail();
        String fullName = user.getFullName();

        fullNameTextView.setText(fullName);
        emailTextView.setText(email);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.child("profileImageUrl").exists()) {
                        String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Use Glide to load the image
                            Glide.with(profile.this)
                                    .load(imageUrl)
                                    .into(imageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("RealtimeDatabase", "Error getting document: ", databaseError.toException());
                }
            });
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            imageView.setImageURI(imageUri);
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });
        deleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }
    private void deleteAccount(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // User clicked OK button. Proceed with account deletion.
            proceedToDeleteAccount();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // User cancelled the dialog. Just dismiss.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void openLoginActivity() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);
    }

    private void proceedToDeleteAccount() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Loading.....");
        dialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("RealtimeDatabase", "User document deleted successfully.");
                            user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("Delete Account", "User account deleted.");
                                    FirebaseAuth.getInstance().signOut();
                                    if(dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    openLoginActivity();
                                } else {
                                    Log.w("Delete Account", "Failed to delete user account.", task.getException());
                                    if(dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("RealtimeDatabase", "Error deleting user document", e);
                            if(dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    });
        }else{
            Log.d("Delete Account", "No user to delete.");
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference fileRef = storageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateUserProfileImage(imageUrl);
                        });
                        Toast.makeText(profile.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(profile.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUserProfileImage(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            progressDialog.setTitle("Updating Profile...");
            progressDialog.show();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.child("profileImageUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Log.d("RealtimeDatabase", "DocumentSnapshot successfully updated!");
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.w("RealtimeDatabase", "Error updating document", e);
                    });
        }
    }
}
package com.example.smartgardenassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class myplants extends AppCompatActivity {
    User user;
    private LinearLayout container;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    Map<String, Object> child = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplants);
        user = getIntent().getParcelableExtra("USER");
        this.container = findViewById(R.id.container);
        Button addChildButton = findViewById(R.id.addChild);
        addChildButton.setOnClickListener(this::showAddChildDialog);
        if (currentUser != null) {
            fetchChildrenAndDisplay();
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchChildrenAndDisplay() {
        if (currentUser == null) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading children...");
        progressDialog.show();

        db.collection("users").document(currentUser.getUid()).collection("plants")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fullName = document.getString("fullName");

                            if (fullName != null) {
                                addChildCard(fullName);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @SuppressLint("SetTextI18n")
    private void addChildCard(String fullName) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);

        TextView fullNameTextView = cardView.findViewById(R.id.textView3);
        fullNameTextView.setText(fullName);

        // Assuming you have another TextView with id textViewDoctor for the doctor's name



        this.container.addView(cardView);







    }

    public void showAddChildDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.addplants, null);
        builder.setView(dialogView);
        EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName2);
        Spinner regions = dialogView.findViewById(R.id.regions);

        EditText editTextDOB = dialogView.findViewById(R.id.editTextDOB);
        editTextDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view1, year, month, dayOfMonth) -> editTextDOB.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, id) -> {});
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view1 -> {
                String fullName = editTextFullName.getText().toString().trim();

                String dob = editTextDOB.getText().toString().trim();



                if (fullName.isEmpty() ||  dob.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {

                   addChildToFirestore(fullName, dob);
                    dialog.dismiss();

                }
            });
        });

        dialog.show();

    }
    private void addChildToFirestore(String fullName, String dob) {
        if (currentUser == null) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Adding plant...");
        progressDialog.show();


        child.put("fullName", fullName);

        child.put("dob", dob);


        db.collection("users").document(currentUser.getUid()).collection("plants").add(child)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Plant added successfully!", Toast.LENGTH_SHORT).show();
                    addChildCard(fullName);
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding plant", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

}
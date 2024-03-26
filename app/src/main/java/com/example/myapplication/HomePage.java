package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomePage extends AppCompatActivity {
    User user;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        user = getIntent().getParcelableExtra("USER");
        Toast.makeText(this, "" + user.getFullName(), Toast.LENGTH_SHORT).show();
        setContentView(R.layout.home_page);

    }

    public void settingspage(View v) {
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }
    public void menupage(View v) {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }
    public void myplants(View v) {
        Intent intent = new Intent(this, myplants.class);
        startActivity(intent);
    }
    public void profile(View v) {

        try{
           /* FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
           FirebaseFirestore.getInstance().collection("users")
                   .document(user.getUid()).addSnapshotListener(
                            (snapshot, e) -> {
                                if (e != null) {
                                    Toast.makeText(this, "cant fetch metadata", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                assert snapshot != null:"data snapshot is null when it is not expected";
*/
                                Intent intent = new Intent(HomePage.this, profile.class);

                                intent.putExtra("USER", user);

                                startActivity(intent);






        }catch (Exception e){
            Toast.makeText(this, "error opening login page: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void weather(View v) {
        Intent intent = new Intent(this, weather.class);
        startActivity(intent);
    }
}
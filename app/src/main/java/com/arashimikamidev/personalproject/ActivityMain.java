package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityMain extends AppCompatActivity {

    private EditText txtSearch;
    private TextView lblNameUser;
    private ImageView imgUser, btnSettings;
    private ListView ltsUser;
    private AdapterFriends adapterFriends;
    private FloatingActionButton fabAddFriend;
    private FirebaseAuth mAuth;
    private FirebaseFirestore dbFirestore;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIComponents();
        loadUserDetails();
        loadFriendsList();

        btnSettings.setOnClickListener(v -> signOut());

        fabAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityAddFriend.class);
            startActivity(intent);
        });

        ltsUser.setOnItemClickListener((parent, view, position, id) -> {
            ClassFriends friend = adapterFriends.getItem(position);
            Intent intent = new Intent(getApplicationContext(), ActivityChat.class);
            intent.putExtra("friend", friend);
            startActivity(intent);
        });
    }

    private void initializeUIComponents() {
        txtSearch = findViewById(R.id.txtSearch);
        lblNameUser = findViewById(R.id.lblNameUser);
        imgUser = findViewById(R.id.imgUser);
        btnSettings = findViewById(R.id.btnSettings);
        ltsUser = findViewById(R.id.listUser);
        fabAddFriend = findViewById(R.id.fabAddFriend);

        mAuth = FirebaseAuth.getInstance();
        dbFirestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_web_client_google))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void loadUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            lblNameUser.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(imgUser);
            }
        }
    }

    private void loadFriendsList() {
        dbFirestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ClassFriends> friendsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("name");
                            String userEmail = document.getString("email");
                            String userUid = document.getString("uid");

                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = user.getEmail();

                            if (!Objects.equals(email, userEmail)) {
                                ClassFriends friend = new ClassFriends(userName, userEmail, null);
                                friendsList.add(friend);
                            }
                        }

                        adapterFriends = new AdapterFriends(ActivityMain.this, friendsList);
                        ltsUser.setAdapter(adapterFriends);
                    } else {
                        Log.w("ActivityMain", "Error getting documents.", task.getException());
                        // Add error handling here
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
            startActivity(intent);
            finish();
        });
    }
}
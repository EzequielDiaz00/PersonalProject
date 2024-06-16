package com.arashimikamidev.personalproject;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityMain extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1;

    private EditText txtSearch;
    private TextView lblNameUser;
    private ImageView imgUser, btnSettings;
    private ListView ltsUser;
    private ClassVeryNet classVeryNet;
    private ClassNotification classNotification;
    private AdapterFriends adapterFriends;
    private FirebaseAuth mAuth;
    private FirebaseFirestore dbFirestore;
    private GoogleSignInClient googleSignInClient;
    private boolean veryNet;
    private List<ClassFriends> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        classNotification = new ClassNotification(this);
        classVeryNet = new ClassVeryNet();
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                classNotification.loadMessages(); // Iniciar la carga continua de mensajes
            }
        } else {
            classNotification.loadMessages(); // Iniciar la carga continua de mensajes
        }

        initializeUIComponents();
        loadUserDetails();
        loadFriendsList();

        btnSettings.setOnClickListener(v -> signOut());

        ltsUser.setOnItemClickListener((parent, view, position, id) -> {
            veryNet = classVeryNet.isNetworkAvailable(this);

            if (!veryNet) {
                Toast.makeText(ActivityMain.this, "No se puede abrir el chat, porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
                return;
            }
            ClassFriends friend = adapterFriends.getItem(position);
            Intent intent = new Intent(getApplicationContext(), ActivityChat.class);
            intent.putExtra("friend", friend);
            startActivity(intent);
        });

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtreFriends();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initializeUIComponents() {
        txtSearch = findViewById(R.id.txtSearch);
        lblNameUser = findViewById(R.id.lblNameUser);
        imgUser = findViewById(R.id.imgUser);
        btnSettings = findViewById(R.id.btnSettings);
        ltsUser = findViewById(R.id.listUser);

        mAuth = FirebaseAuth.getInstance();
        dbFirestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_web_client_google))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void filtreFriends() {
        String searchText = txtSearch.getText().toString().toLowerCase();
        List<ClassFriends> filteredList = new ArrayList<>();

        for (ClassFriends friend : friendsList) {
            if (friend.getUserName().toLowerCase().contains(searchText) ||
                    friend.getUserEmail().toLowerCase().contains(searchText)) {
                filteredList.add(friend);
            }
        }

        adapterFriends.updateList(filteredList);
    }

    private void loadUserDetails() {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityMain.this, "No se pueden mostrar los datos porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            lblNameUser.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl().toString()).into(imgUser);

                String imgData = user.getPhotoUrl().toString();
                Log.d("ActivityMain", "Data foto: " + imgData);
            }
        }
    }

    private void loadFriendsList() {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityMain.this, "No se pueden mostrar los datos porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        dbFirestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("name");
                            String userEmail = document.getString("email");
                            String userPhoto = document.getString("photo");
                            String userUrlPhoto = document.getString("urlPhoto");

                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = user.getEmail();

                            if (!Objects.equals(email, userEmail)) {
                                ClassFriends friend = new ClassFriends(userName, userEmail, userPhoto, userUrlPhoto);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                classNotification.loadMessages();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

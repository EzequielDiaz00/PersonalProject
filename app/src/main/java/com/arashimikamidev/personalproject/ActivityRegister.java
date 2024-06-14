package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    EditText txtUserName, txtEmail, txtPassword, txtPassConfirm;
    Button btnRegister, btnLogin;
    ClassVeryNet classVeryNet;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    FirebaseFirestore dbFirestore;

    private static boolean veryNet;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        classVeryNet = new ClassVeryNet();
        veryNet = classVeryNet.isNetworkAvailable(this);

        loadObjects();

        btnLogin.setOnClickListener(v -> navigateToLoginActivity());

        btnRegister.setOnClickListener(v -> {
            String user = txtUserName.getText().toString();
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            String passConfirm = txtPassConfirm.getText().toString();

            if (validateInputs(user, email, password, passConfirm)) {
                signUpEmail(email, password, user);
            }
        });
    }

    private void loadObjects() {
        txtUserName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPassConfirm = findViewById(R.id.txtConfirm);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        dbFirestore = FirebaseFirestore.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private boolean validateInputs(String user, String email, String password, String passConfirm) {
        if (user.isEmpty()) {
            Toast.makeText(ActivityRegister.this, "Ingrese nombre de usuario", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.isEmpty()) {
            Toast.makeText(ActivityRegister.this, "Ingrese el correo electronico", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(ActivityRegister.this, "Ingrese la contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passConfirm.isEmpty()) {
            Toast.makeText(ActivityRegister.this, "Confirme la contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(passConfirm)) {
            Toast.makeText(ActivityRegister.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signUpEmail(String email, String password, String userName) {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (veryNet == false) {
            Toast.makeText(ActivityRegister.this, "No se puede registrar el usuario porque no hay conexion a internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ActivityRegister", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, userName);
                        }
                    } else {
                        Log.w("ActivityRegister", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(ActivityRegister.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserProfile(FirebaseUser user, String userName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(Uri.parse("null"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ActivityRegister", "User profile updated.");
                        insertDataToFirestore(user.getUid(), user.getDisplayName(), user.getEmail());
                        navigateToMainActivity();
                    } else {
                        Log.d("ActivityRegister", "Error al actualizar perfil", task.getException());
                    }
                });
    }

    private void insertDataToFirestore(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("type", "Email");

        dbFirestore.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        mDatabaseRef.child("users").child(uid).child("chat").setValue("Chat");

                        Log.d("ActivityRegister", "DocumentSnapshot added with ID: " + avoid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ActivityRegister", "Error adding document", e);
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
        startActivity(intent);
    }
}
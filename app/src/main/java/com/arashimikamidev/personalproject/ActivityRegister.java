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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityRegister extends AppCompatActivity {

    EditText txtUserName, txtEmail, txtPassword, txtPassConfirm;
    Button btnRegister, btnLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore dbFirebase;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loadObjects();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = txtUserName.getText().toString();
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                String passConfirm = txtPassConfirm.getText().toString();

                if (user.isEmpty()) {
                    Toast.makeText(ActivityRegister.this, "Ingrese nombre de usuario", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (email.isEmpty()) {
                    Toast.makeText(ActivityRegister.this, "Ingrese el correo electronico", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(ActivityRegister.this, "Ingrese la contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (passConfirm.isEmpty()) {
                    Toast.makeText(ActivityRegister.this, "Confirme la contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.equals(passConfirm)) {
                    SignUpEmail(email, password, user);
                }
            }
        });
    }

    private void loadObjects() {
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPassConfirm = findViewById(R.id.txtConfirm);
        txtUserName = findViewById(R.id.txtName);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
    }

    private void SignUpEmail(String email, String password, String userName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("ActivityRegister", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            updateUserEmail(userName);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("ActivityRegister", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ActivityRegister.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserEmail(String userName) {
        FirebaseUser user = mAuth.getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(Uri.parse("null"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("ActivityRegister", "User profile updated.");

                            Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("ActivityRegister", "Error al actualizar");
                        }
                    }
                });
    }
}
package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ActivityLogin extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private Button btnLogin, btnRegister, btnGoogle;
    private ClassVeryNet classVeryNet;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore dbFirestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    private static final int REQ_GOOGLE_SIGN_IN = 2;
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
        setContentView(R.layout.activity_login);

        classVeryNet = new ClassVeryNet();
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityLogin.this, "No hay conexión a internet. Conéctese a una red", Toast.LENGTH_SHORT).show();
        }

        loadObjects();

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityRegister.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(ActivityLogin.this, "Ingrese el correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(ActivityLogin.this, "Ingrese la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            signInEmail(email, password);
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        loadGoogleSignIn();
    }

    private void loadObjects() {
        txtEmail = findViewById(R.id.txtEmailLogin);
        txtPassword = findViewById(R.id.txtPasswordLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        dbFirestore = FirebaseFirestore.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void signInEmail(String email, String password) {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityLogin.this, "No se puede iniciar sesión porque no hay conexión a internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ActivityLogin", "signInWithEmail:success");
                        navigateToMainActivity();
                    } else {
                        Log.w("ActivityLogin", "signInWithEmail:failure", task.getException());
                        Toast.makeText(ActivityLogin.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGoogleSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_web_client_google))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityLogin.this, "No se puede iniciar sesión porque no hay conexión a internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w("ActivityLogin", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ActivityLogin", "signInWithCredential:success");
                        loadUser();
                        navigateToMainActivity();
                    } else {
                        Log.w("ActivityLogin", "signInWithCredential:failure", task.getException());
                        Toast.makeText(ActivityLogin.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertDataToFirestore(String uid, String name, String email, String type, String photo) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("type", type);
        user.put("photo", photo);

        dbFirestore.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    mDatabaseRef.child("users").child(uid).child("chat").setValue("Chat");
                    Log.d("ActivityLogin", "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> Log.w("ActivityLogin", "Error writing document", e));
    }

    private void updateDataToFirestore(String uid, String photo) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("urlPhoto", photo);

        dbFirestore.collection("users").document(uid)
                .update(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ActivityLogin", "DocumentSnapshot successfully updated!");
                })
                .addOnFailureListener(e -> Log.w("ActivityLogin", "Error updating document", e));
    }

    private void insertDataToStorage(String uid, String photoUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Convert bitmap to byte array
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        // Storage reference for the user's photo
                        StorageReference userRef = storageRef.child(uid);
                        StorageReference userFotosRef = userRef.child("fotosUser/photo.jpg");

                        // Upload byte array to Firebase Storage
                        UploadTask uploadTask = userFotosRef.putBytes(data);

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("ActivityRegister", "Error uploading data to FireStorage: " + exception.getMessage());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Handle successful upload
                                Log.d("ActivityRegister", "Upload successful");
                            }
                        });

                        uploadTask = userFotosRef.putBytes(data);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return userFotosRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();

                                        updateDataToFirestore(uid, downloadUri.toString());
                                    }
                                } catch (Exception ex) {
                                    Log.d("ActivityRegister_insertDataToStorage", "Error al extraer URL de Storage: " + ex.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Remove any references to the target when it's no longer valid
                    }
                });
    }

    private void loadUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String uid = user.getUid();
            String photo = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            insertDataToFirestore(uid, name, email, "Google", photo);
            if (photo != null) {
                insertDataToStorage(uid, photo);
            }
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
        startActivity(intent);
        finish();
    }
}

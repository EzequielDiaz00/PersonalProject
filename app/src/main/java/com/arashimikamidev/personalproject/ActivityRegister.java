package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActivityRegister extends AppCompatActivity {
    ImageView imgUser;
    EditText txtUserName, txtEmail, txtPassword, txtPassConfirm;
    Button btnRegister, btnLogin;
    ProgressBar progressBar;
    ClassVeryNet classVeryNet;
    ClassFoto classFoto;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    FirebaseFirestore dbFirestore;
    FirebaseStorage storage;
    StorageReference storageRef;

    private Bitmap stringImg;
    private String stringUrlImg;
    private static boolean veryNet;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 1;

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
        classFoto = new ClassFoto(ActivityRegister.this);

        veryNet = classVeryNet.isNetworkAvailable(this);

        loadObjects();

        imgUser.setOnClickListener(v -> selectOptionImg());

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
        imgUser = findViewById(R.id.imgUser);
        txtUserName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPassConfirm = findViewById(R.id.txtConfirm);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        dbFirestore = FirebaseFirestore.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    private void selectOptionImg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una opción")
                .setItems(new CharSequence[]{"Abrir Cámara", "Abrir Galería"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Acción para abrir la cámara
                                openCamera();
                                break;
                            case 1:
                                // Acción para abrir la galería
                                openGallery();
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(ActivityRegister.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ActivityRegister.this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            classFoto.iniciarCamara();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                classFoto.iniciarCamara();
            } else {
                Toast.makeText(ActivityRegister.this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                classFoto.iniciarCamara();
            } else {
                Toast.makeText(ActivityRegister.this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(classFoto.stringFoto);
                stringImg = bitmap;
                imgUser.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.d("ClassFoto", "No se pudo tomar la foto: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ActivityRegister.this, "Se canceló la subida de foto", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                stringImg = bitmap;
                imgUser.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        if (!veryNet) {
            Toast.makeText(ActivityRegister.this, "No se puede registrar el usuario porque no hay conexion a internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("ActivityRegister", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            insertDataToStorage(user, userName);
                        }
                    } else {
                        Log.w("ActivityRegister", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(ActivityRegister.this, "No se pudo registrar el nuevo usuario", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateUserProfile(FirebaseUser user, String userName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(Uri.parse(stringUrlImg))
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
        user.put("urlPhoto", stringUrlImg);

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

    private void insertDataToStorage(FirebaseUser user, String userName) {
        String uid = user.getUid();
        Calendar calendar = Calendar.getInstance();
        double date = calendar.getTimeInMillis();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stringImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                        stringUrlImg = downloadUri.toString();
                        updateUserProfile(user, userName);
                    }
                } catch (Exception ex) {
                    Log.d("ActivityRegister_insertDataToStorage", "Error al extraer URL de Storage: " + ex.getMessage());
                }
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
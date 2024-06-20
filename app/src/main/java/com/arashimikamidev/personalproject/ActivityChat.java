package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityChat extends AppCompatActivity {

    TextView lblFriendName;
    ImageView imgFriend;
    EditText txtMsg;
    Button btnSend, imgCapture, imgGalery;
    RecyclerView rcChat;
    ClassVeryNet classVeryNet;
    ClassFoto classFoto;
    List<ClassChat> classChats;
    AdapterChat adapterChat;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    FirebaseFirestore dbFirestore;
    FirebaseStorage storage;
    StorageReference storageRef;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    private String lblFriendEmail;
    private static boolean veryNet;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        classVeryNet = new ClassVeryNet();
        classFoto = new ClassFoto(ActivityChat.this);

        veryNet = classVeryNet.isNetworkAvailable(this);

        loadObjects();
        loadFriends();
        loadMessages();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("null");
            }
        });

        imgCapture.setOnClickListener(v -> selectOptionImg());
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

    private void loadObjects() {
        rcChat = findViewById(R.id.rcChat);
        rcChat.setLayoutManager(new LinearLayoutManager(this));
        lblFriendName = findViewById(R.id.lblNameFriend);
        imgFriend = findViewById(R.id.imgFriend);
        txtMsg = findViewById(R.id.txtMsg);
        btnSend = findViewById(R.id.btnSendM);
        imgCapture = findViewById(R.id.btnCapture);
        //imgGalery = findViewById(R.id.btnGalery);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        dbFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        classChats = new ArrayList<>();
        adapterChat = new AdapterChat(this, classChats);
        rcChat.setAdapter(adapterChat);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id_web_client_google))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void loadFriends() {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityChat.this, "No se pueden mostar los datos porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        ClassFriends friends = (ClassFriends) getIntent().getSerializableExtra("friend");

        if (friends != null) {
            lblFriendName.setText(friends.getUserName());
            lblFriendEmail = friends.getUserEmail();
            Glide.with(this).load(friends.getUserFoto()).into(imgFriend);
        }

        dbFirestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ClassFriends> friendsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("ActivityChat", document.getId() + " => " + document.getData());

                                String userName = document.getString("name");
                                String userEmail = document.getString("email");

                                if (lblFriendName.toString().equals(userName)) {
                                    Log.d("ActivityChat", "Name User: " + userName);
                                }
                            }
                        } else {
                            Log.w("ActivityChat", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void sendMessage(String mFoto) {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityChat.this, "No se puede enviar el mensaje porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        ClassFriends friends = (ClassFriends) getIntent().getSerializableExtra("friend");

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && friends != null) {
            String keyMsg = mDatabaseRef.push().getKey();

            String uid = user.getUid();
            String nameReceptor = friends.getUserName();
            String emailReceptor = friends.userEmail;
            String nameEmisor = user.getDisplayName();
            String emailEmisor = user.getEmail();

            ClassDate classDate = new ClassDate();
            classDate.obtenerHora();
            classDate.obtenerFecha();

            String hora = classDate.horaAndroid;
            String fecha = classDate.fechaAndroid;

            Log.d("ActivityChat", "Date: " + hora);

            String txtMessage = txtMsg.getText().toString();

            if (!Objects.equals(mFoto, "null")) {
                txtMessage = "Foto";
            }

            HashMap<String, Object> mensajeMap = new HashMap<>();
            mensajeMap.put("nEmisor", nameEmisor);
            mensajeMap.put("emisor", emailEmisor);
            mensajeMap.put("nReceptor", nameReceptor);
            mensajeMap.put("receptor", emailReceptor);
            mensajeMap.put("msg", txtMessage);
            mensajeMap.put("img", mFoto);
            mensajeMap.put("hora", hora);
            mensajeMap.put("fecha", fecha);

            if (keyMsg != null) {
                mDatabaseRef.child("chats").child(keyMsg).setValue(mensajeMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    txtMsg.setText("");

                                } else {
                                    //
                                }
                            }
                        });
            }
        } else {
            //
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(ActivityChat.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ActivityChat.this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            classFoto.iniciarCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                classFoto.iniciarCamara();
            } else {
                Toast.makeText(ActivityChat.this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                classFoto.iniciarCamara();
            } else {
                Toast.makeText(ActivityChat.this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ejecuteStorage(Bitmap stringImg, FirebaseUser user) {
        String uid = user.getUid();
        Calendar calendar = Calendar.getInstance();
        double date = calendar.getTimeInMillis();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stringImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Storage reference for the user's photo

        StorageReference userRef = storageRef.child(uid);
        StorageReference userFotosRef = userRef.child("fotosChat/" + date);

        // Upload byte array to Firebase Storage
        UploadTask uploadTask = userFotosRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("ActivityChat", "Error uploading data to FireStorage: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful upload
                Log.d("ActivityChat", "Upload successful");
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

                        if (downloadUri != null) {
                            sendMessage(downloadUri.toString());
                        }
                    }
                } catch (Exception ex) {
                    Log.d("ActivityChat_insertDataToStorage", "Error al extraer URL de Storage: " + ex.getMessage());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(classFoto.stringFoto);

                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    ejecuteStorage(bitmap, user);
                }

            } catch (Exception e) {
                Log.d("ClassFoto", "No se pudo tomar la foto: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ActivityChat.this, "Se canceló la subida de foto", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    ejecuteStorage(bitmap, user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages() {
        veryNet = classVeryNet.isNetworkAvailable(this);

        if (!veryNet) {
            Toast.makeText(ActivityChat.this, "No se pueden mostar los datos porque no hay conexion a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            mDatabaseRef.child("chats")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            classChats.clear();

                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                String emisor = messageSnapshot.child("emisor").getValue(String.class);
                                String receptor = messageSnapshot.child("receptor").getValue(String.class);
                                String msg = messageSnapshot.child("msg").getValue(String.class);
                                String img = messageSnapshot.child("img").getValue(String.class);
                                String fecha = messageSnapshot.child("fecha").getValue(String.class);
                                String hora = messageSnapshot.child("hora").getValue(String.class);

                                // Verificar si alguno de los valores es nulo antes de proceder
                                if (emisor != null && receptor != null) {
                                    if (Objects.equals(user.getEmail(), emisor) && Objects.equals(lblFriendEmail, receptor)) {
                                        classChats.add(new ClassChat(null, msg, null, fecha + " - " + hora, img));
                                    } else if (Objects.equals(user.getEmail(), receptor) && Objects.equals(lblFriendEmail, emisor)) {
                                        classChats.add(new ClassChat(msg, null, fecha + " - " + hora, null, img));
                                    }
                                } else {
                                    Log.e("firebase", "Message data is incomplete: " + messageSnapshot.getKey());
                                }
                            }

                            // Notificar al adaptador que los datos han cambiado
                            adapterChat.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("firebase", "Error loading messages", error.toException());
                        }
                    });
        } else {
            Log.e("firebase", "User is null, unable to load messages");
        }
    }
}
package com.arashimikamidev.personalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityChat extends AppCompatActivity {

    TextView lblFriendName;
    ImageView imgFriend;
    EditText txtMsg;
    Button btnSend;
    RecyclerView rcChat;
    List<ClassChat> classChats;
    AdapterChat adapterChat;
    DatabaseReference mDatabaseRef;
    FirebaseAuth mAuth;
    FirebaseFirestore dbFirestore;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    private String lblFriendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        loadObjects();
        loadFriends();
        loadMessages();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadObjects() {
        rcChat = findViewById(R.id.rcChat);
        rcChat.setLayoutManager(new LinearLayoutManager(this));
        lblFriendName = findViewById(R.id.lblNameFriend);
        imgFriend = findViewById(R.id.imgFriend);
        txtMsg = findViewById(R.id.txtMsg);
        btnSend = findViewById(R.id.btnSendM);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        dbFirestore = FirebaseFirestore.getInstance();

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

        ClassFriends friends = (ClassFriends) getIntent().getSerializableExtra("friend");

        if (friends != null) {
            lblFriendName.setText(friends.getUserName());
            lblFriendEmail = friends.getUserEmail();
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

    private void sendMessage() {
        ClassFriends friends = (ClassFriends) getIntent().getSerializableExtra("friend");

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && friends != null) {
            String keyMsg = mDatabaseRef.push().getKey();

            String uid = user.getUid();
            String emailReceptor = friends.userEmail;
            String emailEmisor = user.getEmail();
            String txtMessage = txtMsg.getText().toString();

            HashMap<String, Object> mensajeMap = new HashMap<>();
            mensajeMap.put("emisor", emailEmisor);
            mensajeMap.put("receptor", emailReceptor);
            mensajeMap.put("msg", txtMessage);

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

    private void loadMessages() {
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

                                // Verificar si alguno de los valores es nulo antes de proceder
                                if (emisor != null && receptor != null && msg != null) {
                                    if (Objects.equals(user.getEmail(), emisor) && Objects.equals(lblFriendEmail, receptor)) {
                                        classChats.add(new ClassChat(null, msg));
                                    } else if (Objects.equals(user.getEmail(), receptor) && Objects.equals(lblFriendEmail, emisor)) {
                                        classChats.add(new ClassChat(msg, null));
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
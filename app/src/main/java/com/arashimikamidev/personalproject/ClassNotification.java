package com.arashimikamidev.personalproject;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class ClassNotification {
    private final Context context;
    private final ClassVeryNet classVeryNet;
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabaseRef;
    private static final String CHANNEL_ID = "CHANNEL_ID_NOTIFICATION";

    public ClassNotification(Context context) {
        this.context = context;
        classVeryNet = new ClassVeryNet();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public void loadMessages() {
        boolean veryNet = classVeryNet.isNetworkAvailable(context);

        if (!veryNet) {
            Log.d("ClassNotification", "No hay internet");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            mDatabaseRef.child("chats")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            DataSnapshot lastMessageSnapshot = null;

                            String nameEmisor = null;
                            String emisor = null;
                            String receptor = null;
                            String msg = null;

                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                lastMessageSnapshot = messageSnapshot;

                                nameEmisor = messageSnapshot.child("nEmisor").getValue(String.class);
                                emisor = messageSnapshot.child("emisor").getValue(String.class);
                                receptor = messageSnapshot.child("receptor").getValue(String.class);
                                msg = messageSnapshot.child("msg").getValue(String.class);

                                if (emisor != null && receptor != null && msg != null) {
                                    if (Objects.equals(user.getEmail(), emisor)) {
                                        // Manejar el mensaje del emisor
                                    } else if (Objects.equals(user.getEmail(), receptor)) {
                                        //
                                    }
                                } else {
                                    Log.e("firebase", "Message data is incomplete: " + messageSnapshot.getKey());
                                }
                            }

                            if (lastMessageSnapshot != null) {
                                String lastMsg = lastMessageSnapshot.child("msg").getValue(String.class);
                                String lastEmi = lastMessageSnapshot.child("emisor").getValue(String.class);
                                Log.d("firebase", "Último emisor: " + lastEmi);
                                Log.d("firebase", "Último mensaje: " + lastMsg);

                                if (!Objects.equals(lastEmi, user.getEmail())) {
                                    Log.d("firebase", "Último emisor email: " + user.getEmail());
                                    showNotificationMessage(nameEmisor, emisor, msg);
                                }
                            }
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

    public void showNotificationMessage(String user, String email, String msg) {
        ClassFriends friend = new ClassFriends(user, email, null);
        Intent intent = new Intent(context, ActivityMain.class);
        intent.putExtra("friend", friend);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_appicon_3)
                .setContentTitle("Nuevo mensaje")
                .setContentText(user + ": " + msg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(user)
                        .bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, user, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(msg);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(1, builder.build());
        }
    }

    public void showNotification() {
        Intent intent = new Intent(context, ActivityChat.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_appicon_3)
                .setContentTitle("Merequetengue")
                .setContentText("Bienvenido a mi app")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Merequetengue", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Bienvenido a mi app");
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(2, builder.build());
        }
    }
}

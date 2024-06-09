package com.arashimikamidev.personalproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClassFoto {
    private Activity activity;
    private ImageView imgFoto;
    public String stringFoto;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    public ClassFoto(Activity activity, ImageView imgFoto) {
        this.activity = activity;
        this.imgFoto = imgFoto;
    }

    public void tomarFoto() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            iniciarCamara();
        }
    }

    private void iniciarCamara() {
        Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fotoUser = null;
        try {
            fotoUser = crearImagen();
            if (fotoUser != null) {
                Uri uri = FileProvider.getUriForFile(activity,
                        "com.arashimikamidev.personalproject.fileprovider", fotoUser);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                activity.startActivityForResult(tomarFotoIntent, 1);
            } else {
                Toast.makeText(activity, "No se pudo iniciar la camara", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("ClassFoto", "Error al iniciar la camara: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File crearImagen() throws Exception {
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "imagen_" + fechaHoraMs + "_";
        File dirAlmacenamiento = activity.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if (dirAlmacenamiento != null && !dirAlmacenamiento.exists()) {
            dirAlmacenamiento.mkdirs();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        stringFoto = image.getAbsolutePath();

        return image;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamara();
            } else {
                Toast.makeText(activity, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                Bitmap imagenBitmap = BitmapFactory.decodeFile(stringFoto);
                imgFoto.setImageBitmap(imagenBitmap);
            } catch (Exception e) {
                Log.d("ClassFoto", "No se pudo tomar la foto: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Toast.makeText(activity, "Se canceló la captura de cámara", Toast.LENGTH_SHORT).show();
        }
    }
}
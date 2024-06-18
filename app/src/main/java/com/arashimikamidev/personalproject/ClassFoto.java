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
    public String stringFoto;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public ClassFoto(Activity activity) {
        this.activity = activity;
    }

    public void iniciarCamara() {
        Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fotoUser = null;
        try {
            fotoUser = crearImagen();
            if (fotoUser != null) {
                Uri uri = FileProvider.getUriForFile(activity,
                        "com.arashimikamidev.personalproject.fileprovider", fotoUser);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                activity.startActivityForResult(tomarFotoIntent, REQUEST_IMAGE_CAPTURE);
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
}
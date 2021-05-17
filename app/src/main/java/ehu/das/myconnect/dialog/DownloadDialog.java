package ehu.das.myconnect.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import ehu.das.myconnect.R;

public class DownloadDialog extends DialogFragment {

    private String fileName;
    private String fileText;
    private static final int COD_NUEVO_FICHERO = 40;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            fileName = bundle.getString("fileName");
            fileText = bundle.getString("fileText");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialog_download, null);

        //Para descargar la receta nuestro telefono
        ImageView archivos = vista.findViewById(R.id.imagenArchivos);
        archivos.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                startActivityForResult(intent, COD_NUEVO_FICHERO);
            }
        });

        //Para subir la receta a drive
        ImageView drive = vista.findViewById(R.id.imagenDrive);
        drive.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                //requestSignIn();
                //createFile();
            }
        });

        Button volver = vista.findViewById(R.id.buttonVolverDescargas);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Para la descarga de la receta en el telefono
        if (requestCode == COD_NUEVO_FICHERO && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "w");
                    OutputStreamWriter ficheroexterno = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()));
                    ficheroexterno.write(fileText);
                    ficheroexterno.close();
                    pfd.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            NotificationManager elManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(getActivity(), "IdCanal");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
                elBuilder.setSmallIcon(R.drawable.descarga)
                        //.setContentTitle(getText(R.string.recetaDescargada))
                        //.setContentText(getString(R.string.notiLaReceta)+" '"+recetaNombre+"' "+getString(R.string.notiDescarga))
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true);
                elCanal.enableLights(true);
                elManager.createNotificationChannel(elCanal);
            }
            elManager.notify(1, elBuilder.build());
            //Para crear la receta en drive - Inicio de sesión
        }
    }
}
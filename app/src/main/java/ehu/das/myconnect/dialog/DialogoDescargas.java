package ehu.das.myconnect.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class DialogoDescargas extends DialogFragment {

    private static final int COD_NUEVO_FICHERO = 40;
    private String recetaNombre;
    private String ingredientes;
    private String pasos;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    //private DriveServiceHelper mDriveServiceHelper;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            recetaNombre = bundle.getString("nombreReceta");
            ingredientes = bundle.getString("ingredientes");
            pasos = bundle.getString("pasos");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_descargas, null);

        //Para descargar la receta nuestro telefono
        ImageView archivos = vista.findViewById(R.id.imagenArchivos);
        archivos.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, recetaNombre.replaceAll("\\s+", "")+".txt");
                startActivityForResult(intent, COD_NUEVO_FICHERO);
            }
        });

        //Para subir la receta a drive
        ImageView drive = vista.findViewById(R.id.imagenDrive);
        drive.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                requestSignIn();
                createFile();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void idioma(String idioma) {
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration configuration = getActivity().getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context = getActivity().getBaseContext().createConfigurationContext(configuration);
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
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
                    ficheroexterno.write("Nombre de la receta: "+recetaNombre+"\n");
                    ficheroexterno.write("Ingredientes: "+ingredientes+"\n");
                    ficheroexterno.write("Pasos a seguir: "+pasos+"\n");
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
                        .setContentTitle(getText(R.string.recetaDescargada))
                        .setContentText(getString(R.string.notiLaReceta)+" '"+recetaNombre+"' "+getString(R.string.notiDescarga))
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true);
                elCanal.enableLights(true);
                elManager.createNotificationChannel(elCanal);
            }
            elManager.notify(1, elBuilder.build());
            //Para crear la receta en drive - Inicio de sesión
        } else if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            handleSignInResult(data);
        }
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(getActivity(), signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    getActivity(), Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    new NetHttpTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                });
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createFile() {
        if (mDriveServiceHelper != null) {

            mDriveServiceHelper.createFile()
                    .addOnSuccessListener(this::saveFile)
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create file.", exception));
        }
    }

    /**
     * Saves the currently opened file created via {@link #createFile()} if one exists.
     */
    private void saveFile(String mOpenFileId) {
        if (mDriveServiceHelper != null && mOpenFileId != null) {

            String fileContent = "Nombre de la receta: "+recetaNombre+"\n" + "Ingredientes: "+ingredientes+"\n" + "Pasos a seguir: "+pasos+"\n";

            mDriveServiceHelper.saveFile(mOpenFileId, recetaNombre.replaceAll("\\s+", ""), fileContent)
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }
}

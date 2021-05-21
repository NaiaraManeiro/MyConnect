package ehu.das.myconnect.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.interfaces.ILoading;
import ehu.das.myconnect.fragment.Server;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.fragment.ServerManagmentFragment;
import ehu.das.myconnect.interfaces.OnDialogDismiss;
import ehu.das.myconnect.interfaces.OnDialogOptionPressed;
import ehu.das.myconnect.service.SSHConnectionWorker;

public class DialogAccessPem extends DialogFragment {

    public View v;
    public OnDialogOptionPressed<String> scriptAddListener;
    public boolean recreate = false;
    public ServerManagmentFragment serverManagmentFragment;
    public int position;
    public ServerListFragment serverListFragment;
    public ILoading loadingListener;
    private final int PICKFILE_RESULT_CODE = 10;
    private String key = "";
    private String error = "";
    public OnDialogDismiss<String> onDialogDismiss;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Dialogo para acceder al servidor mediante un archivo pem
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getResources().getString(R.string.accessPem));
        alert.setIcon(R.drawable.password);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loginServer = inflater.inflate(R.layout.access_pem_dialog, null);
        Button pemButton = loginServer.findViewById(R.id.pemButton);
        //Para obtener el archivo pem
        pemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_PICK);
                chooseFile.setType("*/*");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });
        alert.setPositiveButton(getResources().getString(R.string.access), new DialogInterface.OnClickListener() {
            // Realiza la conexión
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!key.equals("")) {
                    dismiss();
                    loadingListener.startLoading();
                    Server old = ServerListFragment.selectedServer;
                    if (recreate) {
                        ServerListFragment.selectedServer = ServerListFragment.serverList.get(position); }
                    Data data = new Data.Builder()
                            .putString("user", ServerListFragment.selectedServer.getUser())
                            .putString("host", ServerListFragment.selectedServer.getHost())
                            .putString("password", key)
                            .putInt("port", ServerListFragment.selectedServer.getPort())
                            .putBoolean("keyPem", true)
                            .build();
                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHConnectionWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    if (!status.getOutputData().getString("result").equals("")) {
                                        ServerListFragment.connection = null;
                                        scriptAddListener.onYesPressed("fail","");
                                        ServerListFragment.selectedServer = old;
                                        onDialogDismiss.onDismiss(error);
                                        dismiss();
                                    } else {
                                        scriptAddListener.onYesPressed("succesful","");
                                        ServerListFragment.selectedServer.setPassword(key);
                                        if (!recreate) {
                                            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment);
                                        } else {
                                            recreate = false;
                                            ServerListFragment.selectedServer = ServerListFragment.serverList.get(position);
                                            serverManagmentFragment.recreateFragment();
                                        }
                                    }
                                    loadingListener.stopLoading();
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        alert.setView(loginServer);
        return alert.create();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Recibe la ubicación del archivo pem
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();

                String uri2 = getPath(getContext(), uri);

                if (uri2 != null) {
                    if (uri2.contains(".pem")) {
                        key = uri2;
                    } else {
                        error = "noPem";
                    }
                } else {
                    error = "noFile";
                }
            }
        }
    }
    @Nullable
    public static String getPath(Context context, Uri uri) {
        // Convierte una uri en un path
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);

        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        // Utiliza content resolver para conocer el path a partir de una uri
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            } else {
                return null;
            }
        }
        return null;
    }
}

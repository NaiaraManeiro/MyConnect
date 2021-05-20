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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.LoginFragment;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.ServerWorker;

public class DialogPem extends DialogFragment {

    private String serverName;
    private String user;
    private String host;
    private int port;
    public View view;
    public OnDialogDismiss<String> onDialogDismiss;
    private final int PICKFILE_RESULT_CODE = 10;
    private String key = "";
    private String error = "";
    public ILoading loadingListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.editWithPem));
        builder.setIcon(R.drawable.password);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.access_pem_dialog, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            serverName = bundle.getString("serverName");
            user = bundle.getString("user");
            host = bundle.getString("host");
            port = bundle.getInt("port");
        }

        String oldServerName = ServerListFragment.selectedServer.getName();
        String username = LoginFragment.username;

        Button pemButton = vista.findViewById(R.id.pemButton);

        //Para obtener el archivo pem
        pemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_PICK);
                chooseFile.setType("*/*");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        builder.setPositiveButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingListener.startLoading();
                //Editamos los datos a la bd en caso de que se pueda realizar el ssh
                Data datos = new Data.Builder()
                        .putString("action", "editServer")
                        .putString("user", user)
                        .putString("host", host)
                        .putInt("port", port)
                        .putString("serverName", serverName)
                        .putString("oldServerName", oldServerName)
                        .putString("userName", username)
                        .putString("password", key)
                        .build();

                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                        .setInputData(datos)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                String result = status.getOutputData().getString("result");
                                loadingListener.stopLoading();
                                if (error.equals("")) {
                                    onDialogDismiss.onDismiss(result);
                                } else {
                                    onDialogDismiss.onDismiss(error);
                                }
                                dismiss();
                            }
                        });
                WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);

        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
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
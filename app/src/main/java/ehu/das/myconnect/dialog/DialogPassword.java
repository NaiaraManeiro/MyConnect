package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.LoginFragment;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.ServerWorker;

public class DialogPassword extends DialogFragment {

    private String serverName;
    private String user;
    private String host;
    private int port;
    public View view;
    public OnDialogDismiss<String> onDialogDismiss;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.editWithPassword));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.access_password_layout, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            serverName = bundle.getString("serverName");
            user = bundle.getString("user");
            host = bundle.getString("host");
            port = bundle.getInt("port");
        }

        String oldServerName = ServerListFragment.selectedServer.getName();
        String username = LoginFragment.username;

        builder.setPositiveButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText contraServidor = vista.findViewById(R.id.passwordAccess);
                String contra = contraServidor.getText().toString();

                //Editamos los datos a la bd en caso de que se pueda realizar el ssh
                Data datos = new Data.Builder()
                        .putString("action", "editServer")
                        .putString("user", user)
                        .putString("host", host)
                        .putInt("port", port)
                        .putString("serverName", serverName)
                        .putString("oldServerName", oldServerName)
                        .putString("userName", username)
                        .putString("password", contra)
                        .build();

                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                        .setInputData(datos)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                String result = status.getOutputData().getString("result");
                                onDialogDismiss.onDismiss(result);
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
}


package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
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
import ehu.das.myconnect.service.ServerWorker;

public class PasswordDialog extends DialogFragment {

    private String serverName;
    private String user;
    private String host;
    private int port;
    private String userName;
    private String oldServerName;
    private String action;
    public View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vw = inflater.inflate(R.layout.dialogo_contrasena, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            serverName = bundle.getString("serverName");
            user = bundle.getString("user");
            host = bundle.getString("host");
            port = bundle.getInt("port");
            userName = bundle.getString("userName");
            oldServerName = bundle.getString("oldServerName");
            action = bundle.getString("action");
        }

        Button acept = vw.findViewById(R.id.aceptContra);
        acept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText serverPassword = vw.findViewById(R.id.contraDialog);
                String password = serverPassword.getText().toString();

                if (action != null && action.equals("conectServer")) {
                    Data data = new Data.Builder()
                            .putString("action", "conectServer")
                            .putString("user", user)
                            .putString("host", host)
                            .putString("password", password)
                            .putInt("port", port)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    if (result.equals("authFail")) {
                                        Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                                    } else if (result.equals("failConnect")) {
                                        Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                                    } else {
                                        dismiss();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("user", user);
                                        bundle.putString("host", host);
                                        bundle.putString("password", password);
                                        bundle.putInt("port", port);
                                        bundle.putString("serverName", serverName); //Para saber cual es el server seleccionado
                                        Navigation.findNavController(view).navigate(R.id.action_serverListFragment_to_filesFragment, bundle);
                                    }
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                } else {
                    //Editamos los datos a la bd en caso de que se pueda realizar el ssh
                    Data data = new Data.Builder()
                            .putString("action", "editServer")
                            .putString("user", user)
                            .putString("host", host)
                            .putInt("port", port)
                            .putString("serverName", serverName)
                            .putString("oldServerName", oldServerName)
                            .putString("password", password)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    if (result.equals("Error")) {
                                        Toast.makeText(getContext(), getString(R.string.servidorExistente), Toast.LENGTH_SHORT).show();
                                    } else if (result.equals("authFail")) {
                                        Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                                    } else if (result.equals("failConnect")) {
                                        Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.servidorEditado), Toast.LENGTH_SHORT).show();
                                    }
                                    dismiss();
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        Button back = vw.findViewById(R.id.volverContra);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vw);

        return builder.create();
    }
}

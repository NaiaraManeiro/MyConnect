package ehu.das.myconnect.dialog;

import android.app.Activity;
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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.service.ServerWorker;

public class DialogoContrasena extends DialogFragment {

    private String nombreServer;
    private String usuario;
    private String host;
    private int puerto;
    private String nombreUsuario;
    private String nombreServerViejo;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_contrasena, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            nombreServer = bundle.getString("nombreServer");
            usuario = bundle.getString("usuario");
            host = bundle.getString("host");
            puerto = bundle.getInt("puerto");
            nombreUsuario = bundle.getString("nombreUsuario");
            nombreServerViejo = bundle.getString("nombreServerViejo");
        }

        Button acept = vista.findViewById(R.id.aceptContra);
        acept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText contraServidor = vista.findViewById(R.id.contraDialog);
                String contra = contraServidor.getText().toString();
                //Editamos los datos a la bd en caso de que se pueda realizar el ssh
                Data datos = new Data.Builder()
                        .putString("funcion", "editServer")
                        .putString("usuario", usuario)
                        .putString("host", host)
                        .putInt("puerto", puerto)
                        .putString("nombreServidor", nombreServer)
                        .putString("nombreServidorViejo", nombreServerViejo)
                        .putString("nombreUsuario", nombreUsuario)
                        .putString("contrasena", contra)
                        .build();

                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                        .setInputData(datos)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                String result = status.getOutputData().getString("resultado");
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
        });

        Button volver = vista.findViewById(R.id.volverContra);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        return builder.create();
    }
}

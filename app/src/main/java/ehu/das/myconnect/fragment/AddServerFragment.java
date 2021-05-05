package ehu.das.myconnect.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.service.ServerWorker;


public class AddServerFragment extends Fragment {

    private String nombreUsuario = "Naiara";
    private Switch keyPemSwitch;
    private int PICKFILE_RESULT_CODE = 12;
    private String key;

    public AddServerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_server, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*Bundle extras = this.getArguments();
        if (extras != null) {
            nombreUsuario = extras.getString("nombreUsuario");
        }*/

        EditText contrasenaCaja = getActivity().findViewById(R.id.contrasena);
        keyPemSwitch = getActivity().findViewById(R.id.keyPem);
        Button keyPemButton = getActivity().findViewById(R.id.keyPemButton);
        keyPemButton.setEnabled(false);

        keyPemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    keyPemButton.setEnabled(true);
                    contrasenaCaja.setEnabled(false);
                } else {
                    keyPemButton.setEnabled(false);
                    contrasenaCaja.setEnabled(true);
                }
            }
        });

        keyPemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        EditText usuarioCaja = getActivity().findViewById(R.id.usuario);
        EditText hostCaja = getActivity().findViewById(R.id.host);
        EditText puertoCaja = getActivity().findViewById(R.id.puerto);
        EditText servidorCaja = getActivity().findViewById(R.id.nombreServidor);

        Button anadir = getActivity().findViewById(R.id.anadirServidor);
        anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = usuarioCaja.getText().toString();
                String host = hostCaja.getText().toString();
                int puerto = Integer.parseInt(puertoCaja.getText().toString());
                String contrasena = contrasenaCaja.getText().toString();
                String servidor = servidorCaja.getText().toString();

                //Validamos los datos
                if (usuario.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.usuarioVacio), Toast.LENGTH_SHORT).show();
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches()) {
                    Toast.makeText(getContext(), getString(R.string.hostPattern), Toast.LENGTH_SHORT).show();
                    hostCaja.setText("");
                } else if (contrasena.equals("") && !keyPemSwitch.isChecked()) {
                    Toast.makeText(getContext(), getString(R.string.contraVacia), Toast.LENGTH_SHORT).show();
                } else if (servidor.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.servidorVacio), Toast.LENGTH_SHORT).show();
                } else if (servidor.length() > 20) {
                    Toast.makeText(getContext(), getString(R.string.servidorLargo), Toast.LENGTH_SHORT).show();
                } else {
                    Boolean keyPem = keyPemSwitch.isChecked();

                    if (keyPem) {
                        contrasena = key;
                    }

                    //Añadimos los datos a la bd en caso de que se pueda realizar el ssh
                    Data datos = new Data.Builder()
                            .putString("funcion", "addServer")
                            .putString("usuario", usuario)
                            .putString("host", host)
                            .putString("contrasena", contrasena)
                            .putInt("puerto", puerto)
                            .putString("nombreServidor", servidor)
                            .putString("nombreUsuario", nombreUsuario)
                            .putBoolean("keyPem", keyPem)
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
                                        servidorCaja.setText("");
                                    } else if (result.equals("authFail")) {
                                        Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.servidorCreado), Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(v).popBackStack();
                                    }
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Para obtener el archivo pem
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                File filePem = new File(uri.getPath());
                key = filePem.getName();
                /*try {
                    key = new String(Files.readAllBytes(filePem.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }

        }
    }
}
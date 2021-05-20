package ehu.das.myconnect.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.service.ServerWorker;


public class AddServerFragment extends Fragment {

    private Switch keyPemSwitch;
    private final int PICKFILE_RESULT_CODE = 12;
    private String key;
    private int passwordPem = 0;
    private boolean conexionCheck = false;
    public LoadingDialog loadingDialog;

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

        CheckBox conexion = getActivity().findViewById(R.id.checkBox);
        conexion.setText(getString(R.string.conexion));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("server_connnect", true)) {
            conexion.setChecked(true);
        }

        EditText passwordBox = getActivity().findViewById(R.id.contrasena);
        keyPemSwitch = getActivity().findViewById(R.id.keyPem);
        Button keyPemButton = getActivity().findViewById(R.id.pemButton);
        keyPemButton.setEnabled(false);

        keyPemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    keyPemButton.setEnabled(true);
                    passwordBox.setEnabled(false);
                } else {
                    keyPemButton.setEnabled(false);
                    passwordBox.setEnabled(true);
                }
            }
        });

        //Para obtener el archivo pem
        keyPemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_PICK);
                chooseFile.setType("*/*");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        EditText userBox = getActivity().findViewById(R.id.usuario);
        EditText hostBox = getActivity().findViewById(R.id.host);
        EditText portBox = getActivity().findViewById(R.id.puerto);
        EditText serverBox = getActivity().findViewById(R.id.nombreServidor);

        Button add = getActivity().findViewById(R.id.anadirServidor);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userBox.getText().toString();
                String host = hostBox.getText().toString();
                int port = Integer.parseInt(portBox.getText().toString());
                String password = passwordBox.getText().toString();
                String server = serverBox.getText().toString();

                //Validamos los datos
                if (user.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.usuarioVacio), Toast.LENGTH_SHORT).show();
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches()) {
                    Toast.makeText(getContext(), getString(R.string.hostPattern), Toast.LENGTH_SHORT).show();
                    hostBox.setText("");
                } else if (password.equals("") && !keyPemSwitch.isChecked()) {
                    Toast.makeText(getContext(), getString(R.string.contraVacia), Toast.LENGTH_SHORT).show();
                } else if (server.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.servidorVacio), Toast.LENGTH_SHORT).show();
                } else if (server.length() > 20) {
                    Toast.makeText(getContext(), getString(R.string.servidorLargo), Toast.LENGTH_SHORT).show();
                } else {
                    loadingDialog = new LoadingDialog();
                    loadingDialog.setCancelable(false);
                    loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");

                    boolean keyPem = keyPemSwitch.isChecked();

                    if (keyPem) {
                        password = key;
                        passwordPem = 1;
                    }

                    boolean checked = conexion.isChecked();

                    if (checked) {
                        conexionCheck = true;
                    }

                    //Añadimos los datos a la bd en caso de que se pueda realizar el ssh
                    Data data = new Data.Builder()
                            .putString("action", "addServer")
                            .putString("user", user)
                            .putString("host", host)
                            .putString("password", password)
                            .putInt("port", port)
                            .putString("serverName", server)
                            .putString("userName", LoginFragment.username)
                            .putBoolean("keyPem", keyPem)
                            .putInt("passwordPem", passwordPem)
                            .putBoolean("conexion", conexionCheck)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    loadingDialog.dismiss();
                                    if (result.equals("Error")) {
                                        Toast.makeText(getContext(), getString(R.string.servidorExistente), Toast.LENGTH_SHORT).show();
                                        serverBox.setText("");
                                    } else if (result.equals("authFail")) {
                                        Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                                    } else if (result.equals("failConnect")) {
                                        Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
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

        Button back = getActivity().findViewById(R.id.volverAdd);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
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
                        Toast.makeText(getContext(), getString(R.string.notPem), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.badFileType), Toast.LENGTH_SHORT).show();
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
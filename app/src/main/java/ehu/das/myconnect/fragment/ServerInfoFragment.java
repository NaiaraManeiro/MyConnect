package ehu.das.myconnect.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogPassword;
import ehu.das.myconnect.dialog.DialogPem;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.interfaces.ILoading;
import ehu.das.myconnect.interfaces.OnDialogDismiss;
import ehu.das.myconnect.dialog.RemoveDialog;
import ehu.das.myconnect.service.ServerWorker;
import lib.folderpicker.FolderPicker;

public class ServerInfoFragment extends Fragment implements OnDialogDismiss<String>, ILoading {

    private Button edit;
    private EditText serveNameBox;
    private EditText serverUserBox;
    private EditText serverHostBox;
    private EditText serverPortBox;
    private OnDialogDismiss<String> fragment;
    private ILoading iLoading;
    public LoadingDialog loadingDialog;
    private CheckBox conexion;
    private String name;

    public ServerInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_server_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Muestra los datos del servidor
        super.onActivityCreated(savedInstanceState);
        conexion = getActivity().findViewById(R.id.checkBox);
        conexion.setText(getString(R.string.conexion));
        conexion.setVisibility(View.INVISIBLE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("server_connnect", true)) {
            conexion.setChecked(true);
        }
        serveNameBox = getActivity().findViewById(R.id.nombreServidorInfo);
        serveNameBox.setEnabled(false);
        serverUserBox = getActivity().findViewById(R.id.usuarioInfo);
        serverUserBox.setEnabled(false);
        serverHostBox = getActivity().findViewById(R.id.hostInfo);
        serverHostBox.setEnabled(false);
        serverPortBox = getActivity().findViewById(R.id.puertoInfo);
        serverPortBox.setEnabled(false);
        //Obtenemos los datos del servidor
        obtenerDatosServidor();
        //((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));
        edit = getActivity().findViewById(R.id.editarServidorInfo);
        edit.setVisibility(View.INVISIBLE);
        fragment = this;
        iLoading = this;
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = serveNameBox.getText().toString();
                String user = serverUserBox.getText().toString();
                String host = serverHostBox.getText().toString();
                int port = Integer.parseInt(serverPortBox.getText().toString());
                //Validamos los datos
                if (user.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.usuarioVacio), Toast.LENGTH_SHORT).show();
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches() || !Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\\\.)+[A-Za-z]{2,6}$").matcher(host).matches()) {
                    Toast.makeText(getContext(), getString(R.string.hostPattern), Toast.LENGTH_SHORT).show();
                    serverHostBox.setText("");
                } else if (name.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.servidorVacio), Toast.LENGTH_SHORT).show();
                } else if (name.length() > 20) {
                    Toast.makeText(getContext(), getString(R.string.servidorLargo), Toast.LENGTH_SHORT).show();
                } else if (port > 65535) {
                    Toast.makeText(getContext(), getString(R.string.invalidPort), Toast.LENGTH_SHORT).show();
                    serverPortBox.setText("");
                } else {
                    boolean checked = conexion.isChecked();

                    if (checked) {
                        //Pedimos la contraseña o .pem para asegurar que se puede hacer ssh
                        Bundle bundle = new Bundle();
                        bundle.putString("serverName", name);
                        bundle.putString("user", user);
                        bundle.putString("host", host);
                        bundle.putInt("port", port);
                        if (ServerListFragment.selectedServer.getPem() == 0) {
                            DialogPassword dialogPassword = new DialogPassword();
                            dialogPassword.setArguments(bundle);
                            dialogPassword.onDialogDismiss = fragment;
                            dialogPassword.loadingListener = iLoading;
                            dialogPassword.show(getActivity().getSupportFragmentManager(), "contrasena");
                        } else {
                            DialogPem dialogPem = new DialogPem();
                            dialogPem.setArguments(bundle);
                            dialogPem.onDialogDismiss = fragment;
                            dialogPem.loadingListener = iLoading;
                            dialogPem.show(getActivity().getSupportFragmentManager(), "contrasena");
                        }
                    } else {
                        startLoading();
                        //Editamos los datos a la bd en caso de que se pueda realizar el ssh
                        Data datos = new Data.Builder()
                                .putString("action", "editServer")
                                .putString("user", user)
                                .putString("host", host)
                                .putInt("port", port)
                                .putString("serverName", name)
                                .putString("oldServerName", ServerListFragment.selectedServer.getName())
                                .putString("userName", LoginFragment.username)
                                .build();

                        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                                .setInputData(datos)
                                .build();
                        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                                .observe(getActivity(), status -> {
                                    if (status != null && status.getState().isFinished()) {
                                        stopLoading();
                                        String result = status.getOutputData().getString("result");
                                        if (result.equals("Error")) {
                                            Toast.makeText(getContext(), getString(R.string.servidorExistente), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), getString(R.string.servidorEditado), Toast.LENGTH_SHORT).show();
                                            ServerListFragment.selectedServer.setName(name);
                                        }
                                    }
                                });
                        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                    }
                }
            }
        });

        ActionMenuItemView iv = getActivity().findViewById(R.id.edit2);
        iv.setOnClickListener(v -> {
            if (edit.getVisibility() == View.VISIBLE) {
                edit.setVisibility(View.INVISIBLE);
                conexion.setVisibility(View.INVISIBLE);
                serveNameBox.setEnabled(false);
                serverUserBox.setEnabled(false);
                serverHostBox.setEnabled(false);
                serverPortBox.setEnabled(false);
            } else {
                edit.setVisibility(View.VISIBLE);
                conexion.setVisibility(View.VISIBLE);
                serveNameBox.setEnabled(true);
                serverUserBox.setEnabled(true);
                serverHostBox.setEnabled(true);
                serverPortBox.setEnabled(true);
            }
        });

        ActionMenuItemView iv1 = getActivity().findViewById(R.id.eliminar2);
        iv1.setOnClickListener(v -> {
            RemoveDialog dialogoEliminar = new RemoveDialog();
            dialogoEliminar.loadingListener = iLoading;
            Bundle bundle = new Bundle();
            dialogoEliminar.view = getView();
            bundle.putString("serverName", ServerListFragment.selectedServer.getName());
            bundle.putString("where", "server");
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getActivity().getSupportFragmentManager(), "eliminar");
        });

        Button back = getActivity().findViewById(R.id.volverInfo);
        back.setOnClickListener(new View.OnClickListener() {
            // Volver atrás
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    //Creación del menú
  /*  @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.server_info_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Al seleccionar una opción del servidor
        int id = item.getItemId();
        if (id == R.id.eliminar) {
            RemoveDialog dialogoEliminar = new RemoveDialog();
            dialogoEliminar.loadingListener = iLoading;
            Bundle bundle = new Bundle();
            dialogoEliminar.view = getView();
            bundle.putString("serverName", ServerListFragment.selectedServer.getName());
            bundle.putString("where", "server");
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getActivity().getSupportFragmentManager(), "eliminar");
        } if (id == R.id.edit) {
            if (edit.getVisibility() == View.VISIBLE) {
                edit.setVisibility(View.INVISIBLE);
                conexion.setVisibility(View.INVISIBLE);
                serveNameBox.setEnabled(false);
                serverUserBox.setEnabled(false);
                serverHostBox.setEnabled(false);
                serverPortBox.setEnabled(false);
            } else {
                edit.setVisibility(View.VISIBLE);
                conexion.setVisibility(View.VISIBLE);
                serveNameBox.setEnabled(true);
                serverUserBox.setEnabled(true);
                serverHostBox.setEnabled(true);
                serverPortBox.setEnabled(true);
            }
        }
        return super.onOptionsItemSelected(item);
    } */

    private void obtenerDatosServidor() {
        // Poner los datos del servidor en la interfaz
        serveNameBox.setText(ServerListFragment.selectedServer.getName());
        serverUserBox.setText(ServerListFragment.selectedServer.getUser());
        serverHostBox.setText(ServerListFragment.selectedServer.getHost());
        serverPortBox.setText(String.valueOf(ServerListFragment.selectedServer.getPort()));
    }

    @Override
    public void onDismiss(String result) {
        if (result.equals("Error")) {
            Toast.makeText(getContext(), getString(R.string.servidorExistente), Toast.LENGTH_SHORT).show();
        } else if (result.equals("authFail")) {
            Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
        } else if (result.equals("failConnect")) {
            Toast.makeText(getContext(), getString(R.string.connectRefused), Toast.LENGTH_LONG).show();
        } else if (result.equals("hostUnreachable")) {
            Toast.makeText(getContext(), getString(R.string.hostUnreachable), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.servidorEditado), Toast.LENGTH_SHORT).show();
            ServerListFragment.selectedServer.setName(name);
        }
    }

    public void startLoading() {
        // Poner pantalla de carga
        loadingDialog = new LoadingDialog();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
    }

    public void stopLoading() {
        // Quitar pantalla de carga
        loadingDialog.dismiss();
    }
}
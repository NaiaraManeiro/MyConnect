package ehu.das.myconnect.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogPassword;
import ehu.das.myconnect.dialog.DialogoAccessPassword;
import ehu.das.myconnect.dialog.OnDialogDismiss;
import ehu.das.myconnect.dialog.RemoveDialog;
import ehu.das.myconnect.service.ServerWorker;

public class ServerInfoFragment extends Fragment implements OnDialogDismiss<String> {

    private Button edit;
    private EditText serveNameBox;
    private EditText serverUserBox;
    private EditText serverHostBox;
    private EditText serverPortBox;
    private OnDialogDismiss<String> fragment;

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
        super.onActivityCreated(savedInstanceState);

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

        ((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));
        edit = getActivity().findViewById(R.id.editarServidorInfo);
        edit.setVisibility(View.INVISIBLE);

        fragment = this;
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = serveNameBox.getText().toString();
                String user = serverUserBox.getText().toString();
                String host = serverHostBox.getText().toString();
                int port = Integer.parseInt(serverPortBox.getText().toString());

                //Validamos los datos
                if (user.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.usuarioVacio), Toast.LENGTH_SHORT).show();
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches()) {
                    Toast.makeText(getContext(), getString(R.string.hostPattern), Toast.LENGTH_SHORT).show();
                    serverHostBox.setText("");
                } else if (name.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.servidorVacio), Toast.LENGTH_SHORT).show();
                } else if (name.length() > 20) {
                    Toast.makeText(getContext(), getString(R.string.servidorLargo), Toast.LENGTH_SHORT).show();
                } else {
                    //Pedimos la contraseña para asegurar que se puede hacer ssh
                    DialogPassword dialogPassword = new DialogPassword();
                    Bundle bundle = new Bundle();
                    bundle.putString("serverName", name);
                    bundle.putString("user", user);
                    bundle.putString("host", host);
                    bundle.putInt("port", port);
                    dialogPassword.setArguments(bundle);
                    dialogPassword.onDialogDismiss = fragment;
                    dialogPassword.show(getActivity().getSupportFragmentManager(), "contrasena");
                }
            }
        });

        Button back = getActivity().findViewById(R.id.volverInfo);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    //Creación del menú
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.eliminar) {
            RemoveDialog dialogoEliminar = new RemoveDialog();
            Bundle bundle = new Bundle();
            dialogoEliminar.view = getView();
            bundle.putString("serverName", ServerListFragment.selectedServer.getName());
            bundle.putString("where", "server");
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getActivity().getSupportFragmentManager(), "eliminar");
        } if (id == R.id.edit) {
            if (edit.getVisibility() == View.VISIBLE) {
                edit.setVisibility(View.INVISIBLE);
                serveNameBox.setEnabled(false);
                serverUserBox.setEnabled(false);
                serverHostBox.setEnabled(false);
                serverPortBox.setEnabled(false);
            } else {
                edit.setVisibility(View.VISIBLE);
                serveNameBox.setEnabled(true);
                serverUserBox.setEnabled(true);
                serverHostBox.setEnabled(true);
                serverPortBox.setEnabled(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void obtenerDatosServidor() {
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
            Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.servidorEditado), Toast.LENGTH_SHORT).show();
        }
    }
}
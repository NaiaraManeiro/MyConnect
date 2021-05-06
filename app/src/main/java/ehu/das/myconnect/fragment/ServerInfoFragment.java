package ehu.das.myconnect.fragment;

import android.content.DialogInterface;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogoContrasena;
import ehu.das.myconnect.dialog.DialogoEliminar;
import ehu.das.myconnect.list.ServerListAdapter;
import ehu.das.myconnect.service.ServerWorker;

public class ServerInfoFragment extends Fragment {

    private Button editar;
    private String nombreServer;
    private String nombreUsuario;
    private EditText nombreServidor;
    private EditText usuarioServidor;
    private EditText hostServidor;
    private EditText puertoServidor;

    public ServerInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nombreServer = bundle.getString("serverName");
            nombreUsuario = bundle.getString("nombreUsuario");
        }
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

        nombreServidor = getActivity().findViewById(R.id.nombreServidorInfo);
        nombreServidor.setEnabled(false);
        usuarioServidor = getActivity().findViewById(R.id.usuarioInfo);
        usuarioServidor.setEnabled(false);
        hostServidor = getActivity().findViewById(R.id.hostInfo);
        hostServidor.setEnabled(false);
        puertoServidor = getActivity().findViewById(R.id.puertoInfo);
        puertoServidor.setEnabled(false);

        //Obtenemos los datos del servidor
        obtenerDatosServidor();

        ((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));
        editar = getActivity().findViewById(R.id.editarServidorInfo);
        editar.setVisibility(View.INVISIBLE);

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = nombreServidor.getText().toString();
                String usuario = usuarioServidor.getText().toString();
                String host = hostServidor.getText().toString();
                int puerto = Integer.parseInt(puertoServidor.getText().toString());

                //Validamos los datos
                if (usuario.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.usuarioVacio), Toast.LENGTH_SHORT).show();
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches()) {
                    Toast.makeText(getContext(), getString(R.string.hostPattern), Toast.LENGTH_SHORT).show();
                    hostServidor.setText("");
                } else if (nombre.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.servidorVacio), Toast.LENGTH_SHORT).show();
                } else if (nombre.length() > 20) {
                    Toast.makeText(getContext(), getString(R.string.servidorLargo), Toast.LENGTH_SHORT).show();
                } else {
                    //Pedimos la contraseña para asegurar que se puede hacer ssh
                    DialogFragment dialogoContrasena = new DialogoContrasena();
                    Bundle bundle = new Bundle();
                    bundle.putString("nombreServerViejo", nombreServer);
                    bundle.putString("nombreServer", nombre);
                    bundle.putString("usuario", usuario);
                    bundle.putString("host", host);
                    bundle.putString("nombreUsuario", nombreUsuario);
                    bundle.putInt("puerto", puerto);
                    dialogoContrasena.setArguments(bundle);
                    dialogoContrasena.show(getActivity().getSupportFragmentManager(), "contrasena");

                    nombreServer = nombre;
                }
            }
        });

        Button volver = getActivity().findViewById(R.id.volverInfo);
        volver.setOnClickListener(new View.OnClickListener() {
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
            DialogFragment dialogoEliminar = new DialogoEliminar();
            Bundle bundle = new Bundle();
            bundle.putString("nombreServer", nombreServer);
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getActivity().getSupportFragmentManager(), "eliminar");
        } if (id == R.id.edit) {
            if (editar.getVisibility() == View.VISIBLE) {
                editar.setVisibility(View.INVISIBLE);
                nombreServidor.setEnabled(false);
                usuarioServidor.setEnabled(false);
                hostServidor.setEnabled(false);
                puertoServidor.setEnabled(false);
            } else {
                editar.setVisibility(View.VISIBLE);
                nombreServidor.setEnabled(true);
                usuarioServidor.setEnabled(true);
                hostServidor.setEnabled(true);
                puertoServidor.setEnabled(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void obtenerDatosServidor() {
        Data datos = new Data.Builder()
                .putString("funcion", "infoServer")
                .putString("nombreServidor", nombreServer)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (!result.equals("")) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String usuario = jsonObject.get("usuario").toString();
                                String host = jsonObject.get("host").toString();
                                String puerto = jsonObject.get("puerto").toString();

                                nombreServidor.setText(nombreServer);
                                usuarioServidor.setText(usuario);
                                hostServidor.setText(host);
                                puertoServidor.setText(puerto);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}
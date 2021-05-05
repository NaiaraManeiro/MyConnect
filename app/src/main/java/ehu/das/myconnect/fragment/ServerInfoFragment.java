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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogoEliminar;
import ehu.das.myconnect.service.ServerWorker;

public class ServerInfoFragment extends Fragment {

    private EditText password;
    private Button editar;
    private String nombreServer;

    public ServerInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nombreServer = bundle.getString("serverName");
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

        //Obtenemos los datos del servidor

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

                                EditText nombreServidor = getActivity().findViewById(R.id.nombreServidorInfo);
                                nombreServidor.setText(nombreServer);
                                EditText usuarioServidor = getActivity().findViewById(R.id.usuarioInfo);
                                usuarioServidor.setText(usuario);
                                EditText hostServidor = getActivity().findViewById(R.id.hostInfo);
                                hostServidor.setText(host);
                                EditText puertoServidor = getActivity().findViewById(R.id.puertoInfo);
                                puertoServidor.setText(puerto);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

        ((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));
        password = getActivity().findViewById(R.id.contrasenaInfo);
        password.setVisibility(View.INVISIBLE);
        editar = getActivity().findViewById(R.id.editarServidorInfo);
        editar.setVisibility(View.INVISIBLE);

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            password.setVisibility(View.VISIBLE);
            editar.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }
}
package ehu.das.myconnect.fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import java.util.regex.Pattern;

import ehu.das.myconnect.R;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.ServerWorker;


public class AddServerFragment extends Fragment {

    private String nombreUsuario = "Naiara";

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

        /*Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            nombreUsuario = extras.getString("nombreUsuario");
        }*/

        EditText usuarioCaja = getActivity().findViewById(R.id.usuario);
        EditText hostCaja = getActivity().findViewById(R.id.host);
        EditText puertoCaja = getActivity().findViewById(R.id.puerto);
        EditText contrasenaCaja = getActivity().findViewById(R.id.contrasena);
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

                } else if (usuario.length() > 20) {
                    usuarioCaja.setText("");
                } else if (!Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$").matcher(host).matches()) {
                    hostCaja.setText("");
                } else if (contrasena.equals("")) {

                } if (servidor.equals("")) {

                } else if (servidor.length() > 20) {

                } else {
                    //Comprobamos que se puede realizar una conexión ssh
                    //SSHConnector sshConnector = new SSHConnector();

                        //sshConnector.connect(usuario, contrasena, host, puerto);

                        //Añadimos los datos a la bd
                        Data datos = new Data.Builder()
                                .putString("funcion", "addServer")
                                .putString("usuario", usuario)
                                .putString("host", host)
                                .putString("contrasena", contrasena)
                                .putInt("puerto", puerto)
                                .putString("nombreServidor", servidor)
                                .putString("nombreUsuario", nombreUsuario)
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
                                            //sshConnector.disconnect();
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
}
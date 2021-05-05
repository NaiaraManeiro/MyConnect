package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.list.ServerListAdapter;
import ehu.das.myconnect.service.ServerWorker;


public class ServerListFragment extends Fragment {

    private String nombreUsuario = "Naiara";
    private List<Server> serverList = new ArrayList();

    public ServerListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*Bundle extras = this.getArguments();
        if (extras != null) {
            nombreUsuario = extras.getString("nombreUsuario");
        }*/

        //Obtenemos los datos de los servidores del usuario
        RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);

        Data datos = new Data.Builder()
                .putString("funcion", "datosServer")
                .putString("nombreUsuario", nombreUsuario)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");

                        if (!result.equals("")) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(result);
                                JSONArray jsonArrayServidores = jsonObject.getJSONArray("nombresServidores");
                                JSONArray jsonArrayUsuarios = jsonObject.getJSONArray("usuarios");
                                JSONArray jsonArrayHosts = jsonObject.getJSONArray("hosts");
                                JSONArray jsonArrayPuertos = jsonObject.getJSONArray("puertos");

                                for (int i = 0; i < jsonArrayServidores.length(); i++) {
                                    String nombreServidor = jsonArrayServidores.get(i).toString();
                                    String usuario = jsonArrayUsuarios.get(i).toString();
                                    String host = jsonArrayHosts.get(i).toString();
                                    int puerto = Integer.parseInt(jsonArrayPuertos.get(i).toString());
                                    Server servidor = new Server(nombreServidor,usuario,host,puerto);
                                    serverList.add(servidor);
                                }

                                if (serverList.size() == jsonArrayHosts.length()) {
                                    serverListRV.setAdapter(new ServerListAdapter(serverList));
                                    LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
                                    serverListRV.setLayoutManager(linearLayoutManager);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

        serverListRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = serverListRV.getChildLayoutPosition(v);
                String serverName = serverList.get(itemPosition).getNombre();
                Bundle bundle = new Bundle();
                bundle.putString("serverName", serverName); //Para saber cual es el server seleccionado
                //bundle.putParcelableArrayList("listaServidores", (ArrayList<? extends Parcelable>) serverList);
                Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment, bundle);
            }
        });

        Button addServer = getActivity().findViewById(R.id.addServer);
        addServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_addServerFragment);
            }
        });
    }
}
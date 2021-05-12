package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogoAccessPassword;
import ehu.das.myconnect.list.ServerListAdapter;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.ServerWorker;


public class ServerListFragment extends Fragment {

    public static SSHConnector connection;
    private String nombreUsuario = "Naiara";
    public static List<Server> serverList;
    public static Server selectedServer;

    public ServerListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_server_list, container, false);
        RecyclerView serverListRV = v.findViewById(R.id.serverListRV);

        serverList = new ArrayList<>();

        Data data = new Data.Builder()
                .putString("action", "serverData")
                .putString("userName", nombreUsuario)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");

                        if (!result.equals("")) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(result);
                                JSONArray jsonArrayServers = jsonObject.getJSONArray("serversNames");
                                JSONArray jsonArrayUsers = jsonObject.getJSONArray("users");
                                JSONArray jsonArrayHosts = jsonObject.getJSONArray("hosts");
                                JSONArray jsonArrayPorts = jsonObject.getJSONArray("ports");

                                for (int i = 0; i < jsonArrayServers.length(); i++) {
                                    String serverName = jsonArrayServers.get(i).toString();
                                    String user = jsonArrayUsers.get(i).toString();
                                    String host = jsonArrayHosts.get(i).toString();
                                    int port = Integer.parseInt(jsonArrayPorts.get(i).toString());
                                    Server server = new Server(serverName,user,host,port);
                                    serverList.add(server);
                                }

                                if (serverList.size() == jsonArrayHosts.length()) {
                                    ServerListAdapter serverListAdapter = new ServerListAdapter(serverList, getActivity().getSupportFragmentManager());
                                    serverListAdapter.fragment = this;
                                    serverListRV.setAdapter(serverListAdapter);
                                    GridLayoutManager gridLayoutManager= new GridLayoutManager(getContext(), 3, LinearLayoutManager.HORIZONTAL,false);
                                    serverListRV.setLayoutManager(gridLayoutManager);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /**RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);
        List<Server> serverList = new ArrayList<Server>();
        serverListRV.setAdapter(new ServerListAdapter(serverList));
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        serverListRV.setLayoutManager(linearLayoutManager);
         */

        /*Bundle extras = this.getArguments();
        if (extras != null) {
            userName = extras.getString("userName");
        }*/

        //Obtenemos los datos de los servidores del usuario


        Button addServer = getActivity().findViewById(R.id.addServer);
        addServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_addServerFragment);
            }
        });
    }

    public void connectServer() {
        if (connection == null) {
            DialogoAccessPassword d = new DialogoAccessPassword();
            d.v = getView();
            d.show(getActivity().getSupportFragmentManager(),null);
        }
        else {
            Navigation.findNavController(getView()).navigate(R.id.action_serverListFragment_to_serverManagmentFragment);
        }
    }

}
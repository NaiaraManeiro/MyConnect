package ehu.das.myconnect.fragment;

import android.graphics.Color;
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

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogAccessPem;
import ehu.das.myconnect.dialog.DialogoAccessPassword;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.dialog.OnDialogDismiss;
import ehu.das.myconnect.dialog.OnDialogOptionPressed;
import ehu.das.myconnect.list.ServerListAdapter;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.ServerWorker;

public class ServerListFragment extends Fragment implements OnDialogOptionPressed<String>, ILoading, OnDialogDismiss<String> {

    public static SSHConnector connection;
    public static List<Server> serverList;
    public static Server selectedServer = null;
    public LoadingDialog loadingDialog;

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
        serverListRV.bringToFront();
        startLoading();
        serverList = new ArrayList<>();
        Data data = new Data.Builder()
                .putString("action", "serverData")
                .putString("userName", LoginFragment.username)
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
                                JSONArray jsonArrayPems = jsonObject.getJSONArray("pems");
                                for (int i = 0; i < jsonArrayServers.length(); i++) {
                                    String serverName = jsonArrayServers.get(i).toString();
                                    String user = jsonArrayUsers.get(i).toString();
                                    String host = jsonArrayHosts.get(i).toString();
                                    int port = Integer.parseInt(jsonArrayPorts.get(i).toString());
                                    int pem = Integer.parseInt(jsonArrayPems.get(i).toString());
                                    Server server = new Server(serverName,user,host,port,pem);
                                    serverList.add(server);
                                }
                                if (serverList.size() == jsonArrayHosts.length()) {
                                    ServerListAdapter serverListAdapter = new ServerListAdapter(serverList, getActivity().getSupportFragmentManager());
                                    serverListAdapter.fragment = this;
                                    serverListRV.setAdapter(serverListAdapter);
                                    GridLayoutManager gridLayoutManager= new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL,false);
                                    serverListRV.setLayoutManager(gridLayoutManager);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        stopLoading();
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
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);scriptAddListener
        serverListRV.setLayoutManager(linearLayoutManager);
         */

        ImageView conf = getActivity().findViewById(R.id.confServerList);
        conf.setColorFilter(Color.WHITE);
        conf.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_preferences);
        });
        TextView username = getActivity().findViewById(R.id.usernameTV);
        username.setText(LoginFragment.username);
        ImageView logout = getActivity().findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_loginFragment);
        });
        logout.setColorFilter(Color.WHITE);
        //Obtenemos los datos de los servidores del usuario
        RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);
        serverListRV.bringToFront();

        ImageView addServer = getActivity().findViewById(R.id.addServer);
        addServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_addServerFragment);
            }
        });
        addServer.setColorFilter(Color.WHITE);
    }

    public void connectServer() {
        if (selectedServer.getPem() == 0) {
            DialogoAccessPassword d = new DialogoAccessPassword();
            d.scriptAddListener = this;
            d.v = getView();
            d.serverListFragment = this;
            d.loadingListener = this;
            d.show(getActivity().getSupportFragmentManager(),null);
        } else {
            OnDialogDismiss<String> fragment = this;
            DialogAccessPem d = new DialogAccessPem();
            d.scriptAddListener = this;
            d.v = getView();
            d.serverListFragment = this;
            d.loadingListener = this;
            d.onDialogDismiss = fragment;
            d.show(getActivity().getSupportFragmentManager(),null);
        }
    }

    @Override
    public void onYesPressed(String data1, String data2) {
        if (data1.equals("fail")) {
            Toast.makeText(getContext(), getResources().getString(R.string.connectRefused), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.authSuccessful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoPressed(String data) {

    }

    @Override
    public void notifyError(String string) {

    }

    public void startLoading() {
        loadingDialog = new LoadingDialog();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
    }

    public void stopLoading() {
        loadingDialog.dismiss();
    }

    @Override
    public void onDismiss(String data) {
        if (data.equals("noPem")) {
            Toast.makeText(getContext(), getString(R.string.notPem), Toast.LENGTH_SHORT).show();
        } else if (data.equals("noFile")) {
            Toast.makeText(getContext(), getString(R.string.badFileType), Toast.LENGTH_SHORT).show();
        }
    }
}
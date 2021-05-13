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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogoAccessPassword;
import ehu.das.myconnect.dialog.OnDialogOptionPressed;
import ehu.das.myconnect.list.ServerListReducedAdapter;
import ehu.das.myconnect.service.SSHCommandWorker;
import ehu.das.myconnect.service.SSHConnectionWorker;

import static ehu.das.myconnect.fragment.ServerListFragment.serverList;

public class ServerManagmentFragment extends Fragment implements OnDialogOptionPressed<String> {

    public ServerManagmentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_server_managment, container, false);
        RecyclerView serverListRV = v.findViewById(R.id.serverListRV);
        serverListRV.setAdapter(new ServerListReducedAdapter(ServerListFragment.serverList, ServerListFragment.selectedServer, this));
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        serverListRV.setLayoutManager(linearLayoutManager);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);
        /**
        List<Server> serverList = new ArrayList<>();
        serverList.add(new Server("ander"));
        serverList.add(new Server("dawe"));
        serverList.add(new Server("das"));
        serverList.add(new Server("dawe2"));
        serverList.add(new Server("das2"));
        serverList.add(new Server("das3"));
        serverList.add(new Server("das4"));
        serverList.add(new Server("das5"));
        */
        ImageButton iv = getActivity().findViewById(R.id.disconnectServer);
        iv.setOnClickListener(v -> {
            ServerListFragment.selectedServer = null;
            Navigation.findNavController(getView()).navigate(R.id.action_serverManagmentFragment_to_serverListFragment);
        });
        ServerListReducedAdapter serverListReducedAdapter = new ServerListReducedAdapter(ServerListFragment.serverList, ServerListFragment.selectedServer, this);
        serverListRV.setAdapter(serverListReducedAdapter);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        serverListRV.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onYesPressed(String data1, String data2) {
        if (data1.equals("fail")) {
            Toast.makeText(getContext(), getResources().getString(R.string.authFail), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.authSuccessful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoPressed(String data) {

    }

    public void changeServer(int position) {
        DialogoAccessPassword d = new DialogoAccessPassword();
        d.scriptAddListener = this;
        d.recreate = true;
        d.serverManagmentFragment = this;
        d.position = position;
        d.show(getActivity().getSupportFragmentManager(),"");
    }

    public void recreateFragment() {
        getActivity().recreate();
    }
}
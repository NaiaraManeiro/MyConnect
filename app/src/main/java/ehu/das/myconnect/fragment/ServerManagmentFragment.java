package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.Server;
import ehu.das.myconnect.list.ServerListAdapter;

public class ServerManagmentFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_server_managment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);
        List<Server> serverList = new ArrayList<Server>();
        serverListRV.setAdapter(new ServerListAdapter(serverList));
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        serverListRV.setLayoutManager(linearLayoutManager);
    }
}
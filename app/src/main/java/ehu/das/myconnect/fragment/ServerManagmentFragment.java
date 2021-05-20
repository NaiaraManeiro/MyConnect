package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogAccessPem;
import ehu.das.myconnect.dialog.DialogoAccessPassword;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.dialog.OnDialogOptionPressed;
import ehu.das.myconnect.list.ServerListReducedAdapter;

public class ServerManagmentFragment extends Fragment implements OnDialogOptionPressed<String>, ILoading {

    public LoadingDialog loadingDialog;

    public ServerManagmentFragment() {}

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

        ActionMenuItemView conf = getActivity().findViewById(R.id.configButton);
        conf.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_serverManagmentFragment_to_preferences);
        });
        RecyclerView serverListRV = getActivity().findViewById(R.id.serverListRV);

        ActionMenuItemView iv = getActivity().findViewById(R.id.disconnectServer);
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
            Toast.makeText(getContext(), getResources().getString(R.string.connectRefused), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.authSuccessful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoPressed(String data) {}

    @Override
    public void notifyError(String string) {

    }

    public void changeServer(int position) {
        Server server = ServerListFragment.serverList.get(position);
         if (server.getPem() == 0) {
             DialogoAccessPassword d = new DialogoAccessPassword();
             d.scriptAddListener = this;
             d.recreate = true;
             d.serverManagmentFragment = this;
             d.loadingListener = this;
             d.position = position;
             d.show(getActivity().getSupportFragmentManager(),"");
        } else {
            DialogAccessPem d = new DialogAccessPem();
             d.scriptAddListener = this;
             d.recreate = true;
             d.serverManagmentFragment = this;
             d.loadingListener = this;
             d.position = position;
            d.show(getActivity().getSupportFragmentManager(),"");
        }
    }

    public void recreateFragment() {
        getActivity().recreate();
    }

    @Override
    public void startLoading() {
        loadingDialog = new LoadingDialog();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
    }

    @Override
    public void stopLoading() {
        loadingDialog.dismiss();
    }
}
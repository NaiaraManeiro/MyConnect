package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.Server;
import ehu.das.myconnect.fragment.ServerListFragment;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListElementViewHolder> {

    public ServerListFragment fragment;
    private final List<Server> servers;
    private final FragmentManager fragmentManager;

    public ServerListAdapter(List<Server> servers, FragmentManager fragmentManager) {
        this.servers = servers;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ServerListElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View serverLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_element_layout, null);
        return new ServerListElementViewHolder(serverLayout, fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerListElementViewHolder holder, int position) {
        holder.serverName.setText(servers.get(position).getName());
        holder.user.setText(servers.get(position).getUser());
        holder.host.setText(servers.get(position).getHost());
        holder.itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", holder.serverName.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            ServerListFragment.selectedServer = servers.get(position);
            fragment.connectServer();
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

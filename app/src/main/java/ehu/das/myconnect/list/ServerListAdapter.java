package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.Server;
import ehu.das.myconnect.fragment.ServerListFragment;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListElementViewHolder> {

    private List<Server> servers;

    public ServerListAdapter(List<Server> servers) {
        this.servers = servers;
    }

    @NonNull
    @Override
    public ServerListElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View serverLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_element_layout, null);
        ServerListElementViewHolder serverListElementViewHolder = new ServerListElementViewHolder(serverLayout);
        return serverListElementViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ServerListElementViewHolder holder, int position) {
        holder.nombreServidor.setText(servers.get(position).getNombre());
        holder.usuarioHost.setText(servers.get(position).getUsuario() + "@"+ servers.get(position).getHost());
        holder.port.setText(""+servers.get(position).getPuerto()+"");
        holder.itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", holder.nombreServidor.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            ServerListFragment.selected = position;
            Bundle bundle = new Bundle();
            bundle.putString("serverName", holder.nombreServidor.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

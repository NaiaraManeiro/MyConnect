package ehu.das.myconnect.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.Server;

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
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

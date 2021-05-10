package ehu.das.myconnect.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.Server;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListElementViewHolder> {

    private List<Server> servers;
    private FragmentManager fragmentManager;
    private View view;

    public ServerListAdapter(List<Server> servers, FragmentManager fragmentManager, View view) {
        this.servers = servers;
        this.fragmentManager = fragmentManager;
        this.view = view;
    }

    @NonNull
    @Override
    public ServerListElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View serverLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_element_layout, null);
        ServerListElementViewHolder serverListElementViewHolder = new ServerListElementViewHolder(serverLayout, fragmentManager, view);
        return serverListElementViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ServerListElementViewHolder holder, int position) {
        holder.nombreServidor.setText(servers.get(position).getNombre());
        holder.usuario.setText(servers.get(position).getUsuario());
        holder.host.setText(servers.get(position).getHost());
        holder.port.setText(""+servers.get(position).getPuerto()+"");
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

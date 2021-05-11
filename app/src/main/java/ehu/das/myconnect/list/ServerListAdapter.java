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
        holder.port.setText(""+servers.get(position).getPort()+"");
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

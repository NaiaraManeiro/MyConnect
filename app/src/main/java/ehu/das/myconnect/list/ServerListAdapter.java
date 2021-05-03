package ehu.das.myconnect.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.Server;

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
        holder.nameShort.setText("S" + position);
        holder.name.setText(servers.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

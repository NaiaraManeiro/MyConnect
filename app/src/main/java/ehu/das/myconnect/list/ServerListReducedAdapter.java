package ehu.das.myconnect.list;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.Server;

public class ServerListReducedAdapter extends RecyclerView.Adapter<ServerListReducedElementViewHolder> {

    private List<Server> servers;
    private Server connectedServer;

    public ServerListReducedAdapter(List<Server> servers, Server connectedServer) {
        this.servers = servers;
        this.connectedServer = connectedServer;
    }

    @NonNull
    @Override
    public ServerListReducedElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View serverLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_element_layout, null);
        ServerListReducedElementViewHolder serverListReducedElementViewHolder = new ServerListReducedElementViewHolder(serverLayout);
        return serverListReducedElementViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ServerListReducedElementViewHolder holder, int position) {
        holder.nameShort.setText("S" + position);
        holder.name.setText(servers.get(position).getName());
        // holder.name.setText("ander");
        holder.serverCircle.setImageResource(R.drawable.circle);
        if (servers.get(position).equals(connectedServer)) {
            holder.serverCircle.setColorFilter(Color.GREEN);
        } else {
            holder.serverCircle.setColorFilter(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }
}

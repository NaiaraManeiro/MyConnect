package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView nombreServidor;
    public TextView usuarioHost;
    public TextView port;

    public ServerListElementViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(v -> {
            // Conectarse al servidor
        });
    }

}

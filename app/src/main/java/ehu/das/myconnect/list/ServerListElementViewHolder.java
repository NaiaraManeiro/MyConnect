package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView nombreServidor;
    public TextView usuarioHost;
    public TextView port;

    public ServerListElementViewHolder(@NonNull View itemView) {
        super(itemView);
        nombreServidor = itemView.findViewById(R.id.servidorNombre);
        usuarioHost = itemView.findViewById(R.id.servidorUsuarioHost);
        port = itemView.findViewById(R.id.servidorPuerto);

        itemView.setOnClickListener(v -> {
            // Conectarse al servidor
        });
    }

}

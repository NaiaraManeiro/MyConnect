package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
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

        itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", nombreServidor.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });

        itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", nombreServidor.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment, bundle);
        });
    }

}

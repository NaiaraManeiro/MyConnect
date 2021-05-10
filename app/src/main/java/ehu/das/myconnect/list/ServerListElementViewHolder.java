package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.DialogoContrasena;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView nombreServidor;
    public TextView usuario;
    public TextView host;
    public TextView port;

    public ServerListElementViewHolder(@NonNull View itemView, FragmentManager fragmentManager, View view) {
        super(itemView);

        nombreServidor = itemView.findViewById(R.id.servidorNombre);
        usuario = itemView.findViewById(R.id.servidorUsuario);
        host = itemView.findViewById(R.id.servidorHost);
        port = itemView.findViewById(R.id.servidorPuerto);

        itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", nombreServidor.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });

        itemView.setOnClickListener(v -> {
            DialogoContrasena dialogoContrasena = new DialogoContrasena();
            dialogoContrasena.view = v;
            Bundle bundle = new Bundle();
            bundle.putString("funcion", "conectServer");
            bundle.putString("usuario", usuario.getText().toString());
            bundle.putString("host", host.getText().toString());
            bundle.putInt("puerto", Integer.parseInt(port.getText().toString()));
            bundle.putString("nombreServer", nombreServidor.getText().toString());
            dialogoContrasena.setArguments(bundle);
            dialogoContrasena.show(fragmentManager, "contrasena");
        });
    }
}

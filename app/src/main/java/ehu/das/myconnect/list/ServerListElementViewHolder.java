package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;

/**
 * Cada elemento de la lista de servidores
 */
public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView serverName;
    public TextView user;
    public TextView host;

    public ServerListElementViewHolder(@NonNull View itemView, FragmentManager fragmentManager) {
        super(itemView);

        serverName = itemView.findViewById(R.id.servidorNombre);
        user = itemView.findViewById(R.id.servidorUsuario);
        host = itemView.findViewById(R.id.servidorHost);

        itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", serverName.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });
    }
}

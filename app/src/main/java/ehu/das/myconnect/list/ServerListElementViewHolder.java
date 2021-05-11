package ehu.das.myconnect.list;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.PasswordDialog;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView serverName;
    public TextView user;
    public TextView host;
    public TextView port;

    public ServerListElementViewHolder(@NonNull View itemView, FragmentManager fragmentManager) {
        super(itemView);

        serverName = itemView.findViewById(R.id.servidorNombre);
        user = itemView.findViewById(R.id.servidorUsuario);
        host = itemView.findViewById(R.id.servidorHost);
        port = itemView.findViewById(R.id.servidorPuerto);

        itemView.setOnLongClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("serverName", serverName.getText().toString()); //Para saber cual es el server seleccionado
            Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverInfoFragment, bundle);
            return false;
        });

        itemView.setOnClickListener(v -> {
            PasswordDialog passwordDialog = new PasswordDialog();
            passwordDialog.view = v;
            Bundle bundle = new Bundle();
            bundle.putString("action", "conectServer");
            bundle.putString("user", user.getText().toString());
            bundle.putString("host", host.getText().toString());
            bundle.putInt("port", Integer.parseInt(port.getText().toString()));
            bundle.putString("serverName", serverName.getText().toString());
            passwordDialog.setArguments(bundle);
            passwordDialog.show(fragmentManager, "password");
        });
    }
}

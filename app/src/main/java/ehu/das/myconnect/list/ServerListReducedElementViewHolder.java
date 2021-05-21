package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import ehu.das.myconnect.R;


/**
 * Adaptador de la lista reducida de servidores del serverManagmentFragment
 */
public class ServerListReducedElementViewHolder extends RecyclerView.ViewHolder {

    public TextView nameShort;
    public TextView name;
    public ImageView serverCircle;

    public ServerListReducedElementViewHolder(@NonNull View itemView) {
        super(itemView);
        nameShort = itemView.findViewById(R.id.serverNameShort);
        name = itemView.findViewById(R.id.serverName);
        serverCircle = itemView.findViewById(R.id.serverCircleIV);
        itemView.setOnClickListener(v -> {
            // Conectarse al servidor
            // Pedir contraseña
        });
    }

}

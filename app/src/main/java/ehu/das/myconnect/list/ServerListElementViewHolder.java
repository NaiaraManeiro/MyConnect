package ehu.das.myconnect.list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView nameShort;
    public TextView name;

    public ServerListElementViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(v -> {
            // Conectarse al servidor
            // Pedir contraseña
        });
    }

}

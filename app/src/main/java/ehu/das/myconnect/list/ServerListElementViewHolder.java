package ehu.das.myconnect.list;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ServerListElementViewHolder extends RecyclerView.ViewHolder {

    public ServerListElementViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(v -> {
            // Conectarse al servidor
        });
    }

}

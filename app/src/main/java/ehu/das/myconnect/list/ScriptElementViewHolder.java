package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;

/**
 * Cada elemento de la lista de scripts
 */
public class ScriptElementViewHolder extends RecyclerView.ViewHolder {

    public TextView scriptName;
    public TextView scriptCmd;
    public ImageView run;

    public ScriptElementViewHolder(@NonNull View itemView) {
        super(itemView);
        scriptName = itemView.findViewById(R.id.scriptName);
        scriptCmd = itemView.findViewById(R.id.scriptCmd);
        run = itemView.findViewById(R.id.runScript);
        run.setOnClickListener(v -> {
           // Ejecutar comando
            // mostrar salida
        });
    }
}

package ehu.das.myconnect.list;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.Server;

public class ScriptListAdapter extends RecyclerView.Adapter<ScriptElementViewHolder> {

    private List<String> scriptNames;
    private List<String> scriptCmds;

    public ScriptListAdapter(List<String> scriptNames, List<String> scriptCmds) {
        this.scriptNames = scriptNames;
        this.scriptCmds = scriptCmds;
    }

    @NonNull
    @Override
    public ScriptElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View scriptLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.script_element_layout, parent, false);
        scriptLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ScriptElementViewHolder(scriptLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ScriptElementViewHolder holder, int position) {
        holder.scriptName.setText(scriptNames.get(position));
        holder.scriptCmd.setText(scriptCmds.get(position));
    }

    @Override
    public int getItemCount() {
        return scriptNames.size();
    }
}
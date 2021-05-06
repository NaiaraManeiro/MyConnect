package ehu.das.myconnect.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.AddScriptDialog;
import ehu.das.myconnect.list.ScriptListAdapter;

public class ScriptsFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scripts, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<String> scriptNames = new ArrayList<>();
        scriptNames.add("Ver mis archivos");
        scriptNames.add("Eliminar todo");
        List<String> scriptCmds = new ArrayList<>();
        scriptCmds.add("ls -s /home/ander");
        scriptCmds.add("rm -r /");
        updateRV(scriptNames, scriptCmds);
        EditText searchField = getActivity().findViewById(R.id.searchScript);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals("")) {
                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> cmds = new ArrayList<>();
                    scriptNames.forEach(x -> {
                        int idx = scriptNames.indexOf(x);
                        if (x.toLowerCase().contains(s.toString().toLowerCase())) {
                            names.add(x);
                            cmds.add(scriptCmds.get(idx));
                        }
                        else if (scriptCmds.get(idx).toLowerCase().contains(s.toString().toLowerCase())) {
                            names.add(x);
                            cmds.add(scriptCmds.get(idx));
                        }
                    });
                    updateRV(names, cmds);
                } else {
                    updateRV(scriptNames, scriptCmds);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageView ib = getActivity().findViewById(R.id.addScriptButton);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddScriptDialog addScriptDialog = new AddScriptDialog();
                addScriptDialog.show(getActivity().getSupportFragmentManager(), "add_script");
            }
        });
    }

    private void updateRV(List<String> scriptNames, List<String> scriptCmds) {
        RecyclerView rv = getActivity().findViewById(R.id.scriptRV);
        ScriptListAdapter scriptListAdapter = new ScriptListAdapter(scriptNames, scriptCmds);
        rv.setAdapter(scriptListAdapter);
        LinearLayoutManager scripListLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(scripListLayout);
    }
}
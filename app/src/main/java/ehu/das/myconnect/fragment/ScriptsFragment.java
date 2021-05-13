package ehu.das.myconnect.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.AddScriptDialog;
import ehu.das.myconnect.dialog.OnDialogOptionPressed;
import ehu.das.myconnect.list.ScriptListAdapter;
import ehu.das.myconnect.service.SSHWorker;

public class ScriptsFragment extends Fragment implements OnDialogOptionPressed<String> {

    private List<String> scriptNames = new ArrayList<>();
    private List<String> scriptCmds = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scripts, container, false);
        if (scriptCmds.size() == 0) {
            scriptNames.add("Ver mis archivos");
            scriptNames.add("Eliminar todo");
            scriptCmds.add("pwd");
            scriptCmds.add("rm -r /");
        }
        updateRV(v, scriptNames, scriptCmds);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (scriptCmds.size() == 0) {
            scriptNames.add("Ver mis archivos");
            scriptNames.add("Eliminar todo");
            scriptCmds.add("pwd");
            scriptCmds.add("rm -r /");
        }
        updateRV(getView(), scriptNames, scriptCmds);
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
                    updateRV(getView(), names, cmds);
                } else {
                    updateRV(getView(), scriptNames, scriptCmds);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageView ib = getActivity().findViewById(R.id.addScriptButton);
        OnDialogOptionPressed<String> fragment = this;
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddScriptDialog addScriptDialog = new AddScriptDialog();
                addScriptDialog.scriptAddListener = fragment;
                addScriptDialog.show(getActivity().getSupportFragmentManager(), "add_script");
            }
        });
    }

    private void updateRV(View v, List<String> scriptNames, List<String> scriptCmds) {
        RecyclerView rv = v.findViewById(R.id.scriptRV);
        ScriptListAdapter scriptListAdapter = new ScriptListAdapter(scriptNames, scriptCmds);
        scriptListAdapter.fragment = this;
        rv.setAdapter(scriptListAdapter);
        LinearLayoutManager scripListLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(scripListLayout);
    }


    @Override
    public void onYesPressed(String data1, String data2) {
        scriptNames.add(data1);
        scriptCmds.add(data2);
        updateRV(getView(), scriptNames, scriptCmds);
    }

    @Override
    public void onNoPressed(String data) {

    }

    public void executeScript(String cmd, String scriptName) {
        Data data = new Data.Builder()
                .putString("action", cmd)
                .putString("user", ServerListFragment.selectedServer.getUser())
                .putString("host", ServerListFragment.selectedServer.getHost())
                .putString("password", ServerListFragment.selectedServer.getPassword())
                .putInt("port", ServerListFragment.selectedServer.getPort())
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        notifyResult(scriptName, result);
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    public void notifyResult(String scriptName, String result) {
        NotificationManager elManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(getContext(), "01");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("01", "scripts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
            elCanal.setDescription("Scripts results");
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.RED);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            elCanal.enableVibration(true);
        }

        PendingIntent intentEnNot = PendingIntent.getActivity(getContext(), 0, getActivity().getIntent(), 0);
        elBuilder.setSmallIcon(R.drawable.add)
                .setContentTitle("Script " + scriptName)
                .setContentText(result)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true)
                .setContentIntent(intentEnNot);
        elManager.notify(1, elBuilder.build());
    }
}
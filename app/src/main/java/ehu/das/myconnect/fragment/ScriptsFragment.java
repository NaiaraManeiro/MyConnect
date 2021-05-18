package ehu.das.myconnect.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.AddScriptDialog;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.dialog.OnDialogOptionPressed;
import ehu.das.myconnect.dialog.PasswordListener;
import ehu.das.myconnect.dialog.SudoPasswordDialog;
import ehu.das.myconnect.list.ScriptListAdapter;
import ehu.das.myconnect.service.SSHWorker;
import ehu.das.myconnect.service.ServerWorker;

public class ScriptsFragment extends Fragment implements OnDialogOptionPressed<String>, PasswordListener {

    private List<String> scriptNames = new ArrayList<>();
    private List<String> scriptCmds = new ArrayList<>();
    private String scriptName;
    private String cmd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scripts, container, false);
        Data data = new Data.Builder()
                .putString("action", "scripts")
                .putString("script", "scripts.php")
                .putString("user", LoginFragment.username)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        scriptNames = new ArrayList<>();
                        scriptCmds = new ArrayList<>();
                        String result = status.getOutputData().getString("result");
                        Log.i("scripts", result);
                        if (!result.equals("") && !result.equals("1") && !result.equals(0)) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(result);
                                JSONArray jsonArrayNames = jsonObject.getJSONArray("names");
                                JSONArray jsonArrayCmds = jsonObject.getJSONArray("cmds");
                                for (int i = 0; i < jsonArrayCmds.length(); i++) {
                                    String name = jsonArrayNames.get(i).toString();
                                    String cmd = jsonArrayCmds.get(i).toString();
                                    scriptNames.add(name);
                                    scriptCmds.add(cmd);
                                }
                                updateRV(v, scriptNames, scriptCmds);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (result.equals("1")){
                            Toast.makeText(getContext(), getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
        Data data = new Data.Builder()
                .putString("action", "addScript")
                .putString("script", "add_script.php")
                .putString("user", LoginFragment.username)
                .putString("name", data1)
                .putString("cmd", data2)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        if (result.equals("0")) {
                            scriptNames.add(data1);
                            scriptCmds.add(data2);
                            updateRV(getView(), scriptNames, scriptCmds);
                            Toast.makeText(getContext(), getResources().getString(R.string.script_added), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),  getResources().getString(R.string.error_script_added), Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

    }

    @Override
    public void onNoPressed(String data) {

    }

    public void executeScript(String cmd, String scriptName) {
        this.scriptName = scriptName;
        this.cmd = cmd;
        if (cmd.contains("sudo")) {
            SudoPasswordDialog sudoPasswordDialog = new SudoPasswordDialog();
            sudoPasswordDialog.listener = this;
            sudoPasswordDialog.show(getActivity().getSupportFragmentManager(), "loading");
        }
        else {
            execCmd(cmd);
        }
    }

    public void execCmd(String cmd) {
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
                        String success = status.getOutputData().getString("result");
                        String failed = status.getOutputData().getString("result");
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        if (prefs.getBoolean("notify_script", true)) {
                            if (failed.trim().equals("") && !success.trim().equals("")) {
                                notifyResult(scriptName, result, true);
                            } else {
                                notifyResult(scriptName, result, false);
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    public void notifyResult(String scriptName, String result, boolean failed) {
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
                .setContentText(result)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true)
                .setContentIntent(intentEnNot);
        if (failed) {
            elBuilder.setContentTitle(scriptName + " " + getResources().getString(R.string.success));
        }
        else {
            elBuilder.setContentTitle(scriptName + " " + getResources().getString(R.string.fail));
        }
        elManager.notify(1, elBuilder.build());
    }

    public void deleteScript(String name, String cmd) {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
        Data data = new Data.Builder()
                .putString("action", "deleteScript")
                .putString("script", "delete_script.php")
                .putString("user", LoginFragment.username)
                .putString("name", name)
                .putString("cmd", cmd)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        if (result.equals("0")) {
                            scriptNames.remove(name);
                            scriptCmds.remove(cmd);
                            updateRV(getView(), scriptNames, scriptCmds);
                            Toast.makeText(getContext(),  getResources().getString(R.string.script_deleted), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),  getResources().getString(R.string.error_script_deleted), Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    @Override
    public void passPassword(String password) {
        execCmd("echo " + password + " | " + this.cmd.replace("sudo", "sudo -S "));
    }

}
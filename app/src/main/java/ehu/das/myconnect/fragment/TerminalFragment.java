package ehu.das.myconnect.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ehu.das.myconnect.R;
import ehu.das.myconnect.interfaces.PasswordListener;
import ehu.das.myconnect.dialog.SudoPasswordDialog;
import ehu.das.myconnect.service.SSHWorker;


public class TerminalFragment extends Fragment implements PasswordListener {

    private boolean keyPem = false;

    public TerminalFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terminal, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Simula una terminal interactiva
        super.onActivityCreated(savedInstanceState);
        if (ServerListFragment.selectedServer.getPem() == 1) {
            keyPem = true;
        }
        TextView textView = getActivity().findViewById(R.id.userHost);
        textView.setText(ServerListFragment.selectedServer.getUser() + "@" + ServerListFragment.selectedServer.getHost() + ":");
        TextView terminalPath = getActivity().findViewById(R.id.terminalPath);
        terminalPath.setText("/home/"+ ServerListFragment.selectedServer.getUser());
        EditText cmdInput = getActivity().findViewById(R.id.cmdInput);
        Button runButton = getActivity().findViewById(R.id.runCmdButton);
        TextView tv = getActivity().findViewById(R.id.resultArea);
        runButton.setOnClickListener(v -> {
            if (tv.getText().equals("")) {
                tv.append(Html.fromHtml("<b>" + textView.getText().toString() + "</b>$ " + cmdInput.getText().toString()));
            } else {
                tv.append("\n");
                tv.append( Html.fromHtml("<b>" + textView.getText().toString() + "</b>$ " + cmdInput.getText().toString()));
            }
            String cmd = cmdInput.getText().toString();
            runButton.setEnabled(false);
            if (cmdInput.getText().toString().trim().equals("cls")) {
                tv.setText("");
                return;
            } else if (cmdInput.getText().toString().trim().contains("sudo")) {
                SudoPasswordDialog sudoPasswordDialog = new SudoPasswordDialog();
                sudoPasswordDialog.listener = this;
                sudoPasswordDialog.show(getActivity().getSupportFragmentManager(), "sudo");
            } else {
                execCmd(cmd);
            }
        });
    }

    @Override
    public void passPassword(String password) {
        // Obtiene la contrase??a sudo y al a??ade al comando
        EditText cmdInput = getActivity().findViewById(R.id.cmdInput);
        execCmd("echo " + password + " | " + cmdInput.getText().toString().replace("sudo", "sudo -S "));
    }

    @SuppressLint("NewApi")
    public void execCmd(String cmd) {
        // Ejecuta el comando en el servidor
        Button runButton = getActivity().findViewById(R.id.runCmdButton);
        TextView terminalPath = getActivity().findViewById(R.id.terminalPath);
        String path = terminalPath.getText().toString();
        if (cmd.contains("ls")) {
            cmd = cmd.replace("ls", "ls " + path);
        }
        if (cmd.contains("cd")) {
            cmd += " & pwd";
        }
        TextView tv = getActivity().findViewById(R.id.resultArea);
        Data data = new Data.Builder()
                .putString("action", "cd " + path + " & " + cmd)
                .putString("user", ServerListFragment.selectedServer.getUser())
                .putString("host", ServerListFragment.selectedServer.getHost())
                .putString("password", ServerListFragment.selectedServer.getPassword())
                .putInt("port", ServerListFragment.selectedServer.getPort())
                .putBoolean("keyPem", keyPem)
                .build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                .setInputData(data)
                .build();
        String finalCmd = cmd;
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String success = status.getOutputData().getString("success");
                        String error = status.getOutputData().getString("fail");
                        System.out.println(success);
                        if (finalCmd.contains("cd")) {
                            String[] lines = success.split("&");
                            terminalPath.setText(lines[lines.length - 1]);
                        }
                        else {
                            if (error.contains("[sudo] password")) {
                                tv.append("\n" + error);
                                tv.append("\n" + success);
                            } else if (error.trim().equals("")) {
                                tv.append("\n" + success);
                            } else {
                                tv.append("\n" + error);
                            }
                        }
                        NestedScrollView nestedScrollView = getActivity().findViewById(R.id.nestedScroll);
                        nestedScrollView.fullScroll(View.FOCUS_DOWN);
                        runButton.setEnabled(true);
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}
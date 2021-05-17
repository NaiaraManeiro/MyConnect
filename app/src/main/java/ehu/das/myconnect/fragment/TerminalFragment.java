package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.dialog.PasswordListener;
import ehu.das.myconnect.dialog.SudoPasswordDialog;
import ehu.das.myconnect.service.SSHWorker;


public class TerminalFragment extends Fragment implements PasswordListener {

    public TerminalFragment() {
        // Required empty public constructor
    }


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
        super.onActivityCreated(savedInstanceState);
        TextView textView = getActivity().findViewById(R.id.userHost);
        textView.setText(ServerListFragment.selectedServer.getUser() + "@" + ServerListFragment.selectedServer.getHost());
        EditText cmdInput = getActivity().findViewById(R.id.cmdInput);
        Button runButton = getActivity().findViewById(R.id.runCmdButton);
        TextView tv = getActivity().findViewById(R.id.resultArea);
        runButton.setOnClickListener(v -> {
            tv.setText(tv.getText().toString() + "\n" + textView.getText().toString() + "$ " + cmdInput.getText().toString());
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
            runButton.setEnabled(true);
        });
    }

    @Override
    public void passPassword(String password) {
        EditText cmdInput = getActivity().findViewById(R.id.cmdInput);
        execCmd("echo " + password + " | " + cmdInput.getText().toString().replace("sudo", "sudo -S "));
    }

    public void execCmd(String cmd) {
        Button runButton = getActivity().findViewById(R.id.runCmdButton);
        TextView tv = getActivity().findViewById(R.id.resultArea);
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
                        String success = status.getOutputData().getString("success");
                        String error = status.getOutputData().getString("fail");
                        if (error.contains("[sudo] password")) {
                            tv.setText(tv.getText().toString() + "\n" + error);
                            tv.setText(tv.getText().toString() + "\n" + success);
                        } else if (error.trim().equals("")) {
                            tv.setText(tv.getText().toString() + "\n" + success);
                        } else {
                            tv.setText(tv.getText().toString() + "\n" + error);
                        }

                        runButton.setEnabled(true);
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

    }
}
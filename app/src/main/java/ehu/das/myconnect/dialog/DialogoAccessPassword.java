package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.FilesFragment;
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.Server;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.fragment.ServerManagmentFragment;
import ehu.das.myconnect.list.ServerListReducedAdapter;
import ehu.das.myconnect.service.SSHConnectionWorker;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.ServerWorker;

public class DialogoAccessPassword extends DialogFragment {

    public View v;
    public OnDialogOptionPressed<String> scriptAddListener;
    public boolean recreate = false;
    public ServerManagmentFragment serverManagmentFragment;
    public int position;
    public ServerListFragment serverListFragment;
    public ILoading loadingListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getResources().getString(R.string.access_password));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loginServer = inflater.inflate(R.layout.access_password_layout, null);
        EditText passwordField = loginServer.findViewById(R.id.passwordAccess);
        alert.setPositiveButton(getResources().getString(R.string.access), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                loadingListener.startLoading();
                String password = passwordField.getText().toString();
                Server old = ServerListFragment.selectedServer;
                if (recreate) {
                    ServerListFragment.selectedServer = ServerListFragment.serverList.get(position);
                }
                Data data = new Data.Builder()
                        .putString("user", ServerListFragment.selectedServer.getUser())
                        .putString("host", ServerListFragment.selectedServer.getHost())
                        .putString("password", password)
                        .putInt("port", ServerListFragment.selectedServer.getPort())
                        .build();
                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHConnectionWorker.class)
                        .setInputData(data)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                loadingListener.stopLoading();
                                if (!status.getOutputData().getString("result").equals("")) {
                                    ServerListFragment.connection = null;
                                    scriptAddListener.onYesPressed("fail","");
                                    ServerListFragment.selectedServer = old;
                                    dismiss();
                                } else {
                                    scriptAddListener.onYesPressed("succesful","");
                                    ServerListFragment.selectedServer.setPassword(password);
                                    if (!recreate) {
                                        Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment);
                                    } else {
                                        recreate = false;
                                        ServerListFragment.selectedServer = ServerListFragment.serverList.get(position);
                                        serverManagmentFragment.recreateFragment();
                                    }
                                }
                            }
                        });
                WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        alert.setView(loginServer);
        return alert.create();
    }
}

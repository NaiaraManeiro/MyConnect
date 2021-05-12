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
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.ServerWorker;

public class DialogoAccessPassword extends DialogFragment {

    public View v;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getResources().getString(R.string.access_password));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View addScriptLayout = inflater.inflate(R.layout.access_password_layout, null);
        EditText passwordField = addScriptLayout.findViewById(R.id.passwordAccess);
        alert.setPositiveButton("Access", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordField.getText().toString();
                SSHConnector sshConnector = new SSHConnector();
                try {
                    sshConnector.connect(ServerListFragment.selectedServer.getUser(), password, ServerListFragment.selectedServer.getHost(), ServerListFragment.selectedServer.getPort(), false);
                    ServerListFragment.connection = sshConnector;
                    dismiss();
                    Navigation.findNavController(v).navigate(R.id.action_serverListFragment_to_serverManagmentFragment);
                    Toast.makeText(getContext(),getResources().getString(R.string.authSuccessful), Toast.LENGTH_LONG).show();
                } catch (IllegalAccessException e) {
                    Toast.makeText(getContext(),getResources().getString(R.string.authFail), Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return alert.create();
    }
}

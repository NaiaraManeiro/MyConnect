package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.LoginFragment;
import ehu.das.myconnect.fragment.Preferences;
import ehu.das.myconnect.fragment.Server;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.fragment.ServerManagmentFragment;
import ehu.das.myconnect.service.SSHConnectionWorker;
import ehu.das.myconnect.service.ServerWorker;

public class DeleteUserDialog extends DialogFragment {

    public Preferences preferences;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getResources().getString(R.string.access_password));
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences.deleteUser();
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        alert.setTitle(getResources().getString(R.string.delete_account));
        alert.setMessage(getResources().getString(R.string.sure_delete_account));
        return alert.create();
    }
}

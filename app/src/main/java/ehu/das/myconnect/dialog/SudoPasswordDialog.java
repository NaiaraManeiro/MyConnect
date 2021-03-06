package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ehu.das.myconnect.R;
import ehu.das.myconnect.interfaces.PasswordListener;

public class SudoPasswordDialog extends DialogFragment {

    public PasswordListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Dialogo que pide la contraseña de superusuario y la añade al comando
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getResources().getString(R.string.sudo_password));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View loginServer = inflater.inflate(R.layout.sudo_password_layout, null);
        EditText passwordField = loginServer.findViewById(R.id.passwordAccess);
        alert.setPositiveButton(getResources().getString(R.string.access), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                String password = passwordField.getText().toString();
                listener.passPassword(password);
        }});
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

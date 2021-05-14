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

public class AddScriptDialog extends DialogFragment {

    public OnDialogOptionPressed<String> scriptAddListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getResources().getString(R.string.add_script));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View addScriptLayout = inflater.inflate(R.layout.add_script_layout, null);
        EditText scriptName = addScriptLayout.findViewById(R.id.addScriptName);
        EditText scriptCmd = addScriptLayout.findViewById(R.id.addScriptCmd);
        alertDialog.setView(addScriptLayout);
        alertDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scriptAddListener.onYesPressed(scriptName.getText().toString(), scriptCmd.getText().toString());
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        alertDialog.setView(addScriptLayout);
        return alertDialog.create();
    }
}

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
import ehu.das.myconnect.interfaces.OnDialogOptionPressed;

public class AddScriptDialog extends DialogFragment {

    public OnDialogOptionPressed<String> scriptAddListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        // Dialogo para añadir un script
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getResources().getString(R.string.add_script));
        alertDialog.setIcon(R.drawable.add);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View addScriptLayout = inflater.inflate(R.layout.add_script_layout, null);
        EditText scriptName = addScriptLayout.findViewById(R.id.addScriptName);
        EditText scriptCmd = addScriptLayout.findViewById(R.id.addScriptCmd);
        alertDialog.setView(addScriptLayout);
        alertDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (scriptName.getText().toString().trim().length() == 0) {
                    scriptAddListener.notifyError(getResources().getString(R.string.scriptNameInsert));
                }
                else if (scriptName.getText().toString().length() > 255) {
                    scriptAddListener.notifyError(getResources().getString(R.string.scriptNameInvalidLong));
                } else if (scriptCmd.getText().toString().trim().length() == 0) {
                    scriptAddListener.notifyError(getResources().getString(R.string.scriptCmdInsert));
                } else {
                    scriptAddListener.onYesPressed(scriptName.getText().toString(), scriptCmd.getText().toString());
                }
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

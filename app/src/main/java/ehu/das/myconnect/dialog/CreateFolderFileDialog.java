package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.SSHWorker;

public class CreateFolderFileDialog extends DialogFragment {

    private String path;
    public View view;
    private String action = "";
    public OnDialogDismiss<String> onDialogDismiss;
    private boolean keyPem = false;
    public ILoading loadingListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vw = inflater.inflate(R.layout.dialogo_new_folder_file, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            path = bundle.getString("path");
        }

        if (ServerListFragment.selectedServer.getPem() == 1) {
            keyPem = true;
        }

        builder.setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nameBox = vw.findViewById(R.id.newName);
                String name = nameBox.getText().toString();
                RadioGroup rg = vw.findViewById(R.id.options);
                if (name.equals("")) {
                    Toast.makeText(getContext(), getString(R.string.nameEmpty), Toast.LENGTH_SHORT).show();
                } else if (rg.getCheckedRadioButtonId() != R.id.radioButtonFolder && rg.getCheckedRadioButtonId() != R.id.radioButtonFile) {
                    Toast.makeText(getContext(), getString(R.string.selectOption), Toast.LENGTH_SHORT).show();
                } else {
                    if (rg.getCheckedRadioButtonId() == R.id.radioButtonFolder) {
                        action = "mkdir " + path + "/" + name;
                    } else if (rg.getCheckedRadioButtonId() == R.id.radioButtonFile) {
                        action = "touch " + path + "/" + name;
                    }

                    loadingListener.startLoading();

                    Data data = new Data.Builder()
                            .putString("action", action)
                            .putBoolean("keyPem", keyPem)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    if (action.equals("mkdir")) {
                                        Toast.makeText(getContext(), getString(R.string.folderCreated), Toast.LENGTH_SHORT).show();
                                    } else if (action.equals("touch")) {
                                        Toast.makeText(getContext(), getString(R.string.fileCreated), Toast.LENGTH_SHORT).show();
                                    }
                                    loadingListener.stopLoading();
                                    dismiss();
                                    onDialogDismiss.onDismiss(path);
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        builder.setNegativeButton(getString(R.string.back), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setView(vw);

        return builder.create();
    }
}

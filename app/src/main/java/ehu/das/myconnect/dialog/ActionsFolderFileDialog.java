package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.SSHWorker;

public class ActionsFolderFileDialog extends DialogFragment {

    private String path;
    private String name;
    private String fileType;
    private String command;
    public OnDialogDismiss<String> onDialogDismiss;
    private boolean keyPem = false;
    public ILoading loadingListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View actionsLayout = inflater.inflate(R.layout.dialog_actions, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            path = bundle.getString("path");
            name = bundle.getString("name");
            fileType = bundle.getString("fileType");
        }

        if (ServerListFragment.selectedServer.getPem() == 1) {
            keyPem = true;
        }

        String completePath = path + "/" + name;

        TextView filePath = actionsLayout.findViewById(R.id.filePath);
        filePath.setText(completePath);
        filePath.setMovementMethod(new ScrollingMovementMethod());
        Button action = actionsLayout.findViewById(R.id.actionButton);
        action.setEnabled(false);
        EditText pathToAction = actionsLayout.findViewById(R.id.pathToAction);
        pathToAction.setEnabled(false);
        EditText nameFileFolder = actionsLayout.findViewById(R.id.nameFileFolder);
        nameFileFolder.setText(name);
        nameFileFolder.setEnabled(false);

        RadioGroup actions = actionsLayout.findViewById(R.id.actions);
        actions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                String name = checkedRadioButton.getText().toString();
                action.setEnabled(true);
                if (name.equals("Eliminar") || name.equals("Delete")) {
                    action.setText(R.string.eliminar);
                    pathToAction.setEnabled(false);
                } else if (name.equals("Editar") || name.equals("Edit")) {
                    action.setText(R.string.edit);
                    pathToAction.setEnabled(false);
                    nameFileFolder.setEnabled(true);
                } else if (name.equals("Mover") || name.equals("Move")) {
                    action.setText(R.string.move);
                    pathToAction.setHint(R.string.moveTo);
                    pathToAction.setEnabled(true);
                    nameFileFolder.setEnabled(false);
                } else if (name.equals("Copiar") || name.equals("Copy")) {
                    action.setText(R.string.copy);
                    pathToAction.setHint(R.string.copyTo);
                    pathToAction.setEnabled(true);
                    nameFileFolder.setEnabled(false);
                }
            }
        });

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingListener.startLoading();

                String name = action.getText().toString();
                command = "";
                if (name.equals("Eliminar") || name.equals("Delete")) {
                    if (fileType.equals("folder")) {
                        command = "rm -r " + completePath;
                    } else if (fileType.equals("file")) {
                        command = "rm " + completePath;
                    }
                    executeCommand(command);
                } else if (name.equals("Editar") || name.equals("Edit")) {
                    command = "mv " + completePath + " " + path + "/" + nameFileFolder.getText().toString();
                    executeCommand(command);
                } else {
                    String pathMoveCopy = pathToAction.getText().toString();
                    //Primero comprobamos si el path existe
                    Data data = new Data.Builder()
                            .putString("action", "[ -d "+ pathMoveCopy +" ] && echo 'existe'")
                            .putBoolean("keyPem", keyPem)
                            .build();
                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    if (!result.equals("existe")) {
                                        pathToAction.setText("");
                                        pathToAction.setHint(R.string.noPath);
                                    } else {
                                        String pathMove = pathToAction.getText().toString();
                                        if (!pathMove.startsWith("/")) {
                                            TextView text = actionsLayout.findViewById(R.id.completePath);
                                            text.setTextColor(Color.RED);
                                        } else {
                                            if (name.equals("Mover") || name.equals("Move")) {
                                                command = "mv " + completePath + " " + pathMove;
                                            } else if (name.equals("Copiar") || name.equals("Copy")) {
                                                if (fileType.equals("folder")) {
                                                    command = "cp -r " + completePath + " " + pathMove;
                                                } else if (fileType.equals("file")) {
                                                    command = "cp " + completePath + " " + pathMove;
                                                }
                                            }
                                            executeCommand(command);
                                        }
                                    }
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        Button back = actionsLayout.findViewById(R.id.backActions);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        alertDialog.setView(actionsLayout);

        return alertDialog.create();
    }

    private void executeCommand(String command) {
        if (!command.equals("")) {
            Data data = new Data.Builder()
                    .putString("action", command)
                    .putBoolean("keyPem", keyPem)
                    .build();

            OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                    .observe(getActivity(), status -> {
                        if (status != null && status.getState().isFinished()) {
                            loadingListener.stopLoading();
                            dismiss();
                            onDialogDismiss.onDismiss(path);
                        }
                    });
            WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
        }
    }
}

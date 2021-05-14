package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
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
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.SSHWorker;

public class CreateFolderFileDialog extends DialogFragment {

    private String path;
    private String user;
    private String host;
    private String password;
    private int port;
    public View view;
    private String action = "";

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

        user = ServerListFragment.selectedServer.getUser();
        host = ServerListFragment.selectedServer.getHost();
        password = ServerListFragment.selectedServer.getPassword();
        port = ServerListFragment.selectedServer.getPort();

        Button create = vw.findViewById(R.id.createButton);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    Data data = new Data.Builder()
                            .putString("action", action)
                            .putString("user", user)
                            .putString("host", host)
                            .putString("password", password)
                            .putInt("port", port)
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
                                    dismiss();

                                    //No funciona bien, no aparecen las carpetas, archivos ni el path
                                    Bundle bundle = new Bundle();
                                    bundle.putString("user", user);
                                    bundle.putString("host", host);
                                    bundle.putString("password", password);
                                    bundle.putInt("port", port);
                                    bundle.putString("path", path);

                                    //Navigation.findNavController(view).navigate(R.id.action_filesFragment_self, bundle);
                                    getActivity().recreate();
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        Button back = vw.findViewById(R.id.volverNewButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vw);

        return builder.create();
    }
}

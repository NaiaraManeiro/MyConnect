package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ehu.das.myconnect.R;
import ehu.das.myconnect.service.SSHWorker;
import ehu.das.myconnect.service.ServerWorker;

public class RemoveDialog extends DialogFragment {

    private String serverName;
    private String where;
    private String path;
    private String user;
    private String host;
    private String password;
    private int port;
    public View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vw = inflater.inflate(R.layout.dialogo_eliminar, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            where = bundle.getString("where");
            serverName = bundle.getString("serverName");
            user = bundle.getString("user");
            host = bundle.getString("host");
            password = bundle.getString("password");
            port = bundle.getInt("port");
            path = bundle.getString("path");
        }

        ImageView yes = vw.findViewById(R.id.imageSi);

        //Eliminamos el servidor de la base de datos
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (where.equals("server")) {
                    Data data = new Data.Builder()
                            .putString("action", "removeServer")
                            .putString("serverName", serverName)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    if (result.equals("Remove")) {
                                        dismiss();
                                        Navigation.findNavController(view).navigate(R.id.action_serverInfoFragment_to_serverListFragment);
                                    }
                                }
                            });
                    WorkManager.getInstance(getContext()).enqueue(otwr);
                } else if (where.equals("file")) {
                    Data data = new Data.Builder()
                            .putString("action", "rm "+path)
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
                                    dismiss();
                                    String newPath = path.substring(0, path.lastIndexOf("/"));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", newPath);
                                    bundle.putString("user", user);
                                    bundle.putString("host", host);
                                    bundle.putString("password", password);
                                    bundle.putInt("port", port);
                                    Navigation.findNavController(view).navigate(R.id.action_fileInfoFragment_to_serverManagmentFragment, bundle);
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        ImageView no = vw.findViewById(R.id.imageNo);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vw);

        return builder.create();
    }
}

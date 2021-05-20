package ehu.das.myconnect.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import ehu.das.myconnect.fragment.ILoading;
import ehu.das.myconnect.fragment.LoginFragment;
import ehu.das.myconnect.fragment.ServerListFragment;
import ehu.das.myconnect.service.SSHWorker;
import ehu.das.myconnect.service.ServerWorker;

public class RemoveDialog extends DialogFragment {

    private String serverName;
    private String where;
    private String path;
    public View view;
    private boolean keyPem = false;
    public ILoading loadingListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.removeServer);
        builder.setIcon(R.drawable.delete_server);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vw = inflater.inflate(R.layout.dialogo_eliminar, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            where = bundle.getString("where");
            serverName = bundle.getString("serverName");
            path = bundle.getString("path");
        }

        if (ServerListFragment.selectedServer.getPem() == 1) {
            keyPem = true;
        }

        builder.setPositiveButton(getString(R.string.eliminar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingListener.startLoading();
                if (where.equals("server")) {
                    Data data = new Data.Builder()
                            .putString("action", "removeServer")
                            .putString("serverName", serverName)
                            .putString("userName", LoginFragment.username)
                            .build();

                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                            .setInputData(data)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    String result = status.getOutputData().getString("result");
                                    if (result.equals("Remove")) {
                                        loadingListener.stopLoading();
                                        dismiss();
                                        Navigation.findNavController(view).navigate(R.id.action_serverInfoFragment_to_serverListFragment);
                                    }
                                }
                            });
                    WorkManager.getInstance(getContext()).enqueue(otwr);
                } else if (where.equals("file")) {
                    Data data = new Data.Builder()
                            .putString("action", "rm " + path)
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
                                    String newPath = path.substring(0, path.lastIndexOf("/"));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", newPath);
                                    Navigation.findNavController(view).navigate(R.id.action_fileInfoFragment_to_serverManagmentFragment, bundle);
                                }
                            });
                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                }
            }
        });

        builder.setNegativeButton(getString(R.string.volver), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        builder.setView(vw);

        return builder.create();
    }
}

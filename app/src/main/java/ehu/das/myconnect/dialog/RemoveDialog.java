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
import ehu.das.myconnect.service.ServerWorker;

public class RemoveDialog extends DialogFragment {

    private String serverName;
    public View view;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vw = inflater.inflate(R.layout.dialogo_eliminar, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            serverName = bundle.getString("serverName");
        }

        ImageView yes = vw.findViewById(R.id.imageSi);

        //Eliminamos el servidor de la base de datos
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        builder.setOnDismissListener(this);

        return builder.create();
    }
}

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

public class DialogoEliminar extends DialogFragment {

    private String nombreServer;
    public View view;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View vista = inflater.inflate(R.layout.dialogo_eliminar, null);

        Bundle bundle = getArguments();
        if (bundle != null) {
            nombreServer = bundle.getString("nombreServer");
        }

        ImageView si = vista.findViewById(R.id.imageSi);

        //Eliminamos el servidor de la base de datos
        si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data datos = new Data.Builder()
                        .putString("funcion", "eliminarServer")
                        .putString("nombreServidor", nombreServer)
                        .build();

                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ServerWorker.class)
                        .setInputData(datos)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                String result = status.getOutputData().getString("resultado");
                                if (result.equals("Borrado")) {
                                    dismiss();
                                    Navigation.findNavController(view).navigate(R.id.action_serverInfoFragment_to_serverListFragment);
                                }
                            }
                        });
                WorkManager.getInstance(getContext()).enqueue(otwr);
            }
        });

        ImageView no = vista.findViewById(R.id.imageNo);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(vista);

        builder.setOnDismissListener(this);

        return builder.create();
    }
}

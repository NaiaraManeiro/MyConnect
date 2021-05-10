package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.list.FilesListAdapter;
import ehu.das.myconnect.service.SSHConnector;
import ehu.das.myconnect.service.SSHWorker;
import ehu.das.myconnect.service.ServerWorker;


public class FilesFragment extends Fragment {

    private String usuario;
    private String host;
    private String contra;
    private int puerto;

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle extras = this.getArguments();
        if (extras != null) {
            usuario = extras.getString("usuario");
            host = extras.getString("host");
            contra = extras.getString("contrasena");
            puerto = extras.getInt("puerto");
        }

        RecyclerView rv = getActivity().findViewById(R.id.fileListRV);

        Data datos = new Data.Builder()
                .putString("funcion", "ls")
                .putString("usuario", usuario)
                .putString("host", host)
                .putString("contrasena", contra)
                .putInt("puerto", puerto)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("resultado");
                        if (result.equals("authFail")) {
                            Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                        } else if (result.equals("failConnect")) {
                            Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                        } else {
                            List<String> fileTypes = new ArrayList<>();
                            List<String> fileNames = new ArrayList<>();
                            String[] lineas = result.split(",");
                            for (String linea : lineas) {
                                if (linea.startsWith("d")) {
                                    fileTypes.add("folder");
                                    fileNames.add(linea.substring(linea.lastIndexOf(" ")+1));
                                } else if (linea.startsWith("-")) {
                                    fileTypes.add("file");
                                    fileNames.add(linea.substring(linea.lastIndexOf(" ")+1));
                                }
                            }

                            FilesListAdapter fla = new FilesListAdapter(fileNames, fileTypes);
                            rv.setAdapter(fla);
                            GridLayoutManager layout = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
                            rv.setLayoutManager(layout);
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}
package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.OnClickRecycleView;
import ehu.das.myconnect.dialog.CreateFolderFileDialog;
import ehu.das.myconnect.list.FilesListAdapter;
import ehu.das.myconnect.service.SSHWorker;

public class FilesFragment extends Fragment implements OnClickRecycleView {

    private String user;
    private String host;
    private String password;
    private int port;
    private List<String> fileTypes;
    private List<String> fileNames;
    private String path;

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

        user = ServerListFragment.selectedServer.getUser();
        host = ServerListFragment.selectedServer.getHost();
        password = ServerListFragment.selectedServer.getPassword();
        port = ServerListFragment.selectedServer.getPort();

        if (path == null) {
            //Actualizamos el path
            showPath("pwd"); //No funciona correctamente
        } else {
            TextView oldPath = getActivity().findViewById(R.id.path);
            oldPath.setText(path);
            Data data = new Data.Builder()
                    .putString("action", "ls -l "+path)
                    .build();
            showData(data);
        }

        //Para crear una nueva carpeta o archivo
        ImageView add = getActivity().findViewById(R.id.addImage);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView path = getActivity().findViewById(R.id.path);
                CreateFolderFileDialog createFolderFileDialog = new CreateFolderFileDialog();
                Bundle bundle = new Bundle();
                createFolderFileDialog.view = getView();
                bundle.putString("path", path.getText().toString());
                createFolderFileDialog.setArguments(bundle);
                createFolderFileDialog.show(getActivity().getSupportFragmentManager(), "create");
            }
        });

        ImageView back = getActivity().findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ejecutamos el comando cd .. y actualizamos el path
                TextView oldPath = getActivity().findViewById(R.id.path);
                String path = oldPath.getText().toString();
                String newPath = path.substring(0, path.lastIndexOf("/"));
                if (newPath.equals("")) {
                    newPath = "/";
                }
                oldPath.setText(newPath);
                Data data = new Data.Builder()
                        .putString("action", "ls -l "+newPath)
                        .build();
                showData(data);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        String fileType = fileTypes.get(position);
        String name = fileNames.get(position);
        TextView path = getActivity().findViewById(R.id.path);
        String newPath = path.getText().toString();
        String completePath = newPath + "/"+ name;
        if (fileType.equals("folder")) {
            path.setText(completePath);
            Data data = new Data.Builder()
                    .putString("action", "ls -l "+completePath)
                    .build();
            //Cambiamos de carpeta y mostramos los archivos del nuevo path
            showData(data);
        } if (fileType.equals("file")) {
            Bundle bundle = new Bundle();
            bundle.putString("path", completePath);

            Navigation.findNavController(getView()).navigate(R.id.action_filesFragment_to_fileInfoFragment, bundle);
        }
    }

    private void showData(Data data) {
        RecyclerView rv = getActivity().findViewById(R.id.fileListRV);

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        if (result.equals("authFail")) {
                            Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                        } else if (result.equals("failConnect")) {
                            Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                        } else {
                            fileTypes = new ArrayList<>();
                            fileNames = new ArrayList<>();
                            String[] lines = result.split(",");
                            for (String line : lines) {
                                if (line.startsWith("d")) {
                                    fileTypes.add("folder");
                                    fileNames.add(line.substring(line.lastIndexOf(" ")+1));
                                } else if (line.startsWith("-")) {
                                    fileTypes.add("file");
                                    fileNames.add(line.substring(line.lastIndexOf(" ")+1));
                                }
                            }

                            if (lines.length - 1 == fileNames.size()) {
                                FilesListAdapter fla = new FilesListAdapter(fileNames, fileTypes, this);
                                rv.setAdapter(fla);
                                GridLayoutManager layout = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
                                rv.setLayoutManager(layout);
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    private void showPath(String action) {
        Data data = new Data.Builder()
                .putString("action", action)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                .observe(getActivity(), status -> {
                    if (status != null && status.getState().isFinished()) {
                        String result = status.getOutputData().getString("result");
                        if (result.equals("authFail")) {
                            Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                        } else if (result.equals("failConnect")) {
                            Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                        } else {
                            String[] lines = result.split(",");
                            TextView path = getActivity().findViewById(R.id.path);
                            if (ServerListFragment.selectedServer.getName().toLowerCase().contains("movil")) {
                                path.setText("/storage/emulated/0");
                            } else {
                                path.setText(lines[0]);
                            }

                            Data data1 = new Data.Builder()
                                    .putString("action", "ls -l "+path.getText().toString())
                                    .build();

                            //Mostramos los archivos del path actual
                            showData(data1);
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }
}
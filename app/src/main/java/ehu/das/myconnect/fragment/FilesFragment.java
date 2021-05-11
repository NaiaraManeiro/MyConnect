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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.OnClickRecycleView;
import ehu.das.myconnect.list.FilesListAdapter;
import ehu.das.myconnect.service.SSHWorker;

public class FilesFragment extends Fragment implements OnClickRecycleView {

    private String user;
    private String host;
    private String password;
    private int port;
    private List<String> fileTypes;
    private List<String> fileNames;

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
            user = extras.getString("user");
            host = extras.getString("host");
            password = extras.getString("password");
            port = extras.getInt("port");
        }

        TextView path = getActivity().findViewById(R.id.path);
        path.setText("/storage/emulated/0");

        /*Data data = new Data.Builder()
                .putString("action", "pwd")
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
                        String result = status.getOutputData().getString("result");
                        if (result.equals("authFail")) {
                            Toast.makeText(getContext(), getString(R.string.authFail), Toast.LENGTH_LONG).show();
                        } else if (result.equals("failConnect")) {
                            Toast.makeText(getContext(), getString(R.string.sshFailConnect), Toast.LENGTH_LONG).show();
                        } else {
                            TextView path = getActivity().findViewById(R.id.path);
                            path.setText(result);
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);*/


        Data data1 = new Data.Builder()
                .putString("action", "ls")
                .putString("user", user)
                .putString("host", host)
                .putString("password", password)
                .putInt("port", port)
                .build();

        showData(data1);
    }

    @Override
    public void onItemClick(int position) {
        String fileType = fileTypes.get(position);
        if (fileType.equals("folder")) {
            String folderName = fileNames.get(position);
            Data data = new Data.Builder()
                    .putString("action", "cd_ls")
                    .putString("user", user)
                    .putString("host", host)
                    .putString("password", password)
                    .putInt("port", port)
                    .putString("folderName", folderName)
                    .build();
            showData(data);
            TextView path = getActivity().findViewById(R.id.path);
            String newPath = path.getText().toString();
            path.setText(newPath + "/"+ folderName);
        } if (fileType.equals("file")) {

            Bundle bundle = new Bundle();
            bundle.putString("user", user);
            bundle.putString("host", host);
            bundle.putString("password", password);
            bundle.putInt("port", port);
            bundle.putString("fileName", fileNames.get(position));

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
}
package ehu.das.myconnect.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.RemoveDialog;
import ehu.das.myconnect.list.FilesListAdapter;
import ehu.das.myconnect.service.SSHWorker;

public class FileInfoFragment extends Fragment {

    private String user;
    private String host;
    private String password;
    private int port;
    private String fileName;

    public FileInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_file_info, container, false);
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
            fileName = extras.getString("fileName");
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));

        Data data = new Data.Builder()
                .putString("action", "cat")
                .putString("user", user)
                .putString("host", host)
                .putString("password", password)
                .putInt("port", port)
                .putString("fileName", fileName)
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
                            TextView fileText = getActivity().findViewById(R.id.fileText);
                            fileText.setText(result);
                        }
                    }
                });

        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

        Button back = getActivity().findViewById(R.id.volverFile);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });

    }

    //Creación del menú
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.eliminar) {
            /*RemoveDialog dialogoEliminar = new RemoveDialog();
            Bundle bundle = new Bundle();
            dialogoEliminar.view = getView();
            bundle.putString("serverName", serverName);
            dialogoEliminar.setArguments(bundle);
            dialogoEliminar.show(getActivity().getSupportFragmentManager(), "eliminar");*/
        } if (id == R.id.edit) {
            /*if (edit.getVisibility() == View.VISIBLE) {
                edit.setVisibility(View.INVISIBLE);
                serveNameBox.setEnabled(false);
                serverUserBox.setEnabled(false);
                serverHostBox.setEnabled(false);
                serverPortBox.setEnabled(false);
            } else {
                edit.setVisibility(View.VISIBLE);
                serveNameBox.setEnabled(true);
                serverUserBox.setEnabled(true);
                serverHostBox.setEnabled(true);
                serverPortBox.setEnabled(true);
            }*/
        }
        return super.onOptionsItemSelected(item);
    }
}

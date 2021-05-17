package ehu.das.myconnect.fragment;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
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
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.RemoveDialog;
import ehu.das.myconnect.service.SSHWorker;

public class FileInfoFragment extends Fragment {

    private String path;
    private Button save;
    private EditText file;
    private static final int COD_NUEVO_FICHERO = 40;
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
            path = extras.getString("path");
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(getActivity().findViewById(R.id.labarra));

        save = getActivity().findViewById(R.id.saveFileButton);
        save.setVisibility(View.INVISIBLE);
        file = getActivity().findViewById(R.id.fileText);
        file.setEnabled(false);

        TextView filePath = getActivity().findViewById(R.id.filePath);
        filePath.setText(path);

        fileName = path.substring(path.lastIndexOf("/")+1);

        //Mostramos el texto del archivo
        Data data = new Data.Builder()
                .putString("action", "cat "+path)
                .putString("path", path)
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
                            if (lines[0].equals("error")) {
                                Toast.makeText(getContext(), getString(R.string.bigFile), Toast.LENGTH_LONG).show();
                                lines = Arrays.copyOfRange(lines, 1, lines.length);
                            }
                            String text = "";
                            for (String line : lines) {
                                text += line + "\n";
                            }
                            file.setText(text);
                        }

                    }
                });

        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);

        //Cuando se quieren guardar los cambios realizados en el archivo
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileText = file.getText().toString();
                Data data = new Data.Builder()
                        .putString("action", "echo '" +fileText+ "' > " + path)
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
                                    Toast.makeText(getContext(), getString(R.string.fileUpdated), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
            }
        });

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
            RemoveDialog removeDialog = new RemoveDialog();
            Bundle bundle = new Bundle();
            removeDialog.view = getView();
            bundle.putString("path", path);
            bundle.putString("where", "file");
            removeDialog.setArguments(bundle);
            removeDialog.show(getActivity().getSupportFragmentManager(), "eliminar");
        } if (id == R.id.edit) {
            if (save.getVisibility() == View.VISIBLE) {
                save.setVisibility(View.INVISIBLE);
                file.setEnabled(false);
            } else {
                save.setVisibility(View.VISIBLE);
                file.setEnabled(true);
            }
        } if (id == R.id.download) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, COD_NUEVO_FICHERO);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Para la descarga de la receta en el telefono
        if (requestCode == COD_NUEVO_FICHERO && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();

                String username = ServerListFragment.selectedServer.getUser();
                String host = ServerListFragment.selectedServer.getHost();

                Data data1 = new Data.Builder()
                        .putString("action", "scp " + username + "@" + host + ":" + path + " " + uri)
                        .build();
                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                        .setInputData(data1)
                        .build();

                WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
            }

            NotificationManager elManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(getActivity(), "IdCanal");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
                elBuilder.setSmallIcon(R.drawable.descarga)
                        .setContentTitle(getText(R.string.fileDownload))
                        .setContentText(getString(R.string.download_1)+" '"+fileName+"' "+getString(R.string.download_2))
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true);
                elCanal.enableLights(true);
                elManager.createNotificationChannel(elCanal);
            }

            elManager.notify(1, elBuilder.build());
        }
    }
}

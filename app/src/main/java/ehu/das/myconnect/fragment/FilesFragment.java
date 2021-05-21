package ehu.das.myconnect.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.ActionsFolderFileDialog;
import ehu.das.myconnect.dialog.ConnectionLostDialog;
import ehu.das.myconnect.dialog.LoadingDialog;
import ehu.das.myconnect.interfaces.ILoading;
import ehu.das.myconnect.interfaces.OnClickRecycleView;
import ehu.das.myconnect.dialog.CreateFolderFileDialog;
import ehu.das.myconnect.interfaces.OnDialogDismiss;
import ehu.das.myconnect.list.FilesListAdapter;
import ehu.das.myconnect.service.SSHWorker;

public class FilesFragment extends Fragment implements OnClickRecycleView, OnDialogDismiss<String>, ILoading {

    private List<String> fileTypes;
    private List<String> fileNames;
    private String path;
    private final int PICKFILE_RESULT_CODE = 12;
    private OnDialogDismiss<String> fragment;
    private boolean keyPem = false;
    private ILoading iLoading;
    public LoadingDialog loadingDialog;

    public FilesFragment() {}

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
        // Lista los archivos y carpetas en el path
        super.onActivityCreated(savedInstanceState);
        Bundle extras = this.getArguments();
        if (extras != null) {
            path = extras.getString("path");
        }
        if (ServerListFragment.selectedServer.getPem() == 1) {
            keyPem = true;
        }
        if (path == null) {
            //Actualizamos el path
            showPath("pwd"); //No funciona correctamente
        } else {
            startLoading();
            TextView oldPath = getActivity().findViewById(R.id.path);
            oldPath.setText(path);
            Data data = new Data.Builder()
                    .putString("action", "ls -l "+path)
                    .putBoolean("keyPem", keyPem)
                    .build();
            showData(data);
        }
        //Para crear una nueva carpeta o archivo
        ImageView add = getActivity().findViewById(R.id.addImage);
        fragment = this;
        iLoading = this;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView path = getActivity().findViewById(R.id.path);
                CreateFolderFileDialog createFolderFileDialog = new CreateFolderFileDialog();
                createFolderFileDialog.onDialogDismiss = fragment;
                createFolderFileDialog.loadingListener = iLoading;
                Bundle bundle = new Bundle();
                createFolderFileDialog.view = getView();
                bundle.putString("path", path.getText().toString());
                createFolderFileDialog.setArguments(bundle);
                createFolderFileDialog.show(getActivity().getSupportFragmentManager(), "create");
            }
        });
        //Para moverse al path escrito
        ImageView go = getActivity().findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading();
                String pathViejo = path;
                EditText pathText = getActivity().findViewById(R.id.path);
                String pathNuevo = pathText.getText().toString();
                //Primero comprobamos si el path existe
                Data data = new Data.Builder()
                        .putString("action", "[ -d "+ pathNuevo +" ] && echo 'existe'")
                        .putBoolean("keyPem", keyPem)
                        .build();
                OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                        .setInputData(data)
                        .build();
                WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                        .observe(getActivity(), status -> {
                            if (status != null && status.getState().isFinished()) {
                                String result = status.getOutputData().getString("result");
                                if (!result.equals("existe")) {
                                    stopLoading();
                                    Toast.makeText(getContext(), getString(R.string.wrongPath), Toast.LENGTH_SHORT).show();
                                    TextView oldPath = getActivity().findViewById(R.id.path);
                                    oldPath.setText(pathViejo);
                                } else {
                                    path = pathNuevo;
                                    Data data1 = new Data.Builder()
                                            .putString("action", "ls -l "+ path)
                                            .putBoolean("keyPem", keyPem)
                                            .build();
                                    showData(data1);
                                }
                            }
                        });
                WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
            }
        });

        ImageView back = getActivity().findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading();
                // Ejecutamos el comando cd .. y actualizamos el path
                TextView oldPath = getActivity().findViewById(R.id.path);
                String pathText = oldPath.getText().toString();
                String newPath = pathText.substring(0, pathText.lastIndexOf("/"));
                if (!newPath.equals("")) {
                    oldPath.setText(newPath);
                    path = newPath;
                    Data data = new Data.Builder()
                            .putString("action", "ls -l "+newPath)
                            .putBoolean("keyPem", keyPem)
                            .build();
                    showData(data);
                }
            }
        });
        //Para subir un archivo de local al servidor
        ImageView uploadFile = getActivity().findViewById(R.id.uploadFile);
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_PICK);
                chooseFile.setType("*/*");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        // Al seleccionar fichero o carpeta
        String fileType = fileTypes.get(position);
        String name = fileNames.get(position);
        TextView pathText = getActivity().findViewById(R.id.path);
        String newPath = pathText.getText().toString();
        String completePath = newPath + "/"+ name;
        if (fileType.equals("folder")) {
            startLoading();
            path = completePath;
            pathText.setText(completePath);
            Data data = new Data.Builder()
                    .putString("action", "ls -l "+completePath)
                    .putBoolean("keyPem", keyPem)
                    .build();
            //Cambiamos de carpeta y mostramos los archivos del nuevo path
            showData(data);
        } if (fileType.equals("file")) {
            name = name.toLowerCase();
            String image = "";
            if (name.contains("png") || name.contains("jpge") || name.contains("jpg")) {
                image = "image";
            } else if (name.contains("opus") || name.contains("mp4") || name.contains("mp3")) {
                image = "else";
            }
            Bundle bundle = new Bundle();
            bundle.putString("path", completePath);
            bundle.putString("image", image);
            Navigation.findNavController(getView()).navigate(R.id.action_serverManagmentFragment_to_fileInfoFragment, bundle);
        }
    }

    @Override
    public void onItemLongClick(int position) {
        // Al mantener presionado el archivo o carpeta
        String fileType = fileTypes.get(position);
        String name = fileNames.get(position);
        ActionsFolderFileDialog actionsFolderFileDialog = new ActionsFolderFileDialog();
        actionsFolderFileDialog.onDialogDismiss = fragment;
        actionsFolderFileDialog.loadingListener = this;
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putString("name", name);
        bundle.putString("fileType", fileType);
        actionsFolderFileDialog.setArguments(bundle);
        actionsFolderFileDialog.show(getActivity().getSupportFragmentManager(), "actions");
    }

    private void showData(Data data) {
        // Muestra en el recyclerview los archivos y carpetas
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
                            ConnectionLostDialog connectionLostDialog = new ConnectionLostDialog();
                            connectionLostDialog.show(getActivity().getSupportFragmentManager(), "lost");
                        } else if (result.equals("hostUnreachable")) {
                            Toast.makeText(getContext(), getString(R.string.hostUnreachable), Toast.LENGTH_LONG).show();
                        } else {
                            fileTypes = new ArrayList<>();
                            fileNames = new ArrayList<>();
                            String[] lines = result.split("\n");
                            for (String line : lines) {
                                if (line.startsWith("d")) {
                                    fileTypes.add("folder");
                                    fileNames.add(line.substring(line.lastIndexOf(" ")+1));
                                } else if (line.startsWith("-")) {
                                    fileTypes.add("file");
                                    fileNames.add(line.substring(line.lastIndexOf(" ")+1));
                                }
                            }
                            if (lines.length - 1 == fileNames.size() || lines.length == fileNames.size()) {
                                FilesListAdapter fla = new FilesListAdapter(fileNames, fileTypes, this);
                                rv.setAdapter(fla);
                                GridLayoutManager layout = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
                                rv.setLayoutManager(layout);

                                stopLoading();
                            }
                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    private void showPath(String action) {
        // Obtiene el path
        startLoading();
        Data data = new Data.Builder()
                .putString("action", action)
                .putBoolean("keyPem", keyPem)
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
                            ConnectionLostDialog connectionLostDialog = new ConnectionLostDialog();
                            connectionLostDialog.show(getActivity().getSupportFragmentManager(), "lost");
                        } else if (result.equals("hostUnreachable")) {
                            Toast.makeText(getContext(), getString(R.string.hostUnreachable), Toast.LENGTH_LONG).show();
                        } else {
                            String[] lines = result.split(",");
                            TextView pathText = getActivity().findViewById(R.id.path);
                            // Para pruebas
                            if (ServerListFragment.selectedServer.getName().toLowerCase().contains("movil")) {
                                pathText.setText("/storage/emulated/0");
                                path = "/storage/emulated/0";
                            } else {
                                pathText.setText(lines[0]);
                                path = lines[0];
                            }
                            Data data1 = new Data.Builder()
                                    .putString("action", "ls -l "+pathText.getText().toString())
                                    .putBoolean("keyPem", keyPem)
                                    .build();
                            //Mostramos los archivos del path actual
                            showData(data1);

                        }
                    }
                });
        WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
    }

    @Override
    public void onDismiss(String path) {
        startLoading();
        Data data = new Data.Builder()
                .putString("action", "ls -l "+ path)
                .putBoolean("keyPem", keyPem)
                .build();

        //Mostramos los archivos del path actual
        showData(data);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Al elegir un archivo para subir
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String uri2 = getPath(getContext(), uri);
                startLoading();
                if (uri2 != null) {
                    Data data1 = new Data.Builder()
                            .putString("action", "")
                            .putString("from", uri2)
                            .putString("to", path + "/")
                            .putString("do", "upload")
                            .putBoolean("keyPem", keyPem)
                            .build();
                    OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SSHWorker.class)
                            .setInputData(data1)
                            .build();
                    WorkManager.getInstance(getActivity()).getWorkInfoByIdLiveData(otwr.getId())
                            .observe(getActivity(), status -> {
                                if (status != null && status.getState().isFinished()) {
                                    Data data2 = new Data.Builder()
                                            .putString("action", "ls -l "+ path)
                                            .putBoolean("keyPem", keyPem)
                                            .build();
                                    //Mostramos los archivos del path actual
                                    showData(data2);
                                }
                            });

                    WorkManager.getInstance(getActivity().getApplicationContext()).enqueue(otwr);
                } else {
                    Toast.makeText(getContext(), getString(R.string.badFileType), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Nullable
    public static String getPath(Context context, Uri uri) {
        // Obtiene el path a partir de una rui
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        // Utiliza content resolver para obtener el path de una rui
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            } else {
                return null;
            }
        }
        return null;
    }

    public void startLoading() {
        // Muestra pantalla de carga
        loadingDialog = new LoadingDialog();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getActivity().getSupportFragmentManager(), "loading");
    }

    public void stopLoading() {
        // Quita la pantalla de carga
        loadingDialog.dismiss();
    }
}
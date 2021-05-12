package ehu.das.myconnect.service;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ehu.das.myconnect.fragment.FilesFragment;
import ehu.das.myconnect.fragment.ServerListFragment;

public class SSHConnectionWorker extends Worker {

    public SSHConnectionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SSHConnector sshConnector = new SSHConnector();
        String exception = "";
        try {
            exception = sshConnector.connect(getInputData().getString("user"), getInputData().getString("password"), getInputData().getString("host"), getInputData().getInt("port",22), getInputData().getBoolean("keyPem", false));
            if (!exception.equals("")) {
                exception = "fail";
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Data resultados = new Data.Builder()
                .putString("result", exception)
                .build();
        ServerListFragment.connection = sshConnector;
        return Result.success(resultados);
    }
}

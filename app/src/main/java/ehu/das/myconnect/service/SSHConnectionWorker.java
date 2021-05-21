package ehu.das.myconnect.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ehu.das.myconnect.fragment.ServerListFragment;

/**
 * Worker que realiza la conexión ssh al servidor y indica si se ha conectado con éxito
 */
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
            Log.i("ssh", "usuario: " + getInputData().getString("user"));
            exception = sshConnector.connect(getInputData().getString("user"), getInputData().getString("password"), getInputData().getString("host"), getInputData().getInt("port",22), getInputData().getBoolean("keyPem", false));
            Log.i("ssh", "exception: " + exception);
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

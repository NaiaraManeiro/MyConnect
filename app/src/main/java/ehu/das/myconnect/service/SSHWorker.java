package ehu.das.myconnect.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

public class SSHWorker  extends Worker {

    private String result = "";
    private String exception = "";

    public SSHWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String action = getInputData().getString("action");

        SSHConnector sshConnector = new SSHConnector();
        try {
            exception = sshConnector.connect(getInputData().getString("user"), getInputData().getString("password"), getInputData().getString("host"), getInputData().getInt("port",22), getInputData().getBoolean("keyPem", false));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (exception.contains("Auth fail")) {
            result = "authFail";
        } else if (exception.contains("failed to")) {
            result = "failConnect";
        } else if (action.equals("ls")) {
            try {
                result = sshConnector.executeCommand("ls -l /storage/emulated/0"); //El path se añade solo para las pruebas en mi móvil
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSchException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Data resultados = new Data.Builder()
                .putString("result", result)
                .build();

        return Result.success(resultados);
    }
}

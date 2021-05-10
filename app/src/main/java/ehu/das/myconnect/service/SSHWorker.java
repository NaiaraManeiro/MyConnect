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
    private SSHConnector sshConnector;

    public SSHWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String funcion = getInputData().getString("funcion");

        sshConnector = new SSHConnector();
        try {
            exception = sshConnector.connect(getInputData().getString("usuario"), getInputData().getString("contrasena"), getInputData().getString("host"), getInputData().getInt("puerto",22), getInputData().getBoolean("keyPem", false));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (exception.contains("Auth fail")) {
            result = "authFail";
        } else if (exception.contains("failed to")) {
            result = "failConnect";
        } else if (funcion.equals("ls")) {
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
                .putString("resultado", result)
                .build();

        return Result.success(resultados);
    }
}

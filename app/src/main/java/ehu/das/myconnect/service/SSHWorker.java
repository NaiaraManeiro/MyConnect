package ehu.das.myconnect.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

import ehu.das.myconnect.fragment.ServerListFragment;

/**
 * Ejecuta comandos mediante ssh en el servidor en segundo plano
 */
public class SSHWorker  extends Worker {

    private String exception = "";

    public SSHWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String command = getInputData().getString("action");

        SSHConnector sshConnector = new SSHConnector();
        try {
            exception = sshConnector.connect(ServerListFragment.selectedServer.getUser(), ServerListFragment.selectedServer.getPassword(), ServerListFragment.selectedServer.getHost(), ServerListFragment.selectedServer.getPort(), getInputData().getBoolean("keyPem", false));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String[] resultM = new String[]{"",""};
        if (exception.contains("Auth fail")) {
            resultM[0] = "authFail";
        } else if (exception.contains("failed to")) {
            resultM[0] = "failConnect";
        }

        if (!exception.contains("Auth fail") && !exception.contains("failed to")) {
            try {
                if (command.equals("")) {
                    String from = getInputData().getString("from");
                    String to = getInputData().getString("to");
                    String paths = from + "," + to;
                    String doAcion = getInputData().getString("do");
                    resultM = sshConnector.executeCommand(paths, doAcion);
                } else {
                    resultM = sshConnector.executeCommand(command, "");
                    String success = resultM[0];
                    if (command.contains("cat") && success.length() > 10240) {
                        success = "";
                        success = success + "error,";
                        resultM[0] = success + sshConnector.executeCommand("head -10 "+getInputData().getString("path"), "");
                    }
                }

            } catch (IllegalAccessException | IOException | JSchException e) {
                e.printStackTrace();
            }
        }

        Data resultados = new Data.Builder()
                .putString("success", resultM[0])
                .putString("fail", resultM[1])
                .putString("result", resultM[0])
                .build();

        return Result.success(resultados);
    }
}

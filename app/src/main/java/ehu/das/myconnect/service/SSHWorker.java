package ehu.das.myconnect.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

import ehu.das.myconnect.fragment.ServerListFragment;

public class SSHWorker  extends Worker {

    private String result = "";
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

        if (exception.contains("Auth fail")) {
            result = "authFail";
        } else if (exception.contains("failed to")) {
            result = "failConnect";
        }

        if (!exception.contains("Auth fail") && !exception.contains("failed to")) {
            try {
                result = sshConnector.executeCommand(command);
                if (command.contains("cat") && result.length() > 10240) {
                    result += "error,";
                    result = result.substring(0, Math.min(result.length(), 10239));
                }
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

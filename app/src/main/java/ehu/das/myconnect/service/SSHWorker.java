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
    private String command;
    private String path;

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

        if (!exception.contains("Auth fail") && !exception.contains("failed to") && !action.equals("pwd")) {
            path = getInputData().getString("path");
        }

        if (exception.contains("Auth fail")) {
            result = "authFail";
        } else if (exception.contains("failed to")) {
            result = "failConnect";
        } else if (action.equals("pwd")) {
            command = "pwd";
        } else if (action.equals("ls")) {
            command = "ls -l "+path;
        } else if (action.equals("cat")) {
            command = "cat "+path;
        } else if (action.equals("rm")) {
            command = "rm "+path;
        } else if (action.equals("editFile")) {
            String fileText = getInputData().getString("fileText");
            command = "echo '" +fileText+ "' > " + path;
        }

        if (!exception.contains("Auth fail") && !exception.contains("failed to")) {
            try {
                result = sshConnector.executeCommand(command);
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

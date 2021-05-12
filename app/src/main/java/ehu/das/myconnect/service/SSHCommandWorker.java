package ehu.das.myconnect.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

import ehu.das.myconnect.fragment.ServerListFragment;

public class SSHCommandWorker extends Worker {

    public SSHCommandWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        String result = "";
        try {
            result = ServerListFragment.connection.executeCommand(getInputData().getString("cmd"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Data resultados = new Data.Builder()
                .putString("result", result)
                .build();
        return ListenableWorker.Result.success(resultados);
    }
}

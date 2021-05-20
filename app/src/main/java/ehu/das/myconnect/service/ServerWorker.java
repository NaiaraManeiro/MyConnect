package ehu.das.myconnect.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerWorker extends Worker {

    private String result = "";
    private String exception = "";

    public ServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String script = getInputData().getString("script");
        if (script == null) {
            script = "servidor.php";
        }
        String direccion = "http://ec2-54-242-79-204.compute-1.amazonaws.com/nmaneiro001/WEB/MyConnect/" + script;
        Log.i("register", direccion);
        HttpURLConnection urlConnection = null;
        String action = getInputData().getString("action");

        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);

            boolean conexion = getInputData().getBoolean("conexion", false);

            if ((action.equals("addServer") || action.equals("editServer")) && conexion) {
                SSHConnector sshConnector = new SSHConnector();
                try {
                    exception = sshConnector.connect(getInputData().getString("user"), getInputData().getString("password"), getInputData().getString("host"), getInputData().getInt("port",22), getInputData().getBoolean("keyPem", false));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            if (exception.toLowerCase().contains("auth fail")) {
                result = "authFail";
            } else if (exception.contains("failed to connect")) {
                result = "failConnect";
            } else if (exception.contains("Host unreachable")) {
                result = "hostUnreachable";
            } else {
                JSONObject parametrosJSON = new JSONObject();
                parametrosJSON.put("action", action);
                if (action.equals("addServer")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("host", getInputData().getString("host"));
                    parametrosJSON.put("port", getInputData().getInt("port",22));
                    parametrosJSON.put("serverName", getInputData().getString("serverName"));
                    parametrosJSON.put("userName", getInputData().getString("userName"));
                    parametrosJSON.put("keyPem", getInputData().getInt("passwordPem",0));
                } else if (action.equals("serverData")) {
                    parametrosJSON.put("userName", getInputData().getString("userName"));
                } else if (action.equals("removeServer")) {
                    parametrosJSON.put("serverName", getInputData().getString("serverName"));
                    parametrosJSON.put("userName", getInputData().getString("userName"));
                } else if (action.equals("editServer")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("host", getInputData().getString("host"));
                    parametrosJSON.put("port", getInputData().getInt("port",22));
                    parametrosJSON.put("serverName", getInputData().getString("serverName"));
                    parametrosJSON.put("oldServerName", getInputData().getString("oldServerName"));
                    parametrosJSON.put("userName", getInputData().getString("userName"));
                } else if (action.equals("register")) {
                    Log.i("register", "register_input");
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("email", getInputData().getString("email"));
                    parametrosJSON.put("password", getInputData().getString("password"));
                } else if (action.equals("login")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("password", getInputData().getString("password"));
                } else if (action.equals("user")) {
                    parametrosJSON.put("email", getInputData().getString("email"));
                } else if (action.equals("scripts")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("server", getInputData().getString("serverName"));
                } else if (action.equals("addScript")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("name", getInputData().getString("name"));
                    parametrosJSON.put("cmd", getInputData().getString("cmd"));
                    parametrosJSON.put("server", getInputData().getString("serverName"));
                } else if (action.equals("deleteScript")) {
                    Log.i("script", "elimino");
                    parametrosJSON.put("user", getInputData().getString("user"));
                    parametrosJSON.put("name", getInputData().getString("name"));
                    parametrosJSON.put("cmd", getInputData().getString("cmd"));
                    parametrosJSON.put("server", getInputData().getString("serverName"));
                } else if (action.equals("deleteUser")) {
                    parametrosJSON.put("user", getInputData().getString("user"));
                }
                urlConnection.setRequestProperty("Content-Type","application/json");
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametrosJSON.toString());
                out.close();
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (!exception.contains("Auth fail")) {
            int statusCode;
            try {
                statusCode = urlConnection.getResponseCode();
                if (statusCode == 200) {
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result += (line);
                    }
                    inputStream.close();
                }
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

package ehu.das.myconnect.service;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase encargada de establecer conexión y ejecutar comandos SSH.
 */
public class SSHConnector {

    /**
     * Constante que representa un enter.
     */
    private static final String ENTER_KEY = ",";
    /**
     * Sesión SSH establecida.
     */
    private Session session;

    /**
     * Establece una conexión SSH.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @param host     Host a conectar.
     * @param port     Puerto del Host.
     *
     * @throws JSchException          Cualquier error al establecer
     *                                conexión SSH.
     * @throws IllegalAccessException Indica que ya existe una conexión
     *                                SSH establecida.
     */
    public String connect(String username, String password, String host, int port, boolean keyPem)
            throws IllegalAccessException {
        if (this.session == null || !this.session.isConnected()) {
            JSch jsch = new JSch();
            try {
                jsch.setKnownHosts("~/.ssh/known_hosts");
                if (keyPem) {
                    jsch.addIdentity(password);
                }

                this.session = jsch.getSession(username, host, port);

                if (!keyPem) {
                    this.session.setPassword(password);
                }

                // Parametro para no validar key de conexion.
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
                config.put("MAXAuthTries", "3");
                this.session.setConfig(config);
                this.session.setOutputStream(System.out);
                this.session.connect();
                return "";
            } catch (JSchException e) {
                e.printStackTrace();
                return String.valueOf(e);
            }
        } else {
            throw new IllegalAccessException("Sesion SSH ya iniciada.");
        }
    }

    /**
     * Ejecuta un comando SSH.
     *
     * @param command Comando SSH a ejecutar.
     *
     * @return
     *
     * @throws IllegalAccessException Excepción lanzada cuando no hay
     *                                conexión establecida.
     * @throws JSchException          Excepción lanzada por algún
     *                                error en la ejecución del comando
     *                                SSH.
     * @throws IOException            Excepción al leer el texto arrojado
     *                                luego de la ejecución del comando
     *                                SSH.
     */
    public final String executeCommand(String command, String sftpAction)
            throws IllegalAccessException, JSchException, IOException {
        if (this.session != null && this.session.isConnected()) {

            if (sftpAction.equals("upload") || sftpAction.equals("download")) {
                Channel channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;
            // Abrimos un canal SSH. Es como abrir una consola.
            ChannelExec channel = (ChannelExec) this.session.
                    openChannel("exec");
            // https://stackoverflow.com/questions/6902386/how-to-read-jsch-command-output
/**
            InputStream in = channelExec.getInputStream();

                try {
                    String[] paths = command.split(",");
                    String from = paths[0];
                    String to = paths[1];
                    if (sftpAction.equals("upload")) {
                        sftp.put(from,to);
                    } else {
                        sftp.get(from, to);
                    }

                } catch (SftpException e) {
                    e.printStackTrace();
                }

                return "";
            } else {
                // Abrimos un canal SSH. Es como abrir una consola.
                ChannelExec channelExec = (ChannelExec) this.session.
                        openChannel("exec");

                InputStream in = channelExec.getInputStream();

                // Ejecutamos el comando.

                channelExec.setCommand(command);
                channelExec.connect();

                // Obtenemos el texto impreso en la consola.
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder builder = new StringBuilder();
                String linea;

                while ((linea = reader.readLine()) != null) {
                    builder.append(linea);
                    builder.append(ENTER_KEY);
                }

                // Cerramos el canal SSH.
                channelExec.disconnect();

                // Retornamos el texto impreso en la consola.
                return builder.toString();
            }
            // Ejecutamos el comando.
            channelExec.setCommand(command);
            final ByteArrayOutputStream error = new ByteArrayOutputStream();
            channelExec.setErrStream(error);
            final ByteArrayOutputStream success = new ByteArrayOutputStream();
            channelExec.setErrStream(error);
            channelExec.setOutputStream(success);
            channelExec.connect();
            try{Thread.sleep(200);}catch(Exception ee){}
            String successStr = new String(success.toByteArray());
            String errorStr = new String(error.toByteArray());
            channelExec.disconnect();
            String[] result = {successStr.trim(), errorStr.trim()};
            // Retornamos el texto impreso en la consola.**/
            InputStream in = channel.getInputStream();
            InputStream err = channel.getExtInputStream();
            StringBuilder outputBuffer = new StringBuilder();
            StringBuilder errorBuffer = new StringBuilder();
            channel.setCommand(command);
            channel.connect();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    errorBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if ((in.available() > 0) || (err.available() > 0)) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (Exception ee) {
                }
            }
            System.out.println("output: " + outputBuffer.toString());
            System.out.println("error: " + errorBuffer.toString());
            return new String[] {outputBuffer.toString().trim(), errorBuffer.toString().trim()};
        } else {
            throw new IllegalAccessException("No existe sesion SSH iniciada.");
        }
    }

    /**
     * Cierra la sesión SSH.
     */
    public final void disconnect() {
        this.session.disconnect();
    }


}

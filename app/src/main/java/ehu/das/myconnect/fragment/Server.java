package ehu.das.myconnect.fragment;

public class Server {
    private String nombre;
    private String usuario;
    private String host;
    private int puerto;

    public Server (String nombre, String usuario, String host, int puerto) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.host = host;
        this.puerto = puerto;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getUsuario() {
        return this.usuario;
    }

    public String getHost() {
        return this.host;
    }

    public int getPuerto() {
        return this.puerto;
    }
}

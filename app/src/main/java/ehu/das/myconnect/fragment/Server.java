package ehu.das.myconnect.fragment;

public class Server {
    private String name;
    private final String user;
    private final String host;
    private final int port;
    private String password;
    private final int pem;

    public Server (String name, String user, String host, int port, int pem) {
        this.name = name;
        this.user = user;
        this.host = host;
        this.port = port;
        this.pem = pem;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return this.user;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public int getPem() { return this.pem; }
}

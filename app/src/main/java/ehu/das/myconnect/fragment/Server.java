package ehu.das.myconnect.fragment;

public class Server {
    private final String name;
    private final String user;
    private final String host;
    private final int port;

    public Server (String name, String user, String host, int port) {
        this.name = name;
        this.user = user;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return this.name;
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
}

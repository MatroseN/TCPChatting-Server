import java.net.InetAddress;
import java.net.Socket;

public class Member {
    public Member(String name, Socket socket){
        this.socket = socket;
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    private String name;
    private Socket socket;
}

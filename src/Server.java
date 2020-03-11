import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server extends Thread{
    public Server(int serverPort){
        connectedMembers = new Hashtable<>();
        this.portNumber = serverPort;
    }

    public void run(){
        serverSocket = null;
        socket = null;

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (Exception e) {
            System.err.println("Couldn't create server socket with the provided port number");
        }

        serverEnd = new EndPoint();

        do{
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                System.err.println("Server socket couldn't accept");
            }

            new HandleClient(serverEnd, socket, connectedMembers);

        }while(true);
    }

    private int portNumber;
    private EndPoint serverEnd;
    private Hashtable<String, Member> connectedMembers;
    private ServerSocket serverSocket;
    private Socket socket;
}

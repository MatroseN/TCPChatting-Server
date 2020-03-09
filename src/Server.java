import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

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

            String receivedMessage = serverEnd.readStream(socket);

            new HandleClient(serverEnd, socket, replyMessage, connectedMembers);

        }while(true);
    }

    private int portNumber;
    private String replyMessage;
    private EndPoint serverEnd;
    private String receivedMessage;
    private String clientName;
    private String command;
    private Hashtable<String, Member> connectedMembers;
    private InetAddress senderAddress;
    private int senderPort;
    private String recipientName;
    private ServerSocket serverSocket;
    private Socket socket;
}

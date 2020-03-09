import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

// Takes care of the response to the clients in this separate thread to allow for some parallelism
public class HandleClient extends Thread{
    public HandleClient(EndPoint serverEnd, Socket socket, String replyMessage, Hashtable<String, Member> connectedMembers){
        this.serverEnd = serverEnd;
        this.socket = socket;
        this.replyMessage = replyMessage;
        this.connectedMembers = connectedMembers;
        start();
    }

    private void checkCommand(String command){
        switch(command){
            case "/handshake":
                processNewMember(clientName);
                break;
            case "/tell":
                tellHandler();
                break;
            case "/list":
                // TODO: sendPrivateMessage(nameList, "Server", senderName);
            case "/leave":
                removeFromNameList();
                broadcast("left the chatroom", clientName);
                break;
            case "/broadcast":
                broadcast();
        }
    }

    private void tellHandler(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage));
        recipientName = temp.get(0);
        temp.remove(0);
        receivedMessage = Arrays.toString(temp.toArray());
        receivedMessage = String.join("|", receivedMessage);
        replyMessage = receivedMessage.replaceFirst("\\[", "").replaceAll("]", "");
        receivedMessage = clientName + ": " + receivedMessage;
        sendPrivateMessage(receivedMessage, clientName, recipientName);
    }

    private void processNewMember(String clientName){
        if(connectedMembers.get(clientName) == null){
            connectedMembers.put(clientName, new Member(clientName, socket));
            broadcast("joined the chatroom", clientName);
        }
    }

    private void sendPrivateMessage(String message, String senderName, String recipientName){
        Member recipient = getConnectedMember(recipientName);
        serverEnd.writeStream(socket, message);
    }

    private void broadcast(){
        replyMessage = String.join("|", receivedMessage);
        replyMessage = receivedMessage.replaceFirst("\\[", "").replaceAll("]", "");
        replyMessage = clientName + ": " + replyMessage;
        Socket recipientSocket;
        Member member;

        for(Member m : connectedMembers.values()){
            member = m;
            recipientSocket = member.getSocket();
            serverEnd.writeStream(recipientSocket, replyMessage);
        }

    }

    private void broadcast(String message, String senderName){
        message = "Server: " + senderName + " " + message;
        Socket recipientSocket;
        Member member;
        for(Member m : connectedMembers.values()){
            member = m;
            recipientSocket = member.getSocket();
            serverEnd.writeStream(recipientSocket, message);
        }
    }

    private void removeFromNameList(){
        connectedMembers.remove(clientName);
    }

    public void setReplyMessage(String replyMessage){
        this.replyMessage = replyMessage;
    }

    private Member getConnectedMember(String memberName){
        return connectedMembers.get(memberName);
    }

    private void getNameFromMessage(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage.split("\\|")));
        clientName = temp.get(temp.size() -1);
        temp.remove(temp.size() -1);
        receivedMessage = "";

        for(String s : temp){
            receivedMessage += "|" + s;
        }
        replyMessage = String.join("|", receivedMessage);
    }

    private void getCommandFromMessage(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage.split("\\|")));
        if(temp.get(0).startsWith("/")){
            command = temp.get(0);
            temp.remove(0);
            receivedMessage = "";

            for(String s : temp){
                receivedMessage += "|" + s;
            }
        }
    }

    public void run(){
        receivedMessage = serverEnd.readStream(socket);
        getCommandFromMessage();
        getNameFromMessage();
        checkCommand(command);
    }

    private EndPoint serverEnd;
    private Socket socket;
    private String replyMessage;
    private String receivedMessage;
    private String clientName;
    private String recipientName;
    private String command;
    private Hashtable<String, Member> connectedMembers;
}

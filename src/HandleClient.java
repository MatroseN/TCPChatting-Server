import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

// Takes care of the response to the clients in this separate thread to allow for some parallelism
public class HandleClient extends Thread{
    public HandleClient(EndPoint serverEnd, Socket socket, Hashtable<String, Member> connectedMembers){
        this.serverEnd = serverEnd;
        this.socket = socket;
        this.connectedMembers = connectedMembers;
        start();
    }

    // Checks what command was passed and handle the request based on what the command was
    private void checkCommand(String command){
        // Only accept handshakes from unconnected clients. And don't allow unconnected clients to pass other commands
        if(getConnectedMember(clientName) == null && command.equals("/handshake")){
            processNewMember(clientName);
        }else{
            switch(command){
                case "/tell":
                    tellHandler();
                    break;
                case "/list":
                    // TODO: sendPrivateMessage(nameList, "Server", senderName);
                case "/leave":
                    broadcast("left the chatroom", clientName);
                    removeFromNameList();
                    break;
                case "/broadcast":
                    broadcast();
                    break;
                default:
                    serverEnd.writeStream(socket, "Server: Not a valid command!");
                    break;
            }
        }
    }

    // Handles what should be done with the message from a /tell command. To make sure it gets formatted correctly
    private void tellHandler(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage.split("\\|")));
        recipientName = temp.get(0);
        temp.remove(0);
        receivedMessage = Arrays.toString(temp.toArray());
        receivedMessage = String.join("|", receivedMessage);
        replyMessage = receivedMessage.replaceFirst("\\[", "").replaceAll("]", "");
        replyMessage = clientName + ": " + replyMessage;
        sendPrivateMessage(replyMessage, recipientName);
    }

    // Adds an unconnected client to the hashtable with all connected clients
    private void processNewMember(String clientName){
        if(connectedMembers.get(clientName) == null){
            connectedMembers.put(clientName, new Member(clientName, socket));
            broadcast("joined the chatroom", clientName);
        }
    }

    // Sends a message to a specific client
    private void sendPrivateMessage(String message, String recipientName){
        Member recipient = getConnectedMember(recipientName);
        serverEnd.writeStream(recipient.getSocket(), message);
    }

    // Broadcast for client messages
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

    // Broadcast for server messages
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

    // Remove client name from the list
    private void removeFromNameList(){
        connectedMembers.remove(clientName);
    }

    // Gets a specific member from the hashtable based on name
    private Member getConnectedMember(String memberName){
        return connectedMembers.get(memberName);
    }

    // Gets the client name from the message and removes it from the message
    private void getNameFromMessage(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage.split("\\|")));
        clientName = temp.get(temp.size() -1);
        temp.remove(temp.size() -1);
        receivedMessage = "";

        for(int i = 0; i < temp.size(); i++){
            if(i != 0){
                receivedMessage += "|" + temp.get(i);
            }else{
                receivedMessage += temp.get(i);
            }
        }

        replyMessage = String.join("|", receivedMessage);
    }

    // Gets the command from the message and removes it from the message
    private void getCommandFromMessage(){
        List<String> temp = new ArrayList<>(Arrays.asList(receivedMessage.split("\\|")));
        if(temp.get(0).startsWith("/")){
            command = temp.get(0);
            temp.remove(0);
            receivedMessage = "";

            for(int i = 0; i < temp.size(); i++){
                if(i != 0){
                    receivedMessage += "|" + temp.get(i);
                }else{
                    receivedMessage += temp.get(i);
                }
            }
        }
    }

    public void run(){
        while(true){
            receivedMessage = serverEnd.readStream(socket);
            if(receivedMessage != null){
                getCommandFromMessage();
                getNameFromMessage();
                checkCommand(command);
            }
        }
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

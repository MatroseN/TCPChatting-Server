public class ServerDriver {
    public static void main(String[] args){
        Server serverInstance;

        int serverPort = 123;

        serverInstance = new Server(serverPort);
        serverInstance.start();
    }
}

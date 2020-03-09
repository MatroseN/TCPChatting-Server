import java.io.*;
import java.net.Socket;

public class EndPoint {

    public void writeStream(Socket socket, String message){
        DataOutputStream dOut = null;
        OutputStream os = null;
        try {
             os = socket.getOutputStream();
        } catch (Exception e) {
            System.err.println("Failed to get the Output stream");
        }
         dOut = new DataOutputStream(os);
        try {
            dOut.writeUTF(message);
        } catch (Exception e) {
            System.err.println("Failed to write UTF for Data output stream");
        }
    }

    public String readStream(Socket socket){
        String message = null;
        InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch (Exception e) {
            System.err.println("Could not get the input stream");
        }
        DataInputStream din = new DataInputStream(is);
        try {
            message = din.readUTF();
        } catch (Exception e) {
            System.err.println("Couldnt read UTF");
        }
        return message;
    }
}

package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class Main {
    public static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int SERVER_PORT = 23456;
    public static final String CLIENT_STARTED = "Client started!";


    public static String readCommandLine(String[] args) {
        return new CommandParser().parseCmdArgs(args);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(CLIENT_STARTED);
        try (
                Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {

            String msg = readCommandLine(args);
            System.out.println("Sent : " + msg);
            output.writeUTF(msg);
            String serverMsg = input.readUTF();
            System.out.println("Received : " + serverMsg);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

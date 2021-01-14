package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {


    public final int SERVER_PORT = 23456;
    public final String SERVER_STARTED = "Server started!";

    public void simulateServer() {

        boolean isRunning = true;
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println(SERVER_STARTED);
            ExecutorService executors = Executors.newCachedThreadPool();

            while (isRunning) {
                Future<Boolean> ex = executors.submit(new ClientRequestHandler(serverSocket.accept()));
                if (ex.get()) {
                    isRunning = false;
                    System.out.println("Exit command received from client !!, closing the server");
                    executors.shutdownNow();
                }

            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main obj = new Main();
        obj.simulateServer();

    }
}

package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static ru.netology.Server.*;

public class Client {

    private final Socket clientSocket;
    private BufferedReader in;
    private BufferedOutputStream out;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void readClient() throws IOException {
        Callable<String> read = () -> {

            String requestLine;
            ClientRequest clientRequest = new ClientRequest(this);

            while (true) {
                try {
                    requestLine = this.in.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    if (!clientRequest.prepareRequest(requestLine)) continue;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    if (clientRequest.requestToDo()) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return "/end";
        };

        Socket socket = this.clientSocket;
        this.in = new
                BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new
                BufferedOutputStream(socket.getOutputStream());

        Future<String> task = threadPool.submit(read);
        threads.add(task);
    }

    public BufferedReader getClientIn() {
        return this.in;
    }

    public BufferedOutputStream getClientOut() {
        return this.out;
    }
}

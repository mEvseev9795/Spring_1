package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {

    private static final int PORT = 8088;
    private static final int THREADS_QUANTITY = 6;


    //public static final ArrayBlockingQueue<Thread> thread = new ArrayBlockingQueue<>(64);
    public static ServerSocket serverSocket = null;
    public static final ConcurrentHashMap<Socket, Client> clientList = new ConcurrentHashMap<>();
    public static final ArrayBlockingQueue<Future<String>> threads = new ArrayBlockingQueue<>(64);

    public static ExecutorService threadPool;

    // инициируем пул потоков
    public static void initPoolThreads() {
        threadPool = Executors.newFixedThreadPool(THREADS_QUANTITY);
    }

    private Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            Client client;
            while (true) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (clientList.containsKey(socket)) {
                    client = clientList.get(socket);
                } else {
                    synchronized (clientList) {
                        client = new Client(socket);
                        clientList.put(socket, client);
                        clientList.notifyAll();
                    }
                }
                client.readClient();
            }
        } catch (IOException |
                 RuntimeException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static void startServer() {
        if (serverSocket == null) new Server();
    }

}

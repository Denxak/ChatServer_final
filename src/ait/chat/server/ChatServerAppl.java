package ait.chat.server;

import ait.chat.model.Message;
import ait.chat.server.task.ChatServerReceiver;
import ait.chat.server.task.ChatServerSender;
import ait.mediation.BlkQueue;
import ait.mediation.BlkQueueImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServerAppl {
    public static void main(String[] args) throws InterruptedException {
        int port = 9000;
        BlkQueue<Message> messageBox = new BlkQueueImpl<>(10);
        ChatServerSender sender = new ChatServerSender(messageBox);
        Thread senderThread = new Thread(sender);
        senderThread.setDaemon(true);
        senderThread.start();
        ExecutorService executorService = Executors.newFixedThreadPool(12);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Server is waiting...");
                Socket socket = serverSocket.accept();
                System.out.println("Connection established");
                System.out.println("Client host: " + socket.getInetAddress() + ":" + socket.getPort());
                sender.addClient(socket);
                ChatServerReceiver receiver = new ChatServerReceiver(socket, messageBox);
                executorService.execute(receiver);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}

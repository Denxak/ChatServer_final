package ait.chat.server.task;

import ait.chat.model.Message;
import ait.mediation.BlkQueue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChatServerSender implements Runnable {
    private final BlkQueue<Message> messageBox;
    private final Set<ObjectOutputStream> clients;

    public ChatServerSender(BlkQueue<Message> messageBox) {
        this.messageBox = messageBox;
        clients = new HashSet<>();
    }

    public synchronized boolean addClient(Socket socket) throws IOException {
        return clients.add(new ObjectOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {
        while (true) {
            Message message = messageBox.pop();
            synchronized (this) {
                Iterator<ObjectOutputStream> iterator = clients.iterator();
                while (iterator.hasNext()) {
                    ObjectOutputStream clientOos = iterator.next();
                    try {
                        clientOos.writeObject(message);
                    } catch (IOException e) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private static ServerSocket socket;
    private static final int PORT = 3000;

    public static void main(String[] args) throws IOException {
	socket = new ServerSocket(PORT);
	System.out.println("Now listening on port " + PORT + "...");

	var tp = Executors.newFixedThreadPool(20);
	tp.execute(new MessageQueueWriter());

	while (true) {
	    Socket clientSocket = socket.accept();
	    //tp.execute(new Session(clientSocket));
	}
    }

    private static class MessageQueueWriter implements Runnable {
	@Override
	public void run() {
	    // Repeatedly check each client's message queue;
	    System.out.println("Hello from the MessageQueueWriter!");
	}
    }
}

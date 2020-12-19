package bg.sofia.uni.fmi.mjt.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 7777;
    private static final String STOP_WORD = "quit";

    public static void main(String[] args) {
        connectToServer();
    }

    public static void connectToServer() {
        try (SocketChannel socketChannel = SocketChannel.open();
                PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, "UTF-8"), true);
                Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress("localhost", SERVER_PORT));
            System.out.println("Connected to the server.");
            MessageReceiver receiver = new MessageReceiver(socketChannel);
            new Thread(receiver).start();
            sendMessagesToServer(scanner, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessagesToServer(Scanner scanner, PrintWriter writer) {
        while (true) { // reads from the console and sends it to the server
            String message = scanner.nextLine(); // read a line from the console
            if (STOP_WORD.equals(message)) {
                break;
            }
            writer.println(message);
        }
    }
}

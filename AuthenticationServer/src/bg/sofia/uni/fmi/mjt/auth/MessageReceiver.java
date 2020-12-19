package bg.sofia.uni.fmi.mjt.auth;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageReceiver implements Runnable {

    private SocketChannel socketChannel;
    private static final int BUFFER_SIZE = 1024;

    public MessageReceiver(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                buffer.clear();
                socketChannel.read(buffer); // buffer fill
                buffer.flip();
                String reply = new String(buffer.array(), 0, buffer.limit()); // buffer drain
                if (reply.equals("disconnected")) {
                    break;
                }
                System.out.println(reply);
            }
        } 
        catch (IOException e) {
            System.out.println("Failed to read the message");
            e.printStackTrace();
        }
    }

}

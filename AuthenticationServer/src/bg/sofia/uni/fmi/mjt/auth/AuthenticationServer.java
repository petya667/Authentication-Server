package bg.sofia.uni.fmi.mjt.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import bg.sofia.uni.fmi.mjt.auth.commands.CommandExecutor;

public class AuthenticationServer {
    private static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 1024;
    private static final int SLEEP_MILLIS = 200;
    private static final CommandExecutor executor = new CommandExecutor();
    private static final UsersDatabase database = new UsersDatabase("UsersDatabase");
    private static final AuditLog auditLog = new AuditLog();
    private static Map<SocketChannel, LocalDate> lockedChannels = new HashMap<>();
    private static final int OPERATION_FOR_LOGGING = 1;
    private static final int ADMIN_OPERATION_COMMMAND_LENGTH = 5;

    public static void connect() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            // port is received
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    System.out.println("Still waiting for a ready channel...");
                    try {
                        Thread.sleep(SLEEP_MILLIS);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                processReadyChannels(selector);
            }
        }
        catch (IOException e) {
            System.out.println("There is a problem with the server socket");
            e.printStackTrace();
        }
    }

    private static void processReadyChannels(Selector selector) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                buffer.clear();
                int r = channel.read(buffer);
                if (r <= 0) {
                    System.out.println("nothing to read, will close channel");
                    channel.close();
                    break;
                }
                // get command, to which user, message..
                buffer.flip();
                String line = new String(buffer.array(), 0, buffer.limit());
                processLine(line, buffer, channel);
            } else if (key.isAcceptable()) {
                ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                SocketChannel accept = sockChannel.accept();
                accept.configureBlocking(false);
                accept.register(selector, SelectionKey.OP_READ);
            }
            keyIterator.remove();
        }
    }

    public static void main(String[] args) {
        connect();
    }

    private static void processLine(String line, ByteBuffer buffer, SocketChannel channel) throws IOException {
        String result;
        if (lockedChannels.containsKey(channel) && lockedChannels.get(channel).equals(LocalDate.now())) {
            result = "You've reached the limit of login tries. Try again the next day.";
        } else {
            if (lockedChannels.containsKey(channel)) {
                lockedChannels.remove(channel); // the lock period has expired
            }
            int logWriting = writeInAuditLog(line, channel); // returns 0 if the command doesn't require logging
            result = executor.processCommand(line, database);
            if (logWriting != 0 && writeInAuditLog(result, channel) == 15) {
                lockedChannels.put(channel, LocalDate.now());
            }
        }
        buffer.clear();
        buffer.put(result.getBytes());
        buffer.flip();
        channel.write(buffer);
    }

    private static int writeInAuditLog(String line, SocketChannel channel) {
        InetAddress ip = channel.socket().getInetAddress();
        String[] splitted = line.split(" ");
        if (line.contains("Failed login")) { // to return the number of failed logins for this channel
            int idxOfUsername = 0;
            return auditLog.writeForFailedLogin(splitted[idxOfUsername], ip);
        }
        int operationId = channel.hashCode();
        if ((line.contains("add-admin-user") || line.contains("remove-admin-user"))
                && splitted.length == ADMIN_OPERATION_COMMMAND_LENGTH) {
            String action = splitted[0];
            String adminSessionId = splitted[2];
            String username = database.getUsernameBySessionId(adminSessionId);
            if (username == null) {
                return 0;
            }
            String victim = splitted[4];
            auditLog.firstWriteForConfigurationChange(username, ip, operationId, action, victim);
            return OPERATION_FOR_LOGGING;
        }
        if (line.contains("Successful configuration change.") || line.contains("Unsuccessful configuration change")) {
            String username = splitted[0];
            String resultOfAction = splitted[1];
            auditLog.secondWriteForConfigurationChange(username, ip, operationId, resultOfAction);
        }

        if (line.contains("login")) {
            return OPERATION_FOR_LOGGING;
        }
        return 0;
    }
}
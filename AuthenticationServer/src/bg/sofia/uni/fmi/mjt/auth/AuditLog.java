package bg.sofia.uni.fmi.mjt.auth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class AuditLog {

    private static final String FILE_PATH = "recources\\AuditLog";
    private Map<String, Integer> failedLogins = new HashMap<>();

    public AuditLog() {
        createFile();
    }

    public int writeForFailedLogin(String username, InetAddress ip) {
        String eventInfo = new Timestamp(System.currentTimeMillis()).toString() + " Failed Login " + username + " "
                + ip.toString() + "\n";
        writeIntoFileEvent(eventInfo);
        if (failedLogins.containsKey(username)) {
            int numberFailed = failedLogins.get(username);
            if (numberFailed + 1 > 15) {
                failedLogins.replace(username, numberFailed, 1);
            } else {
                failedLogins.replace(username, numberFailed, numberFailed + 1);
            }
        } else {
            failedLogins.put(username, 1);
        }
        return failedLogins.get(username);
    }

    public void firstWriteForConfigurationChange(String username, InetAddress ip, int id, String action,
            String victim) {
        String eventInfo = new Timestamp(System.currentTimeMillis()).toString() + " " + id + " Configuration Change "
                + username + " " + ip + " " + action + " " + victim;
        writeIntoFileEvent(eventInfo);
    }

    public void secondWriteForConfigurationChange(String username, InetAddress ip, int id, String resultOfAction) {
        String eventInfo = new Timestamp(System.currentTimeMillis()).toString() + " " + id + " Configuration Change "
                + username + " " + ip + " " + resultOfAction + "\n";
        writeIntoFileEvent(eventInfo);
    }

    private void writeIntoFileEvent(String eventInfo) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(FILE_PATH, true));
                PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
            printWriter.write(eventInfo);
        }
        catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createFile() {
        File auditLogFile = new File(FILE_PATH);
        if (!auditLogFile.exists()) {
            try {
                auditLogFile.createNewFile();
            } 
            catch (IOException e) {
                throw new RuntimeException("Problem with creating the database file.\n" + e.getMessage());
            }
        }
    }
}

package bg.sofia.uni.fmi.mjt.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.session.UsersSession;

public class UsersDatabase {

    private static Set<String> admins;
    private String database;
    private UsersSession sessions;
    private static final int USER_INFO_ARGS = 5;
    private static final int MIN_ADMINS_FOR_ALLOWED_ADMIN_REMOVAL = 2;
    private long usersWritten;
    private File databaseFile;

    public UsersDatabase(String databaseFile) {
        this.database = databaseFile;
        sessions = new UsersSession();
        createDatabase();
        admins = new HashSet<>();
        List<String> usernames = getAllUsernames();
        if (usernames != null) {
            admins.add(usernames.get(0));
        }
        usersWritten = usernames.size();
    }

    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(databaseFile.getAbsolutePath()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                usernames.add(line.split(" ")[0]);
            }
        } 
        catch (FileNotFoundException e) {
            return null;
        }
        catch (IOException e) {
            System.err.println("Failed to read from file");
            e.printStackTrace();
        }
        return usernames;
    }

    public boolean checkIfUserExists(String username) {
        List<String> usernames = getAllUsernames();
        if (usernames == null) {
            return false;
        }
        return usernames.contains(username);
    }

    public String addUserInfo(String[] splittedArgs) { // when adding new user(logging in)
        String userInfo = getUserInfo(splittedArgs);
        writeIntoDBFile(userInfo);
        String username = splittedArgs[2];
        usersWritten++;
        if (usersWritten == 1) {
            admins.add(username);
        }
        return sessions.addActiveUserSession(username);
    }

    public boolean validateGivenPassword(String username, String passwordTry) {
        String password = getUserPassword(username);
        return password.equals(encrypt(passwordTry, "ghnk"));
    }

    public String setNewUserSessionID(String username) {
        return sessions.addActiveUserSession(username);
    }

    public boolean validateThatTheSessionIsUpToDate(String sessionId) {
        return !sessions.checkIfSessionHasExpired(getUsernameBySessionId(sessionId));
    }

    public String getUsernameBySessionId(String sessionId) {
        return sessions.getUsername(sessionId);
    }

    public void writeChangedInfoIntoFile(String[] args, String username, String sessionId, String operation)
            throws InvalidCommandArgumentsException {
        File tempFile = createFile(databaseFile.getParentFile() + "\\myTempFile.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile.getAbsolutePath()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] changedInfo = changeLine(operation, username, currentLine, args, sessionId);
                writer.write(String.join(" ", changedInfo) + System.getProperty("line.separator"));
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Couldn't find file. " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        new File(databaseFile.getAbsolutePath()).delete();
        tempFile.renameTo(new File(databaseFile.getAbsolutePath()));
    }

    public void logoutSessionId(String sessionId) {
        sessions.deleteSessionId(sessionId);
    }

    public boolean isAdmin(String sessionId) {
        return admins.contains(getUsernameBySessionId(sessionId));
    }

    public void addAdmin(String newAdmin) {
        admins.add(newAdmin);
    }

    public boolean removeAdmin(String admin, String adminToRemove) {
        if (!admin.equals(adminToRemove) || admins.size() >= MIN_ADMINS_FOR_ALLOWED_ADMIN_REMOVAL) {
            admins.remove(adminToRemove);
            return true;
        } else {
            return false;
        }
    }

    private File createFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } 
            catch (IOException e) {
                throw new RuntimeException("Problem with creating the " + fileName + " file.\n" + e.getMessage());
            }
        }
        return file;
    }

    private void writeIntoDBFile(String userInfo) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(databaseFile.getAbsolutePath(), true));
                PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
            printWriter.write(userInfo);
        }
        catch (FileNotFoundException e) {
            System.out.println("Couldn't find file. " + e.getMessage());
        } 
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    private void createDatabase() {
        File resources = new File("recources");
        if (!resources.exists()) {
            resources.mkdir();
        }
        databaseFile = createFile("recources\\" + database);
    }

    private String getUserInfo(String[] args) {
        return String.join(" ", args[2], encrypt(args[4], "ghnk"), args[6], args[8], args[10]);
    }

    private static String encrypt(String strClearText, String strKey) {
        String strData = "";
        try {
            SecretKeySpec skeyspec = new SecretKeySpec(strKey.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
            byte[] encrypted = cipher.doFinal(strClearText.getBytes());
            strData = new String(encrypted);
        }
        catch (GeneralSecurityException e) {
            throw new IllegalStateException("Problem with encrypting function", e);
        }
        return strData;
    }

    private String getUserPassword(String username) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(databaseFile.getAbsolutePath()))) {
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                String[] line = input.split(" ");
                if (username.equals(line[0])) {
                    return line[1];
                }
            }
        } 
        catch (FileNotFoundException e) {
            return null;
        } 
        catch (IOException e) {
            System.err.println("Failed to read from file");
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static String[] getNewUserInfo(String[] oldInfo, String[] commandArgs) {
        String[] optionalArgs = { "--new-username", "", "--new-first-name", "--new-last-name", "--new-email" };
        String[] newUserInfo = new String[USER_INFO_ARGS];
        int currIdx = 3;
        for (int i = 0; i < newUserInfo.length; i++) {
            if (i == 1) {
                newUserInfo[1] = oldInfo[1]; // password stays the same
                continue;
            }
            if (commandArgs.length - 1 >= currIdx && commandArgs[currIdx].equals(optionalArgs[i])) {
                newUserInfo[i] = commandArgs[currIdx + 1];
                currIdx += 2;
            } else {
                newUserInfo[i] = oldInfo[i];
            }
        }
        return newUserInfo;
    }

    private String[] changeLine(String operation, String username, String currentLine, String[] args, String sessionId)
            throws InvalidCommandArgumentsException {
        String[] splitted = currentLine.trim().split(" ");
        if (splitted[0].equals(username)) {
            String[] changedInfo;
            if (operation.equals("delete-user")) {
                if (admins.contains(username)) {
                    admins.remove(username);
                }
                String usersSessionId = sessions.getSessionId(username);
                if (usersSessionId != null) {
                    sessions.deleteSessionId(usersSessionId);
                }
                usersWritten--;
                changedInfo = null; // nothing will be added
            } else if (operation.equals("update-user")) {
                changedInfo = getNewUserInfo(splitted, args);
                if (!username.equals(changedInfo[0])) {
                    sessions.changeUsername(username, changedInfo[0], sessionId);
                }
            } else {
                changedInfo = getUserInfoWithNewPassword(splitted, args);
            }
            return changedInfo;
        } else {
            return splitted;
        }
    }

    private static String[] getUserInfoWithNewPassword(String[] oldInfo, String[] commandArgs)
            throws InvalidCommandArgumentsException {
        String oldPassword = encrypt(commandArgs[6], "ghnk");
        if (oldPassword.equals(oldInfo[1])) { // validate that the given old password is correct
            String newPassword = encrypt(commandArgs[8], "ghnk");
            oldInfo[1] = newPassword; // the rest of the information stays the same
        } else {
            throw new InvalidCommandArgumentsException("Given incorrect old password.");
        }
        return oldInfo;
    }
}

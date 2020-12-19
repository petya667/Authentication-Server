package bg.sofia.uni.fmi.mjt.auth.session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UsersSession {

    private Map<String, Session> sessions;
    private Map<String, String> usernames; // map of id and user name 
    private static long DAYS_TO_LIVE = 2L;

    public UsersSession() {
        sessions = new HashMap<>();
        usernames = new HashMap<>();
    }

    public String addActiveUserSession(String username) {
        Session session = new Session(generateSessionId(), LocalDate.now());
        if (!sessions.containsKey(username)) {
            sessions.put(username, session);
            usernames.put(session.getSessionId(), username);
        } else {
            Session oldSession = sessions.get(username);
            usernames.remove(oldSession.getSessionId());
            sessions.replace(username, oldSession, session);
            usernames.put(session.getSessionId(), username);
        }
        return session.getSessionId();
    }

    public boolean checkIfSessionHasExpired(String username) {
        Session session = sessions.get(username);
        if (session == null) {
            return true;
        }
        return session.getTimeStarted().plusDays(DAYS_TO_LIVE).compareTo(LocalDate.now()) < 0; 
    }

    public String getUsername(String sessionId) {
        return usernames.get(sessionId);
    }

    public void changeUsername(String username, String newUsername, String sessionId) {
        Session session = sessions.get(username);
        sessions.remove(username);
        sessions.put(newUsername, session);
        usernames.replace(sessionId, newUsername);
    }

    public void deleteSessionId(String sessionId) {
        String username = getUsername(sessionId);
        sessions.remove(username);
        usernames.remove(sessionId);
    }

    public String getSessionId(String username) {
        return sessions.get(username).getSessionId();
    }
    
    private String generateSessionId() { // returns unique session id which will be used from the user
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("sessionID".getBytes());
            byte[] digest = md.digest();
            return digest.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}

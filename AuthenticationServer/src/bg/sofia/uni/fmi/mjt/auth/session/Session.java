package bg.sofia.uni.fmi.mjt.auth.session;

import java.time.LocalDate;

public class Session {

    private String sessionId;
    private LocalDate timeStarted;
    
    public Session(String sessionId, LocalDate timeStarted) {
        this.sessionId = sessionId;
        this.timeStarted = timeStarted;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDate getTimeStarted() {
        return timeStarted;
    }
}

package bg.sofia.uni.fmi.mjt.auth.exceptions;

public class ExpiredSessionException extends Exception {

    private static final long serialVersionUID = -6705883369313313508L;

    public ExpiredSessionException() {
        super();
    }

    public ExpiredSessionException(String message) {
        super(message);
    }
}

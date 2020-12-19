package bg.sofia.uni.fmi.mjt.auth.exceptions;

public class InvalidCommandArgumentsException extends Exception{

    private static final long serialVersionUID = 8415269033893745277L;

    public InvalidCommandArgumentsException() {
        super();
    }
    
    public InvalidCommandArgumentsException(String message) {
        super(message);
    }
}

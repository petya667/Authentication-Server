package bg.sofia.uni.fmi.mjt.auth.exceptions;

public class UserAlreadyExistsException extends Exception{

    private static final long serialVersionUID = -4924779197820854936L;

    public UserAlreadyExistsException(){
        super();
    }
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
}

package bg.sofia.uni.fmi.mjt.auth.exceptions;

public class AccessToAdminOperationDeniedException extends Exception{

    private static final long serialVersionUID = 6260551697880783063L; 
    
    public AccessToAdminOperationDeniedException() {
        super();
    }
    
    public AccessToAdminOperationDeniedException(String message) {
        super(message);
    }
}

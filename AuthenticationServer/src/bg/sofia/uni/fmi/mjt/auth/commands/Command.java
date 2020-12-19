package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.AccessToAdminOperationDeniedException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public interface Command {
    public String execute(String[] args, UsersDatabase database) throws UserAlreadyExistsException,
            InvalidCommandArgumentsException, ExpiredSessionException, AccessToAdminOperationDeniedException; 

    public void validateArguments(String[] args) throws InvalidCommandArgumentsException;
}

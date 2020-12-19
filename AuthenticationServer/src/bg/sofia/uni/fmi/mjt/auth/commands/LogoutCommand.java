package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class LogoutCommand implements Command {

    @Override
    public String execute(String[] args, UsersDatabase database)
            throws UserAlreadyExistsException, InvalidCommandArgumentsException, ExpiredSessionException {
        validateArguments(args);
        String sessionId = args[2];
        if (!database.validateThatTheSessionIsUpToDate(sessionId)) {
            throw new ExpiredSessionException("The given session id is not up-to-date or is not correct.");
        }
        database.logoutSessionId(sessionId);
        return "";
    }

    @Override
    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if (args[1].equals("--session-id")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");
    }

}

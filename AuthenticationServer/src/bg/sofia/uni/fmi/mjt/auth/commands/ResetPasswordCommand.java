package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class ResetPasswordCommand implements Command {

    @Override
    public String execute(String[] args, UsersDatabase database)
            throws UserAlreadyExistsException, InvalidCommandArgumentsException, ExpiredSessionException {
        validateArguments(args);
        String sessionId = args[2];
        if (!database.validateThatTheSessionIsUpToDate(sessionId)) {
            throw new ExpiredSessionException("The given session id is not up-to-date or is not correct.");
        }
        String username = args[4];
        if (!username.equals(database.getUsernameBySessionId(sessionId))) {
            throw new InvalidCommandArgumentsException(
                    "Seesion id and username doesn't match. Enter correct username.");
        }
        database.writeChangedInfoIntoFile(args, username, sessionId, "reset-password");
        return "";
    }

    @Override
    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if (args[1].equals("--session-id") && args[3].equals("--username") && args[5].equals("--old-password")
                && args[7].equals("--new-password")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");
    }

}

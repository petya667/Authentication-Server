package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.AccessToAdminOperationDeniedException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class RemoveAdminUserCommand implements Command {

    @Override
    public String execute(String[] args, UsersDatabase database) throws UserAlreadyExistsException,
            InvalidCommandArgumentsException, ExpiredSessionException, AccessToAdminOperationDeniedException {
        validateArguments(args);
        String sessionId = args[2];
        if (!database.validateThatTheSessionIsUpToDate(sessionId)) {
            throw new ExpiredSessionException("The given session id is not up-to-date or is not correct.");
        }
        if (!database.isAdmin(sessionId)) {
            throw new AccessToAdminOperationDeniedException(
                    database.getUsernameBySessionId(sessionId) + " No admin with this username/session id.");
        }
        String adminToRemove = args[4];
        if (database.checkIfUserExists(adminToRemove)) {
            String admin = database.getUsernameBySessionId(sessionId);
            if (database.removeAdmin(admin, adminToRemove)) {
                return database.getUsernameBySessionId(sessionId) + " Successful configuration change.";
            }
        }
        throw new InvalidCommandArgumentsException(
                database.getUsernameBySessionId(sessionId) + " Unsuccessful configuration change."
                        + " Given username doesn't exists or is the only admin of the system.");

    }

    @Override
    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if (args[1].equals("--session-id") && args[3].equals("--username")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");

    }

}

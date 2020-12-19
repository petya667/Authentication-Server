package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.AccessToAdminOperationDeniedException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class DeleteUserCommand implements Command {

    @Override
    public String execute(String[] args, UsersDatabase database) throws UserAlreadyExistsException,
            InvalidCommandArgumentsException, ExpiredSessionException, AccessToAdminOperationDeniedException {
        validateArguments(args);
        String sessionId = args[2];
        if (!database.validateThatTheSessionIsUpToDate(sessionId)) {
            throw new ExpiredSessionException("The given session id is not up-to-date or is not correct.");
        }
        if (!database.isAdmin(sessionId)) {
            throw new AccessToAdminOperationDeniedException(" No admin with this username/session id.");
        }
        String userToRemove = args[4];
        if (database.checkIfUserExists(userToRemove)) {
            database.writeChangedInfoIntoFile(args, userToRemove, sessionId, "delete-user");
        } else {
            throw new InvalidCommandArgumentsException("Given username doesn't exists. Write valid username.");
        }
        return "";
    }

    @Override
    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if (args[1].equals("--session-id") && args[3].equals("--username")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");
    }

}

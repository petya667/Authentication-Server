package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;

public class LoginCommand implements Command {

    @Override
    public String execute(String[] args, UsersDatabase database)
            throws InvalidCommandArgumentsException, ExpiredSessionException {
        validateArguments(args);
        if (args[1].equals("--username")) {
            String username = args[2];
            if (database.checkIfUserExists(username) && database.validateGivenPassword(username, args[4])) {
                return database.setNewUserSessionID(username);
            } else {
                throw new InvalidCommandArgumentsException(username
                        + " Failed login. Given username or password doesn't exist. Write valid username or password.");
            }
        } else {
            String sessionId = args[2];
            if (!database.validateThatTheSessionIsUpToDate(sessionId)) {
                throw new ExpiredSessionException(
                        "The given session id is not up-to-date or is not correct");
            }
            return "";
        }
    }

    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if ((args[1].equals("--username") && args[3].equals("--password")) || args[1].equals("--session-id")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");
    }

}

package bg.sofia.uni.fmi.mjt.auth.commands;

import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.AccessToAdminOperationDeniedException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class CommandExecutor {

    private Map<String, Command> userCommands = new HashMap<>();

    public CommandExecutor() {
        userCommands.put("register", new RegisterCommand());
        userCommands.put("login", new LoginCommand());
        userCommands.put("update-user", new UpdateUserCommand());
        userCommands.put("reset-password", new ResetPasswordCommand());
        userCommands.put("logout", new LogoutCommand());
        userCommands.put("add-admin-user", new AddAdminUserCommand());
        userCommands.put("remove-admin-user", new RemoveAdminUserCommand());
        userCommands.put("delete-user", new DeleteUserCommand());
    }

    public String processCommand(String line, UsersDatabase database) {
        String[] lineArgs = line.substring(0, line.length() - 2).split(" "); // TODO into constants ?
        String command = lineArgs[0];
        try {
            Command givenCommand = userCommands.get(command);
            if (givenCommand == null) {
                throw new InvalidCommandArgumentsException("Given command doesn't exists. Write valid command.");
            }
            return givenCommand.execute(lineArgs, database);
        }
        catch (UserAlreadyExistsException e) {
            return e.getMessage();
        }
        catch (InvalidCommandArgumentsException e) {
            return e.getMessage();
        } 
        catch (ExpiredSessionException e) {
            return e.getMessage();
        } 
        catch (AccessToAdminOperationDeniedException e) {
            return e.getMessage();
        }
    }
}

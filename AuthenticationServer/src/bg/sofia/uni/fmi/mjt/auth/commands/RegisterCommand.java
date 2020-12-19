package bg.sofia.uni.fmi.mjt.auth.commands;

import bg.sofia.uni.fmi.mjt.auth.UsersDatabase;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class RegisterCommand implements Command{

    @Override
    public String execute(String[] args, UsersDatabase database) throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        validateArguments(args);
        if(!database.checkIfUserExists(args[2])) {
            return database.addUserInfo(args);
        }
        else {
            throw new UserAlreadyExistsException("There is already registered user with this username. Try with another.");
        }
    }

    public void validateArguments(String[] args) throws InvalidCommandArgumentsException {
        if(args[1].equals("--username") && args[3].equals("--password") 
                && args[5].equals("--first-name") 
                && args[7].equals("--last-name") && args[9].equals("--email")) {
            return;
        }
        throw new InvalidCommandArgumentsException("Missing requred operands.");
    }
    
    
}

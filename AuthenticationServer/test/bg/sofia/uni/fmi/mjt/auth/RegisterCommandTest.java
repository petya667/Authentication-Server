package bg.sofia.uni.fmi.mjt.auth;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import bg.sofia.uni.fmi.mjt.auth.commands.RegisterCommand;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class RegisterCommandTest {
    
    @Test(expected = UserAlreadyExistsException.class)
    public void testExecuteMethodWithLoggingDuplicateUsers() throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        UsersDatabase databaseMock = Mockito.mock(UsersDatabase.class);
        Mockito.when(databaseMock.checkIfUserExists("new_user")).thenReturn(true);
        
        RegisterCommand registration = new RegisterCommand();
        registration.execute(("register --username new_user --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock);
    }
    
    @Test(expected = InvalidCommandArgumentsException.class)
    public void testExecuteMethodWithWrongArguments() throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        UsersDatabase databaseMock = Mockito.mock(UsersDatabase.class);
        Mockito.when(databaseMock.checkIfUserExists("new_user")).thenReturn(false);
        
        RegisterCommand registration = new RegisterCommand();
        registration.execute(("register --username new_user --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock); //password is missing
    }
    
  /*  @Test
    public void testExecuteMethodWithLoggingOneUser() throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        UsersDatabase databaseMock = Mockito.mock(UsersDatabase.class);
        Mockito.when(databaseMock.checkIfUserExists("new_user")).thenReturn(false);
        
        RegisterCommand registration = new RegisterCommand();
        registration.execute(("register --username "
                + "new_user --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock);
        assertTrue(databaseMock.checkIfUserExists("new_user"));
    }

    @Test
    public void testExecuteMethodWithLoggingFewUsers() throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        UsersDatabase databaseMock = Mockito.mock(UsersDatabase.class);
        Mockito.when(databaseMock.checkIfUserExists("new_user1")).thenReturn(false);
        Mockito.when(databaseMock.checkIfUserExists("new_user2")).thenReturn(false);
        Mockito.when(databaseMock.checkIfUserExists("new_user3")).thenReturn(false);
        
        
        RegisterCommand registration = new RegisterCommand();
        assertEquals("Execute method doesn't return the requered session ids",1,  registration.execute(("register --username "
                + "new_user1 --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock));
        assertEquals("Execute method doesn't return the requered session ids",2,  registration.execute(("register --username "
                + "new_user2 --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock));
        assertEquals("Execute method doesn't return the requered session ids",3,  registration.execute(("register --username "
                + "new_user3 --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg").split(" "), databaseMock));
    }*/
}

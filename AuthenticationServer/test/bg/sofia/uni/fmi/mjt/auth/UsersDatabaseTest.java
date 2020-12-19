package bg.sofia.uni.fmi.mjt.auth;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.auth.commands.AddAdminUserCommand;
import bg.sofia.uni.fmi.mjt.auth.commands.LoginCommand;
import bg.sofia.uni.fmi.mjt.auth.commands.RegisterCommand;
import bg.sofia.uni.fmi.mjt.auth.commands.ResetPasswordCommand;
import bg.sofia.uni.fmi.mjt.auth.commands.UpdateUserCommand;
import bg.sofia.uni.fmi.mjt.auth.exceptions.AccessToAdminOperationDeniedException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.ExpiredSessionException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.InvalidCommandArgumentsException;
import bg.sofia.uni.fmi.mjt.auth.exceptions.UserAlreadyExistsException;

public class UsersDatabaseTest {

    private static UsersDatabase database;
    private static RegisterCommand register = new RegisterCommand();
    private static LoginCommand login = new LoginCommand();
    private static ResetPasswordCommand resetPassword = new ResetPasswordCommand();
    private static AddAdminUserCommand addAdmin = new AddAdminUserCommand();
    private static String usernameFromFile;
    private static String usersPassword;
    private static String sessionId;
    private static UpdateUserCommand updateUser = new UpdateUserCommand();

    @BeforeClass
    public static void setUp() throws IOException, UserAlreadyExistsException, InvalidCommandArgumentsException {
        database = new UsersDatabase("databaseTest.txt") {
            @Override
            public List<String> getAllUsernames() {
                List<String> usernames = new ArrayList<>();
                usernames.add("nick");
                usernames.add("lora");
                usernames.add("user2");
                return usernames;
            }
        };
        sessionId = register.execute(
                "register --username newuser --password abcd --first-name Nick --last-name Bobson --email n_b@abv.bg\n"
                        .split(" "),
                database);
        usernameFromFile = "newuser";
        usersPassword = "abcd";
        database.addAdmin("newuser");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void testRegisterCommandWithLoggingDuplicateUsers()
            throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        register.execute(
                ("register --username lora --password 0000 --first-name Bob" + " --last-name Bobson --email bob@abv.bg")
                        .split(" "),
                database);
    }

    @Test(expected = InvalidCommandArgumentsException.class)
    public void testRegisterCommandWithWrongArguments()
            throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        register.execute(("register --username new_user --first-name Bob" + " --last-name Bobson --email bob@abv.bg\n")
                .split(" "), database); // password is missing
    }

    @Test
    public void testRegisterCommandWithLoggingOneUser()
            throws UserAlreadyExistsException, InvalidCommandArgumentsException {
        String message = "Method should return sessionId";
        assertNotNull(message, register.execute(("register --username " + "new_user --password 0000 --first-name Bob"
                + " --last-name Bobson --email bob@abv.bg\n").split(" "), database));
    }

    @Test(expected = ExpiredSessionException.class)
    public void testLoginCommandWithNonValidSession() throws ExpiredSessionException, InvalidCommandArgumentsException {
        login.execute(("login --session-id [B@2db0f6b2\n").split(" "), database);
    }

    @Test(expected = InvalidCommandArgumentsException.class)
    public void testLoginCommandWithWrongPassword() throws InvalidCommandArgumentsException, ExpiredSessionException {
        login.execute(("login --username " + usernameFromFile + " --password 0000\n").split(" "), database);
    }

    @Test
    public void testLoginCommandWithSessionId()
            throws InvalidCommandArgumentsException, ExpiredSessionException, UserAlreadyExistsException {
        String message = "Method should return sessionId";
        String sessionIdRegister = register.execute(("register --username user1 --password abcd "
                + "--first-name Nick --last-name Bobson --email n_b@abv.bg\n").split(" "), database);
        assertTrue(database.validateThatTheSessionIsUpToDate(sessionIdRegister));
        assertNotNull(message, login.execute(("login --session-id " + sessionIdRegister).split(" "), database));
    }

    @Test(expected = ExpiredSessionException.class)
    public void testUpdateUserCommandWithNonValidSession()
            throws ExpiredSessionException, InvalidCommandArgumentsException, UserAlreadyExistsException {
        updateUser.execute(("update-user --session-id [B@2db0f6b2\n").split(" "), database);
    }

    @Test(expected = InvalidCommandArgumentsException.class)
    public void testUpdateUserCommandWithWrongArguments()
            throws InvalidCommandArgumentsException, ExpiredSessionException, UserAlreadyExistsException {
        updateUser.execute(("update-user --sesson-id [B@2db0f6b2" + usernameFromFile).split(" "), database);
    }

    @Test
    public void testUpdateUserCommandWithChangingUsername()
            throws InvalidCommandArgumentsException, ExpiredSessionException, UserAlreadyExistsException {
        String sessionIdRegister = register.execute(("register --username toBeChanged --password abcd "
                + "--first-name Nick --last-name Bobson --email n_b@abv.bg\n").split(" "), database);
        assertNotNull(updateUser.execute(("update-user --session-id " + sessionId + " --new-username bob\n").split(" "),
                database)); // because of the override method in the setUp we can't test it with login
                            // command which uses the overwritten method
    }

    @Test(expected = ExpiredSessionException.class)
    public void testResetPasswordCommandWithNonValidSession()
            throws ExpiredSessionException, InvalidCommandArgumentsException, UserAlreadyExistsException {
        resetPassword.execute(("reset-password --session-id [B@2db0f6b2 --username " + usernameFromFile
                + " --old-password " + usersPassword + " --new-password 0000\n").split(" "), database);
    }

    @Test(expected = InvalidCommandArgumentsException.class)
    public void testResetPasswordCommandWithWrongArguments()
            throws InvalidCommandArgumentsException, ExpiredSessionException, UserAlreadyExistsException {
        resetPassword.execute(("reset-password --session-id " + sessionId + " --username " + usernameFromFile
                + " --new-password 0000\n").split(" "), database); // old password missing
    }

    @Test
    public void testResetPasswordCommand()
            throws InvalidCommandArgumentsException, ExpiredSessionException, UserAlreadyExistsException {
        String message = "Reset password should work properly with all valid arguments.";
        assertNotNull(message,
                resetPassword
                        .execute(
                                ("reset-password --session-id " + sessionId + " --username " + usernameFromFile
                                        + " --old-password " + usersPassword + " --new-password 0000\n").split(" "),
                                database));
    }

    @Test(expected = ExpiredSessionException.class)
    public void testAddAdminUserCommandWithNonValidSession()
            throws ExpiredSessionException, InvalidCommandArgumentsException, UserAlreadyExistsException, AccessToAdminOperationDeniedException {
        addAdmin.execute(("add-admin-user --session-id [B@2db0f6b2 --username lora\n").split(" "), database);
    }

    @Test(expected = AccessToAdminOperationDeniedException.class)
    public void testAddAdminUserCommandFromUserWithNoAdminRights() throws InvalidCommandArgumentsException,
            ExpiredSessionException, UserAlreadyExistsException, AccessToAdminOperationDeniedException {
        String sessionIdRegister = register.execute(("register --username hello --password abcd "
                + "--first-name Nick --last-name Bobson --email n_b@abv.bg\n").split(" "), database);
        addAdmin.execute(("add-admin-user --session-id " + sessionIdRegister + " --username lora\n").split(" "), database); 
    }

   /* @Test(expected = InvalidCommandArgumentsException.class)
    public void testAddAdminUserCommandWithWrongArguments() throws InvalidCommandArgumentsException,
            ExpiredSessionException, UserAlreadyExistsException, AccessToAdminOperationDeniedException {
        addAdmin.execute(("add-admin-user --session-id " + sessionId +"-u" ).split(" "), database); 
    }
    @Test
    public void testAddAdminUserCommandThatWorksProperly() throws InvalidCommandArgumentsException,
            ExpiredSessionException, UserAlreadyExistsException, AccessToAdminOperationDeniedException {
        assertNotNull(addAdmin.execute(("add-admin-user --session-id " + sessionId + " --username user1\n").split(" "),
                database));
    }*/

}

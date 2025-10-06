package progr3.mail.server.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.model.User;
import progr3.mail.server.user.core.UserConstructor;

public class UserServiceTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private UserService userService;
    private User testUser1;
    private User testUser2;
    private Logger logger;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws IOException, BadRequestException {
        jsonFileHandler = new JsonFileHandler();
        logger = new Logger(LogLevelEnum.DEBUG,
                tempDir.resolve("test.json").toString(),
                false,
                true,
                jsonFileHandler);

        String userFile = tempDir.resolve("users.json").toString();

        testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        testUser2 = UserConstructor.create("user-2@test.com", "user-2");

        userRepository = new UserRepository(jsonFileHandler, userFile);

        userRepository.saveUser(testUser1);
        userRepository.saveUser(testUser2);

        logger.startScope();

        userService = new UserService(userRepository, logger);
    }

    @AfterEach
    void cleanUp() {
        logger.endScope();
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() throws BadRequestException, UserNotFoundException {
        User result = userService.getUserByEmail(testUser1.getEmail());

        assertEquals(testUser1, result);
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail("invalid@email.com");
        });
    }

    @Test
    void getUserByEmail_WithNullEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.getUserByEmail(null);
        });
    }

    @Test
    void getUserByEmail_WithEmptyEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.getUserByEmail("");
        });
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() throws BadRequestException, UserNotFoundException {
        User result = userService.getUserById(testUser1.getGuid());

        assertEquals(testUser1, result);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById("invalid-id");
        });
    }

    @Test
    void getUserById_WithNullId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.getUserById(null);
        });
    }

    @Test
    void getUserById_WithEmptyId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.getUserById("");
        });
    }

    @Test
    void login_WithValidCredentials_ShouldReturnUser() throws BadRequestException, UserNotFoundException {
        User user = userService.login(testUser1.getEmail());

        assertNotNull(user);
        assertEquals(testUser1.getGuid(), user.getGuid());
        assertEquals(testUser1, user);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.login("invalid-email");
        });
    }

    @Test
    void login_WithNullEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.login(null);
        });
    }

    @Test
    void login_WithEmptyEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.login("");
        });
    }
}

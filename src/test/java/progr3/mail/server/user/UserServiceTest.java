package progr3.mail.server.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.model.User;

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
    void setUp() throws IOException {
        jsonFileHandler = new JsonFileHandler();
        logger = new Logger(LogLevelEnum.DEBUG,
                tempDir.resolve("test.json").toString(),
                false,
                true,
                jsonFileHandler);

        String userFile = tempDir.resolve("users.json").toString();

        // Setup test messages
        testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        testUser2 = UserConstructor.create("user-2@test.com", "user-2");

        // Create repository with mocked dependencies
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
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Act
        User result = userService.getUserByEmail(testUser1.getEmail());
        // Assert
        assertEquals(testUser1, result);
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldReturnNull() {
        // Act
        User result = userService.getUserByEmail("invalid email");
        // Assert
        assertEquals(null, result);
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Act
        User result = userService.getUserById(testUser1.getGuid());
        // Assert
        assertEquals(testUser1, result);
    }

    @Test
    void getUserById_WithInvalidId_ShouldReturnNull() {
        // Act
        User result = userService.getUserById("invalid-id");
        // Assert
        assertEquals(null, result);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnUser() {
        // Act
        boolean loggedIn = userService.login(testUser1.getEmail());
        // Assert
        assertEquals(true, loggedIn);
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnFalse() {
        // Act
        boolean loggedIn = userService.login("invalid email");
        // Assert
        assertEquals(false, loggedIn);
    }
}

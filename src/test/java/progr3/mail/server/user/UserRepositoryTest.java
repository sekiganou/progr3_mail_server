package progr3.mail.server.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.User;

public class UserRepositoryTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private UserRepository userRepository;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() throws IOException, BadRequestException {
        jsonFileHandler = new JsonFileHandler();

        String filePath = tempDir.resolve("users.json").toString();

        testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        testUser2 = UserConstructor.create("user-2@test.com", "user-2");

        Class<User> userClass = User.class;

        jsonFileHandler.saveToFile(testUser1, filePath, userClass);
        jsonFileHandler.saveToFile(testUser2, filePath, userClass);

        userRepository = new UserRepository(jsonFileHandler, filePath);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> result = userRepository.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(testUser1));
        assertTrue(result.contains(testUser2));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() throws BadRequestException, UserNotFoundException {
        User result = userRepository.getUserById(testUser1.getGuid());

        assertEquals(testUser1, result);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userRepository.getUserById("invalid-id");
        });
    }

    @Test
    void getUserById_WithNullId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userRepository.getUserById(null);
        });
    }

    @Test
    void getUserById_WithEmptyId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userRepository.getUserById("");
        });
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() throws BadRequestException, UserNotFoundException {
        User result = userRepository.getUserByEmail(testUser1.getEmail());

        assertEquals(testUser1, result);
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userRepository.getUserByEmail("invalid-email@test.com");
        });
    }

    @Test
    void getUserByEmail_WithNullEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userRepository.getUserByEmail(null);
        });
    }

    @Test
    void getUserByEmail_WithEmptyEmail_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userRepository.getUserByEmail("");
        });
    }

    @Test
    void saveUser_WithNewUser_ShouldSaveAndReturnGuid() throws BadRequestException, IOException, UserNotFoundException {
        User newUser = UserConstructor.create("user-3@test.com", "user-3");

        String result = userRepository.saveUser(newUser);

        assertEquals(newUser.getGuid(), result);

        User savedUser = userRepository.getUserById(newUser.getGuid());
        assertEquals(newUser, savedUser);
    }

    @Test
    void saveUser_WithExistingUser_ShouldReturnExistingGuid() throws IOException, BadRequestException {
        String result = userRepository.saveUser(testUser1);

        assertEquals(testUser1.getGuid(), result);
    }

    @Test
    void saveUser_WithNullUser_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userRepository.saveUser(null);
        });
    }

    @Test
    void saveUser_WithInvalidUser_ShouldThrowBadRequestException() {
        User invalidUser = new User();

        assertThrows(BadRequestException.class, () -> {
            userRepository.saveUser(invalidUser);
        });
    }
}

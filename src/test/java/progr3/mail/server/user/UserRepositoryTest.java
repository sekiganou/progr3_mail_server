package progr3.mail.server.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void setUp() throws IOException {
        jsonFileHandler = new JsonFileHandler();

        String filePath = tempDir.resolve("users.json").toString();

        // Setup test messages
        testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        testUser2 = UserConstructor.create("user-2@test.com", "user-2");

        Class<User> userClass = User.class;

        jsonFileHandler.saveToFile(testUser1, filePath, userClass);
        jsonFileHandler.saveToFile(testUser2, filePath, userClass);

        // Create repository with mocked dependencies
        userRepository = new UserRepository(jsonFileHandler, filePath);
    }

    // @AfterEach
    // void cleanUp() {
    // File file = new File(filePath);
    // if (file.exists()) {
    // file.delete();
    // }
    // }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        // Act
        List<User> result = userRepository.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(testUser1));
        assertTrue(result.contains(testUser2));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Act
        User result = userRepository.getUserById(testUser1.getGuid());
        // Assert
        assertEquals(testUser1, result);
    }

    @Test
    void getUserById_WithInvalidId_ShouldReturnNull() {
        // Act
        User result = userRepository.getUserById("invalid-id");
        // Assert
        assertEquals(null, result);
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Act
        User result = userRepository.getUserByEmail(testUser1.getEmail());
        // Assert
        assertEquals(testUser1, result);
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldReturnNull() {
        // Act
        User result = userRepository.getUserByEmail("invalid-email");
        // Assert
        assertEquals(null, result);
    }

}

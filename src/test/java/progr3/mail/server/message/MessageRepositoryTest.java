package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Message;

public class MessageRepositoryTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private MessageRepository messageRepository;
    private Message testMessage1;
    private Message testMessage2;

    @BeforeEach
    void setUp() throws IOException {
        jsonFileHandler = new JsonFileHandler();

        String filePath = tempDir.resolve("messages.json").toString();

        // Setup test messages
        testMessage1 = MessageConstructor.create("user-1",
                Arrays.asList("user-2@test.com"), "Test Subject 1", "Test Body 1");
        testMessage2 = MessageConstructor.create("user-2",
                Arrays.asList("user-1@test.com"), "Test Subject 2", "Test Body 2");

        testMessage1.setGuid("msg-1");
        testMessage2.setGuid("msg-2");
        Class<Message> messageClass = Message.class;

        jsonFileHandler.saveToFile(testMessage1, filePath, messageClass);
        jsonFileHandler.saveToFile(testMessage2, filePath, messageClass);

        // Create repository with mocked dependencies
        messageRepository = new MessageRepository(jsonFileHandler, filePath);
    }

    // @AfterEach
    // void cleanUp() {
    // File file = new File(filePath);
    // if (file.exists()) {
    // file.delete();
    // }
    // }

    @Test
    void getAllMessage_WithValidUserId_ShouldReturnMessage() {
        // Act
        List<Message> result = messageRepository.getAllUserMessages("user-1");

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testMessage1));
    }

    @Test
    void getAllMessage_WithInvalidUserId_ShouldReturnMessage() {
        List<Message> messages = messageRepository.getAllUserMessages("invalid-user");

        // Assert
        assertEquals(0, messages.size());
    }

    @Test
    void getMessageDetails_WithValidMessageId_ShouldReturnMessage() {
        // Act
        Message result = messageRepository.getMessageDetails("msg-1");

        // Assert
        assertEquals(testMessage1, result);
    }

    @Test
    void getMessageDetails_WithInvalidMessageId_ShouldReturnNull() {
        // Act
        Message result = messageRepository.getMessageDetails("invalid-id");

        // Assert
        assertNull(result);
    }

    @Test
    void saveMessage_WithNewMessage_ShouldSaveAndReturnTrue() {
        // Arrange
        Message newMessage = MessageConstructor.create("user-3",
                Arrays.asList("user-1@test.com"), "New Subject", "New Body");

        var newGuid = "msg-3";
        newMessage.setGuid(newGuid);

        // Act
        boolean result = messageRepository.saveMessage(newMessage);

        // Assert
        assertTrue(result);

        // Verify message was added to internal map
        Message savedMessage = messageRepository.getMessageDetails(newGuid);
        assertEquals(newMessage, savedMessage);
    }

    @Test
    void saveMessage_WithExistingMessage_ShouldReturnFalse() throws IOException {
        // Act
        boolean result = messageRepository.saveMessage(testMessage1);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteMessage_WithExistingMessage_ShouldDeleteAndReturnTrue() throws IOException {
        // Act
        boolean result = messageRepository.deleteMessage(testMessage1.getGuid());

        // Assert
        assertTrue(result);

        // Verify message was removed
        Message deletedMessage = messageRepository.getMessageDetails(testMessage1.getGuid());
        assertNull(deletedMessage);
    }

    @Test
    void deleteMessage_WithNonExistingMessage_ShouldStillReturnTrue() throws IOException {
        // Act
        boolean result = messageRepository.deleteMessage("non-existing-id");

        // Assert
        assertFalse(result);
    }
}

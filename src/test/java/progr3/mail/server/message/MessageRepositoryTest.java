package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Message;

@ExtendWith(MockitoExtension.class)
public class MessageRepositoryTest {
    @Mock
    private JsonFileHandler mockJsonFileHandler;

    private MessageRepository messageRepository;
    private Message testMessage1;
    private Message testMessage2;
    private List<Message> mockMessages;

    @BeforeEach
    void setUp() throws IOException {
        // Setup test messages
        testMessage1 = MessageConstructor.create("user-1",
                Arrays.asList("user-2@test.com"), "Test Subject 1", "Test Body 1");
        testMessage2 = MessageConstructor.create("user-2",
                Arrays.asList("user-1@test.com"), "Test Subject 2", "Test Body 2");

        testMessage1.setGuid("msg-1");
        testMessage2.setGuid("msg-2");
        String filePath = "data/test/messages.json";
        Class<Message> messageClass = Message.class;

        mockJsonFileHandler.saveToFile(testMessage1, filePath, messageClass);
        mockJsonFileHandler.saveToFile(testMessage2, filePath, messageClass);

        // Mock the file loading
        mockMessages = mockJsonFileHandler.loadFromFile(filePath, messageClass);

        // Create repository with mocked dependencies
        messageRepository = new MessageRepository(mockJsonFileHandler, filePath);
    }

    void verifyMessageInList(Message message, List<Message> messages) {
        assertNotNull(messages);
        assertTrue(messages.contains(message));
    }

    @Test
    void verifyMessages_ShouldLoadMessagesFromFile() {
        // Assert
        verifyMessageInList(testMessage1, mockMessages);
        verifyMessageInList(testMessage2, mockMessages);
    }

    @Test
    void getAllMessage_WithValidUserId_ShouldReturnMessage() {
        // Act
        List<Message> result = messageRepository.getAllMessages("user-1");

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(testMessage1));
    }

    @Test
    void getAllMessage_WithInvalidUserId_ShouldReturnMessage() {
        List<Message> messages = messageRepository.getAllMessages("invalid-user");

        // Assert
        assertEquals(0, messages.size());
    }

    @Test
    void getMessageDetails_WithValidMessageId_ShouldReturnMessage() {
        // Act
        Message result = messageRepository.getMessageDetails("msg-1");

        // Assert
        assertEquals(result, testMessage1);
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
        boolean result = messageRepository.deleteMessage("msg-1");

        // Assert
        assertTrue(result);

        // Verify message was removed
        Message deletedMessage = messageRepository.getMessageDetails("msg-1");
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

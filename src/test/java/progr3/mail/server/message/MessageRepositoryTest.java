package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.message.core.MessageConstructor;
import progr3.mail.server.model.Message;

public class MessageRepositoryTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private MessageRepository messageRepository;
    private Message testMessage1;
    private Message testMessage2;

    @BeforeEach
    void setUp() throws IOException, BadRequestException {
        jsonFileHandler = new JsonFileHandler();

        String filePath = tempDir.resolve("messages.json").toString();

        testMessage1 = MessageConstructor.create("user-1",
                Arrays.asList("user-2@test.com"), "Test Subject 1", "Test Body 1");
        testMessage2 = MessageConstructor.create("user-2",
                Arrays.asList("user-1@test.com"), "Test Subject 2", "Test Body 2");

        testMessage1.setGuid("msg-1");
        testMessage2.setGuid("msg-2");
        Class<Message> messageClass = Message.class;

        jsonFileHandler.saveToFile(testMessage1, filePath, messageClass);
        jsonFileHandler.saveToFile(testMessage2, filePath, messageClass);

        messageRepository = new MessageRepository(jsonFileHandler, filePath);
    }

    @Test
    void getAllMessage_WithValidUserId_ShouldReturnMessage() throws UserNotFoundException, BadRequestException {
        List<Message> result = messageRepository.getAllUserMessages("user-1");

        assertEquals(1, result.size());
        assertEquals(testMessage1, result.get(0));
    }

    @Test
    void getAllMessage_WithInvalidUserId_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            messageRepository.getAllUserMessages("invalid-user");
        });
    }

    @Test
    void getAllMessage_WithNullUserId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            messageRepository.getAllUserMessages(null);
        });
    }

    @Test
    void getAllMessage_WithEmptyUserId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            messageRepository.getAllUserMessages("");
        });
    }

    @Test
    void getMessageDetails_WithValidMessageId_ShouldReturnMessage() throws MessageNotFoundException {
        Message result = messageRepository.getMessageDetails("msg-1");

        assertEquals(testMessage1, result);
    }

    @Test
    void getMessageDetails_WithInvalidMessageId_ShouldThrowMessageNotFoundException() {
        assertThrows(MessageNotFoundException.class, () -> {
            messageRepository.getMessageDetails("invalid-id");
        });
    }

    @Test
    void saveMessage_WithNewMessage_ShouldSaveAndReturnGuid()
            throws BadRequestException, IOException, MessageNotFoundException {
        Message newMessage = MessageConstructor.create("user-3",
                Arrays.asList("user-1@test.com"), "New Subject", "New Body");

        var newGuid = "msg-3";
        newMessage.setGuid(newGuid);

        String result = messageRepository.saveMessage(newMessage);

        assertEquals(newGuid, result);

        Message savedMessage = messageRepository.getMessageDetails(newGuid);
        assertEquals(newMessage, savedMessage);
    }

    @Test
    void saveMessage_WithExistingMessage_ShouldReturnExistingGuid() throws IOException, BadRequestException {
        String result = messageRepository.saveMessage(testMessage1);

        assertEquals(testMessage1.getGuid(), result);
    }

    @Test
    void saveMessage_WithNullMessage_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            messageRepository.saveMessage(null);
        });
    }

    @Test
    void saveMessage_WithInvalidMessage_ShouldThrowBadRequestException() {
        Message invalidMessage = new Message();
        invalidMessage.setGuid("invalid-msg");

        assertThrows(BadRequestException.class, () -> {
            messageRepository.saveMessage(invalidMessage);
        });
    }

    @Test
    void deleteMessage_WithExistingMessage_ShouldDeleteSuccessfully()
            throws IOException, MessageNotFoundException, BadRequestException {
        messageRepository.deleteMessage(testMessage1.getGuid());

        assertThrows(MessageNotFoundException.class, () -> {
            messageRepository.getMessageDetails(testMessage1.getGuid());
        });
    }

    @Test
    void deleteMessage_WithNonExistingMessage_ShouldThrowMessageNotFoundException() {
        assertThrows(MessageNotFoundException.class, () -> {
            messageRepository.deleteMessage("non-existing-id");
        });
    }

    @Test
    void deleteMessage_WithNullMessageId_ShouldThrowBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            messageRepository.deleteMessage(null);
        });
    }
}

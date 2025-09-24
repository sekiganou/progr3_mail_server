package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import progr3.mail.server.app.Logger;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Message;
import progr3.mail.server.user.UserRepository;

public class MessageServiceTest {
    @Mock
    private JsonFileHandler mockJsonFileHandler;

    private MessageService messageService;
    private Message testMessage1;
    private Message testMessage2;
    private String userFile = "data/test/users.json";
    private String messageFile = "data/test/messages.json";
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
        String messageFile = "data/test/messages.json";
        Class<Message> messageClass = Message.class;

        mockJsonFileHandler.saveToFile(testMessage1, messageFile, messageClass);
        mockJsonFileHandler.saveToFile(testMessage2, messageFile, messageClass);

        // Mock the file loading
        mockMessages = Arrays.asList(testMessage1, testMessage2);
        when(mockJsonFileHandler.loadFromFile(eq(messageFile), eq(messageClass)))
                .thenReturn(mockMessages);

        // Create repository with mocked dependencies
        var messageRepository = new MessageRepository(mockJsonFileHandler, messageFile);
        var userRepository = new UserRepository(mockJsonFileHandler, userFile);
        var logger = new Logger();
        messageService = new MessageService(logger, messageRepository, userRepository);
    }

    @AfterEach
    void cleanUp() {
        File file = new File(messageFile);
        if (file.exists()) {
            file.delete();
        }

        file = new File(userFile);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void sendMessage_WithValidRecipients_ShouldCreateAndSaveMessage() {
        // Arrange
        String senderUserId = "user-1";
        List<String> recipientEmails = Arrays.asList("user2@test.com");
        String subject = "Test Send Subject";
        String body = "Test Send Body";

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        // Assert
        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientEmails, savedMessage.getRecipientsUserGUIDs());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void sendMessage_WithMultipleValidRecipients_ShouldCreateAndSaveMessages() {
        // Arrange
        String senderUserId = "user-1";
        List<String> recipientEmails = Arrays.asList("user2@test.com", "user1@test.com");
        String subject = "Test Multiple Send Subject";
        String body = "Test Multiple Send Body";

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        // Assert
        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientEmails, savedMessage.getRecipientsUserGUIDs());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void sendMessage_WithInvalidRecipient_ShouldReturnNull() {
        // Arrange
        String senderUserId = "user-1";
        List<String> recipientEmails = Arrays.asList("invalid@test.com");
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        // Assert
        assertNull(resultMessageId);
    }

    @Test
    void sendMessage_WithMultipleRecipientsOneInvalid_ShouldReturnNull() {
        // Arrange
        String senderUserId = "user-1";
        List<String> recipientEmails = Arrays.asList("user2@test.com", "invalid@test.com");

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, "Subject", "Body");

        // Assert
        assertNull(resultMessageId);
    }

    @Test
    void replySingleToMessage_WithValidMessage_ShouldCreateReply() {
        // Arrange
        String senderUserId = "user-2";
        String originalMessageId = "msg-1";
        String subject = "Re: Original Subject";
        String body = "Reply body";

        // Act
        String result = messageService.replySingleToMessage(senderUserId, originalMessageId, subject, body);

        // Assert
        assertNotNull(result);

        Message replyMessage = messageService.getMessageDetails(result);

        assertEquals(senderUserId, replyMessage.getSenderUserGUID());
        assertEquals(Arrays.asList("user-1"), replyMessage.getRecipientsUserGUIDs()); // Reply to original sender
        assertEquals(subject, replyMessage.getSubject());
        assertEquals(body, replyMessage.getBody());
    }

    @Test
    void replySingleToMessage_WhenOriginalMessageNotFound_ShouldReturnNull() {
        // Arrange
        String senderUserId = "user-2";
        String originalMessageId = "msg-3"; // Non-existent message ID
        String subject = "Re: Original Subject";
        String body = "Reply body";

        // Act & Assert
        String result = messageService.replySingleToMessage(senderUserId, originalMessageId, subject, body);
        assertNull(result);
    }

    @Test
    void replyAllToMessage_WithValidMessage_ShouldCreateReply() {

    }

    @Test
    void replyAllToMessage_WhenOriginalMessageNotFound_ShouldReturnNull() {
    }

    @Test
    void forwardMessage_WithValidRecipients_ShouldCreateForward() {
    }

    @Test
    void forwardMessage_WithInValidRecipients_ShouldReturnFalse() {
    }

    @Test
    void getAllUserMessages_WithValidUser_ShouldGetAllMessages() {
    }

    @Test
    void getAllUserMessages_WithInvalidUser_() {
    }

    @Test
    void getMessageDetails_ShouldThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            messageService.getMessageDetails("msg-1");
        });
    }

    @Test
    void deleteMessage_ShouldThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            messageService.deleteMessage("msg-1");
        });
    }

}

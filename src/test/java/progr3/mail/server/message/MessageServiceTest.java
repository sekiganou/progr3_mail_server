package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.User;
import progr3.mail.server.user.UserConstructor;
import progr3.mail.server.user.UserRepository;

public class MessageServiceTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private MessageService messageService;
    private Message testMessage1;
    private Message testMessage2;
    private User testUser1;
    private User testUser2;
    private Logger logger;
    private MessageRepository messageRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws IOException {
        jsonFileHandler = new JsonFileHandler();
        logger = new Logger(LogLevelEnum.DEBUG,
                tempDir.resolve("test.json").toString(),
                false,
                true,
                jsonFileHandler);

        String messageFile = tempDir.resolve("messages.json").toString();
        String userFile = tempDir.resolve("users.json").toString();

        testUser1 = UserConstructor.create("user-1@test.com", "user-1");
        testUser2 = UserConstructor.create("user-2@test.com", "user-2");
        // Setup test messages
        testMessage1 = MessageConstructor.create(testUser1.getGuid(),
                Arrays.asList(testUser1.getEmail()), "Test Subject 1", "Test Body 1");
        testMessage2 = MessageConstructor.create(testUser2.getGuid(),
                Arrays.asList(testUser2.getEmail()), "Test Subject 2", "Test Body 2");

        messageRepository = new MessageRepository(jsonFileHandler, messageFile);
        userRepository = new UserRepository(jsonFileHandler, userFile);

        userRepository.saveUser(testUser1);
        userRepository.saveUser(testUser2);

        messageRepository.saveMessage(testMessage1);
        messageRepository.saveMessage(testMessage2);

        logger.startScope();

        messageService = new MessageService(messageRepository, userRepository, logger);
    }

    @AfterEach
    void cleanUp() {
        logger.endScope();
    }

    @Test
    void sendMessage_WithValidRecipients_ShouldCreateAndSaveMessage() {
        // Arrange
        String senderUserId = testUser1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail());
        String subject = "Test Send Subject";
        String body = "Test Send Body";

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        // Assert
        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientEmails, savedMessage.getRecipientsUserEmails());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void sendMessage_WithMultipleValidRecipients_ShouldCreateAndSaveMessages() {
        // Arrange
        String senderUserId = testUser1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail(), testUser2.getEmail());
        String subject = "Test Multiple Send Subject";
        String body = "Test Multiple Send Body";

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        // Assert
        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientEmails, savedMessage.getRecipientsUserEmails());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void sendMessage_WithInvalidRecipient_ShouldReturnNull() {
        // Arrange
        String senderUserId = testUser1.getGuid();
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
        String senderUserId = testUser1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail(), "invalid@test.com");

        // Act
        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, "Subject", "Body");

        // Assert
        assertNull(resultMessageId);
    }

    @Test
    void replySingleToMessage_WithValidMessage_ShouldCreateReply() {
        // Arrange
        String senderUserId = testUser2.getGuid();
        String originalMessageId = testMessage1.getGuid();
        String subject = "Re: Original Subject";
        String body = "Reply body";

        // Act
        String result = messageService.replySingleToMessage(senderUserId, originalMessageId, subject, body);

        // Assert
        assertNotNull(result);

        Message replyMessage = messageService.getMessageDetails(result);

        assertEquals(senderUserId, replyMessage.getSenderUserGUID());
        assertEquals(Arrays.asList(testUser1.getGuid()), replyMessage.getRecipientsUserEmails()); // Reply to original
                                                                                                  // sender
        assertEquals(subject, replyMessage.getSubject());
        assertEquals(body, replyMessage.getBody());
    }

    @Test
    void replySingleToMessage_WhenOriginalMessageNotFound_ShouldReturnNull() {
        // Arrange
        String senderUserId = testUser2.getGuid();
        String originalMessageId = "msg-3"; // Non-existent message ID
        String subject = "Re: Original Subject";
        String body = "Reply body";

        // Act & Assert
        String result = messageService.replySingleToMessage(senderUserId, originalMessageId, subject, body);
        assertNull(result);
    }

    @Test
    void replyAllToMessage_WithValidMessage_ShouldCreateReply() {
        // Arrange
        String senderUserId = testUser2.getGuid();
        String originalMessageId = testMessage1.getGuid();
        String subject = "Re: Original Subject";
        String body = "Reply All body";

        // Act
        String result = messageService.replyAllToMessage(senderUserId, originalMessageId, subject, body);

        // Assert
        assertNotNull(result);

        Message replyMessage = messageService.getMessageDetails(result);
        assertEquals(senderUserId, replyMessage.getSenderUserGUID());
        assertTrue(replyMessage.getRecipientsUserEmails().contains(testUser1.getGuid()));
        assertTrue(replyMessage.getRecipientsUserEmails().contains(testUser1.getEmail()));
        assertEquals(subject, replyMessage.getSubject());
        assertEquals(body, replyMessage.getBody());
    }

    @Test
    void replyAllToMessage_WhenOriginalMessageNotFound_ShouldReturnNull() {
        // Arrange
        String senderUserId = testUser2.getGuid();
        String originalMessageId = "non-existent-msg-id";
        String subject = "Re: Original Subject";
        String body = "Reply All body";

        // Act
        String result = messageService.replyAllToMessage(senderUserId, originalMessageId, subject, body);

        // Assert
        assertNull(result);
    }

    @Test
    void forwardMessage_WithValidRecipients_ShouldCreateForward() {
        // Arrange
        String forwarderUserId = testUser2.getGuid();
        String originalMessageId = testMessage1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail());

        // Act
        String resultId = messageService.forwardMessage(forwarderUserId, originalMessageId, recipientEmails);

        // Assert
        assertNotNull(resultId);

        Message forwardedMessage = messageService.getMessageDetails(resultId);
        assertEquals(forwarderUserId, forwardedMessage.getSenderUserGUID());
        assertEquals(recipientEmails, forwardedMessage.getRecipientsUserEmails());
        assertEquals(Message.IsForwarded.YES, forwardedMessage.getIsForwarded());
    }

    @Test
    void forwardMessage_WithInValidRecipients_ShouldReturnFalse() {
        // Arrange
        String forwarderUserId = testUser2.getGuid();
        String originalMessageId = "non-existent-msg-id";
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail());

        // Act
        String resultId = messageService.forwardMessage(forwarderUserId, originalMessageId, recipientEmails);

        // Assert
        assertNull(resultId);
    }

    @Test
    void getAllUserMessages_WithValidUser_ShouldGetAllMessages() {
        // Arrange
        String userId = testUser1.getGuid();

        // Act
        List<Message> result = messageService.getAllUserMessages(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage1.getGuid(), result.get(0).getGuid());
        assertEquals(testUser1.getGuid(), result.get(0).getSenderUserGUID());
    }

    @Test
    void getAllUserMessages_WithInvalidUser_ShouldReturnEmptyList() {
        // Arrange
        String invalidUserId = "non-existent-user-id";

        // Act
        List<Message> result = messageService.getAllUserMessages(invalidUserId);

        // Assert
        assertNull(result);
    }

    @Test
    void getMessageDetails_WithValidMessageId_ShouldReturnMessage() {
        // Arrange
        String messageId = testMessage1.getGuid();

        // Act
        Message result = messageService.getMessageDetails(messageId);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage1.getGuid(), result.getGuid());
        assertEquals(testMessage1.getSenderUserGUID(), result.getSenderUserGUID());
        assertEquals(testMessage1.getSubject(), result.getSubject());
        assertEquals(testMessage1.getBody(), result.getBody());
    }

    @Test
    void getMessageDetails_WithInvalidMessageId_ShouldReturnNull() {
        // Arrange
        String invalidMessageId = "non-existent-message-id";

        // Act
        Message result = messageService.getMessageDetails(invalidMessageId);

        // Assert
        assertNull(result);
    }

    @Test
    void getMessageDetails_WithNullMessageId_ShouldReturnNull() {
        // Arrange
        String nullMessageId = null;

        // Act
        Message result = messageService.getMessageDetails(nullMessageId);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteMessage_WithValidMessageId_ShouldReturnTrue() {
        // Arrange
        String messageId = testMessage1.getGuid();

        // Act
        boolean result = messageService.deleteMessage(messageId);

        // Assert
        assertTrue(result);

        // Verify message is actually deleted
        Message deletedMessage = messageService.getMessageDetails(messageId);
        assertNull(deletedMessage);
    }

    @Test
    void deleteMessage_WithInvalidMessageId_ShouldReturnFalse() {
        // Arrange
        String invalidMessageId = "non-existent-message-id";

        // Act
        boolean result = messageService.deleteMessage(invalidMessageId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteMessage_WithNullMessageId_ShouldReturnFalse() {
        // Arrange
        String nullMessageId = null;

        // Act
        boolean result = messageService.deleteMessage(nullMessageId);

        // Assert
        assertFalse(result);
    }
}
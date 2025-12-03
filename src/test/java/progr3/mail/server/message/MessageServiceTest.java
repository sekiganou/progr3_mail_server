package progr3.mail.server.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.log.LogLevelEnum;
import progr3.mail.server.log.Logger;
import progr3.mail.server.message.core.MessageConstructor;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.User;
import progr3.mail.server.user.UserRepository;
import progr3.mail.server.user.core.UserConstructor;

public class MessageServiceTest {
    private JsonFileHandler jsonFileHandler;

    @TempDir
    private Path tempDir;

    private MessageService messageService;
    private Message testMessage1;
    private Message testMessage2;
    private Message testMessage3;
    private Message testMessage4;
    private User testUser1;
    private User testUser2;
    private Logger logger;
    private MessageRepository messageRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws IOException, BadRequestException {
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

        testMessage1 = MessageConstructor.create(testUser1.getGuid(),
                Arrays.asList(testUser1.getGuid()), "Test Subject 1", "Test Body 1");
        testMessage2 = MessageConstructor.create(testUser2.getGuid(),
                Arrays.asList(testUser2.getGuid()), "Test Subject 2", "Test Body 2");
        testMessage3 = MessageConstructor.create(testUser2.getGuid(),
                Arrays.asList(testUser1.getGuid()), "Test Subject 3", "Test Body 3");
        testMessage4 = MessageConstructor.create(testUser1.getGuid(),
                Arrays.asList(testUser2.getGuid(), testUser1.getGuid()), "Test Subject 4", "Test Body 4");

        testMessage1.setDate(new Date(System.currentTimeMillis() - 200000));

        messageRepository = new MessageRepository(jsonFileHandler, messageFile);
        userRepository = new UserRepository(jsonFileHandler, userFile);

        userRepository.saveUser(testUser1);
        userRepository.saveUser(testUser2);

        messageRepository.saveMessage(testMessage1);
        messageRepository.saveMessage(testMessage2);
        messageRepository.saveMessage(testMessage3);
        messageRepository.saveMessage(testMessage4);

        logger.startScope();

        messageService = new MessageService(messageRepository, userRepository, logger);
    }

    @AfterEach
    void cleanUp() {
        logger.endScope();
    }

    @Test
    void sendMessage_WithValidRecipients_ShouldCreateAndSaveMessage()
            throws BadRequestException, IOException, UserNotFoundException, MessageNotFoundException {
        String senderUserId = testUser1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail());
        List<String> recipientGUIDs = Arrays.asList(testUser2.getGuid());
        String subject = "Test Send Subject";
        String body = "Test Send Body";

        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientGUIDs, savedMessage.getRecipientsUserGUIDs());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void sendMessage_WithMultipleValidRecipients_ShouldCreateAndSaveMessages()
            throws BadRequestException, IOException, UserNotFoundException, MessageNotFoundException {
        String senderUserId = testUser1.getGuid();
        List<String> recipientEmails = Arrays.asList(testUser2.getEmail(), testUser2.getEmail());
        List<String> recipientGUIDs = Arrays.asList(testUser2.getGuid(), testUser2.getGuid());

        String subject = "Test Multiple Send Subject";
        String body = "Test Multiple Send Body";

        String resultMessageId = messageService.sendMessage(senderUserId, recipientEmails, subject, body);

        assertNotNull(resultMessageId);

        Message savedMessage = messageService.getMessageDetails(resultMessageId);
        assertEquals(senderUserId, savedMessage.getSenderUserGUID());
        assertEquals(recipientGUIDs, savedMessage.getRecipientsUserGUIDs());
        assertEquals(subject, savedMessage.getSubject());
        assertEquals(body, savedMessage.getBody());
        assertNotNull(savedMessage.getGuid());
        assertNotNull(savedMessage.getDate());
    }

    @Test
    void getAllUserMessages_WithValidUser_ShouldGetAllMessages() throws BadRequestException, UserNotFoundException {
        String userId = testUser1.getGuid();

        List<Message> result = messageService.getAllUserMessages(userId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(testMessage1.getGuid(), result.get(0).getGuid());
        assertEquals(testUser1.getGuid(), result.get(0).getSenderUserGUID());
    }

    @Test
    void getAllUserMessages_WithInvalidUser_ShouldThrowUserNotFoundException()
            throws BadRequestException, UserNotFoundException {
        String invalidUserId = "non-existent-user-id";
        List<Message> result = messageService.getAllUserMessages(invalidUserId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getAllUserMessages_WhereOneMessageIsDeletedForUser_ShouldNotReturnDeletedMessage()
            throws BadRequestException, UserNotFoundException, IOException, MessageNotFoundException {
        String userId = testUser1.getGuid();
        String messageIdToDelete = testMessage3.getGuid();

        messageService.deleteMessage(messageIdToDelete, userId);

        List<Message> result = messageService.getAllUserMessages(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getUserMessagesWithDateFilter_WithValidUser_ShouldGetFilteredMessages()
            throws UserNotFoundException, BadRequestException {
        String userId = testUser1.getGuid();
        Date start = new Date(System.currentTimeMillis() - 100000);
        Date end = new Date(System.currentTimeMillis() + 100000);

        List<Message> result = messageService.getUserMessagesWithFilters(userId, start, end);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testMessage3.getGuid(), result.get(0).getGuid());
        assertEquals(testUser2.getGuid(), result.get(0).getSenderUserGUID());
    }

    @Test
    void getUserMessagesWithDateFilter_WithNoFilter_ShouldGetAllMessages()
            throws UserNotFoundException, BadRequestException {
        String userId = testUser1.getGuid();

        List<Message> result = messageService.getUserMessagesWithFilters(userId, null, null);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(testMessage1.getGuid(), result.get(0).getGuid());
        assertEquals(testUser1.getGuid(), result.get(0).getSenderUserGUID());
        assertEquals(testMessage3.getGuid(), result.get(1).getGuid());
        assertEquals(testUser2.getGuid(), result.get(1).getSenderUserGUID());
    }

    @Test
    void getUserMessagesWithDateFilter_WithInvalidUser_ShouldReturnEmptyList()
            throws UserNotFoundException, BadRequestException {
        String invalidUserId = "non-existent-user-id";
        Date start = new Date(System.currentTimeMillis() - 100000);
        Date end = new Date(System.currentTimeMillis() + 100000);

        List<Message> result = messageService.getUserMessagesWithFilters(invalidUserId, start, end);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getMessageDetails_WithValidMessageId_ShouldReturnMessage()
            throws MessageNotFoundException, BadRequestException {
        String messageId = testMessage1.getGuid();

        Message result = messageService.getMessageDetails(messageId);

        assertNotNull(result);
        assertEquals(testMessage1.getGuid(), result.getGuid());
        assertEquals(testMessage1.getSenderUserGUID(), result.getSenderUserGUID());
        assertEquals(testMessage1.getSubject(), result.getSubject());
        assertEquals(testMessage1.getBody(), result.getBody());
    }

    @Test
    void getMessageDetails_WithInvalidMessageId_ShouldThrowMessageNotFoundException() {
        String invalidMessageId = "non-existent-message-id";

        assertThrows(MessageNotFoundException.class, () -> {
            messageService.getMessageDetails(invalidMessageId);
        });
    }

    @Test
    void getMessageDetails_WithNullMessageId_ShouldThrowMessageNotFoundException() {
        String nullMessageId = null;

        assertThrows(MessageNotFoundException.class, () -> {
            messageService.getMessageDetails(nullMessageId);
        });
    }

    @Test
    void deleteMessage_WithValidMessageId_ShouldDeleteSuccessfully()
            throws MessageNotFoundException, BadRequestException, IOException {
        String messageId = testMessage1.getGuid();
        String userId = testUser1.getGuid();

        messageService.deleteMessage(messageId, userId);

        assertThrows(MessageNotFoundException.class, () -> {
            messageService.getMessageDetails(messageId);
        });
    }

    @Test
    void deleteMessage_WithValidMessageId_MultipleRecipients_ShouldRemoveRecipientOnly()
            throws MessageNotFoundException, BadRequestException, IOException {
        String messageId = testMessage4.getGuid();
        String userId = testUser1.getGuid();

        assertEquals(2, testMessage4.getRecipientsUserGUIDs().size());

        messageService.deleteMessage(messageId, userId);

        Message remainingMessage = messageService.getMessageDetails(messageId);
        List<String> remainingRecipients = remainingMessage.getRecipientsUserGUIDs();
        List<String> deletedRecipients = remainingMessage.getDeletedRecipientsUserGUIDs();

        assertEquals(2, remainingRecipients.size());
        assertEquals(1, deletedRecipients.size());
        assertEquals(userId, deletedRecipients.get(0));
    }

    @Test
    void deleteMessage_WithInvalidMessageId_ShouldThrowMessageNotFoundException() {
        String invalidMessageId = "non-existent-message-id";

        assertThrows(MessageNotFoundException.class, () -> {
            messageService.deleteMessage(invalidMessageId, "");
        });
    }
}

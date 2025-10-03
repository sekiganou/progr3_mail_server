package progr3.mail.server.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.Request;
import progr3.mail.server.model.Response;
import progr3.mail.server.model.MailRequest.DeleteMessageBody;
import progr3.mail.server.model.MailRequest.ForwardMessageBody;
import progr3.mail.server.model.MailRequest.GetMessageDetailsBody;
import progr3.mail.server.model.MailRequest.GetMessagesBody;
import progr3.mail.server.model.MailRequest.GetUserDetailsBody;
import progr3.mail.server.model.MailRequest.LoginBodyIn;
import progr3.mail.server.model.MailRequest.LogoutBody;
import progr3.mail.server.model.MailRequest.ReplyAllMessageBody;
import progr3.mail.server.model.MailRequest.ReplySingleMessageBody;
import progr3.mail.server.model.MailRequest.SendMessageBody;
import progr3.mail.server.model.MailResponse.LoginBodyOut;
import progr3.mail.server.user.UserService;

public class ClientHandler implements Runnable {

    private ILogger logger;
    private MessageService messageService;
    private UserService userService;
    private Socket clientSocket;
    private ActiveUsers activeUsers;

    public ClientHandler(Socket clientSocket, ILogger logger, ActiveUsers activeUsers, UserService userService,
            MessageService messageService) {
        this.logger = logger;
        this.clientSocket = clientSocket;
        this.messageService = messageService;
        this.userService = userService;
        this.activeUsers = activeUsers;
    }

    private void sendResponse(Response response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonResponse = mapper.writeValueAsString(response);
            clientSocket.getOutputStream().write(jsonResponse.getBytes());
            clientSocket.getOutputStream().flush();
        } catch (IOException e) {
            logger.logError("Error sending response to client", e);
        }
    }

    private void processRequest(Request request) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            switch (request.getCommand()) {
                case LOGIN:
                    LoginBodyIn loginBodyIn = mapper.readValue(
                            request.getBody(),
                            LoginBodyIn.class);
                    var user = userService.login(loginBodyIn.getEmail());
                    activeUsers.addUser(user.getGuid(), clientSocket);

                    // Handle login with loginBody.getEmail() and loginBody.getPassword()
                    break;

                case LOGOUT:
                    LogoutBody logoutBody = mapper.readValue(
                            request.getBody(),
                            LogoutBody.class);
                    activeUsers.removeUser(logoutBody.getUserId());
                    // Handle logout with logoutBody.getUserId()
                    break;

                case GET_USER_DETAILS:
                    GetUserDetailsBody getUserBody = mapper.readValue(
                            request.getBody(),
                            GetUserDetailsBody.class);
                    // Handle get user details with getUserBody.getUserId()
                    break;

                case SEND_MESSAGE:
                    SendMessageBody sendBody = mapper.readValue(
                            request.getBody(),
                            SendMessageBody.class);
                    String messageId = messageService.sendMessage(
                            sendBody.getSenderUserId(),
                            sendBody.getRecipientsUserEmails(),
                            sendBody.getSubject(),
                            sendBody.getBody());
                    // Send response back to client with messageId
                    break;

                case REPLY_SINGLE_MESSAGE:
                    ReplySingleMessageBody replyBody = mapper.readValue(
                            request.getBody(),
                            ReplySingleMessageBody.class);
                    String replyId = messageService.replySingleToMessage(
                            replyBody.getSenderUserId(),
                            replyBody.getMessageId(),
                            replyBody.getSubject(),
                            replyBody.getBody());
                    // Send response back to client with replyId
                    break;

                case REPLY_ALL_MESSAGE:
                    ReplyAllMessageBody replyAllBody = mapper.readValue(
                            request.getBody(),
                            ReplyAllMessageBody.class);
                    String replyAllId = messageService.replyAllToMessage(
                            replyAllBody.getSenderUserId(),
                            replyAllBody.getMessageId(),
                            replyAllBody.getSubject(),
                            replyAllBody.getBody());
                    // Send response back to client with replyAllId
                    break;

                case FORWARD_MESSAGE:
                    ForwardMessageBody forwardBody = mapper.readValue(
                            request.getBody(),
                            ForwardMessageBody.class);
                    String forwardId = messageService.forwardMessage(
                            forwardBody.getForwarderUserId(),
                            forwardBody.getMessageId(),
                            forwardBody.getRecipientsUserEmails());
                    // Send response back to client with forwardId
                    break;

                case GET_MESSAGES:
                    GetMessagesBody getMessagesBody = mapper.readValue(
                            request.getBody(),
                            GetMessagesBody.class);
                    List<Message> messages = messageService.getAllUserMessages(
                            getMessagesBody.getUserId());
                    // Send response back to client with messages list
                    break;

                case GET_MESSAGE_DETAILS:
                    GetMessageDetailsBody getDetailsBody = mapper.readValue(
                            request.getBody(),
                            GetMessageDetailsBody.class);
                    Message message = messageService.getMessageDetails(
                            getDetailsBody.getMessageId());
                    // Send response back to client with message
                    break;

                case DELETE_MESSAGE:
                    DeleteMessageBody deleteBody = mapper.readValue(
                            request.getBody(),
                            DeleteMessageBody.class);
                    messageService.deleteMessage(
                            deleteBody.getMessageId());
                    // Send response back to client with success/failure
                    break;

                default:
                    logger.logError("Unknown command: " + request.getCommand(), null);
                    break;
            }
        } catch (Exception e) {
            logger.logError("Error processing request: " + request.getCommand(), e);
        }
    }

    @Override
    public void run() {
        byte[] inputStreamBytes;
        try {
            InputStream inputStream = clientSocket.getInputStream();
            inputStreamBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            logger.logError("Error handling client connection", e);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        Request request = new Request();

        try {
            request = mapper.readValue(inputStreamBytes, Request.class);
        } catch (Exception e) {
            logger.logError("Failed to parse request", e);
            return;
        }

        processRequest(request);

        try {
            clientSocket.close();
        } catch (Exception e) {
            logger.logError("Error closing client socket", e);
        }

    }

}

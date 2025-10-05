package progr3.mail.server.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import progr3.mail.server.exceptions.BadRequestException;
import progr3.mail.server.exceptions.MessageNotFoundException;
import progr3.mail.server.exceptions.UserNotFoundException;
import progr3.mail.server.log.ILogger;
import progr3.mail.server.message.MessageService;
import progr3.mail.server.model.Message;
import progr3.mail.server.model.Request;
import progr3.mail.server.model.Response;
import progr3.mail.server.model.User;
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

    private Response processRequest(Request request) {
        System.out.println("Processing request: " + request.getCommand());
        ObjectMapper mapper = new ObjectMapper();
        Response response = new Response();
        String logMessage = "";
        try {
            switch (request.getCommand()) {
                case LOGIN:
                    LoginBodyIn loginBodyIn = mapper.readValue(
                            request.getBody(),
                            LoginBodyIn.class);
                    User user = userService.login(loginBodyIn.getEmail());
                    activeUsers.addUser(user.getGuid(), clientSocket);
                    var loginBodyOut = new LoginBodyOut();
                    loginBodyOut.setEmail(user.getEmail());
                    logMessage = "Login Successful for email: " + loginBodyIn.getEmail();
                    response = ResponseConstructor.success(
                            logMessage,
                            loginBodyOut);
                    logger.logInfo(logMessage);
                    break;
                case LOGOUT:
                    LogoutBody logoutBody = mapper.readValue(
                            request.getBody(),
                            LogoutBody.class);
                    activeUsers.removeUser(logoutBody.getUserId());
                    response = ResponseConstructor.success("Logout Successful", logoutBody.getUserId());
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
        } catch (MessageNotFoundException e) {
            String message = "Message not found";
            logger.logError(message, e);
            response = ResponseConstructor.notFound(message, null);
        } catch (BadRequestException e) {
            String message = "Bad request";
            logger.logError(message, e);
            response = ResponseConstructor.badRequest(message, null);
        } catch (UserNotFoundException e) {
            String message = "User not found";
            logger.logError(message, e);
            response = ResponseConstructor.notFound(message, null);
        } catch (IOException e) {
            String message = "IO Exception while processing request";
            logger.logError(message, e);
            response = ResponseConstructor.internalServerError(message, null);
        } catch (Exception e) {
            String message = "Unexpected error";
            logger.logError(message, e);
            response = ResponseConstructor.internalServerError(message, null);
        }

        return response;
    }

    @Override
    public void run() {
        logger.startScope();

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

        System.out.println("Received request: " + request.getCommand());

        var response = processRequest(request);

        System.out.println("Sending response: " + response.getMessage());

        try {
            String jsonResponse = mapper.writeValueAsString(response);
            clientSocket.getOutputStream().write(jsonResponse.getBytes());
            clientSocket.getOutputStream().flush();
        } catch (IOException e) {
            logger.logError("Error sending response to client", e);
        }

        try {
            clientSocket.close();
        } catch (Exception e) {
            logger.logError("Error closing client socket", e);
        }
        logger.endScope();

    }

}

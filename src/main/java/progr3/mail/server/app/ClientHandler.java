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
import progr3.mail.server.model.MailRequest.ForwardMessageBody;
import progr3.mail.server.model.MailRequest.ReplyAllMessageBody;
import progr3.mail.server.model.MailRequest.ReplySingleMessageBody;
import progr3.mail.server.model.MailRequest.SendMessageBody;
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

    private Response logAndCreateResponse(String message, Object body) {
        logger.logInfo(message);
        return ResponseConstructor.success(message, body);
    }

    private Response processRequest(Request request) {
        System.out.println("Processing request: " + request.getCommand());
        ObjectMapper mapper = new ObjectMapper();
        Response response = new Response();
        try {
            switch (request.getCommand()) {
                case LOGIN:
                    String email = request.getBody();

                    User user = userService.login(email);
                    activeUsers.addUser(user.getGuid(), clientSocket);

                    response = logAndCreateResponse("Login successfull", user);
                    break;
                case LOGOUT:
                    String guid = request.getBody();
                    activeUsers.removeUser(guid);

                    response = logAndCreateResponse("Logout successfull", null);
                    break;
                case SEND_MESSAGE:
                    SendMessageBody sendMessageBody = mapper.readValue(
                            request.getBody(),
                            SendMessageBody.class);
                    String messageId = messageService.sendMessage(
                            sendMessageBody.getSenderUserId(),
                            sendMessageBody.getRecipientsUserEmails(),
                            sendMessageBody.getSubject(),
                            sendMessageBody.getBody());

                    response = logAndCreateResponse("Message sent successfully", messageId);
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

                    response = logAndCreateResponse("Reply sent successfully", replyId);
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

                    response = logAndCreateResponse("Reply all sent successfully", replyAllId);
                    break;

                case FORWARD_MESSAGE:
                    ForwardMessageBody forwardBody = mapper.readValue(
                            request.getBody(),
                            ForwardMessageBody.class);
                    String forwardId = messageService.forwardMessage(
                            forwardBody.getForwarderUserId(),
                            forwardBody.getMessageId(),
                            forwardBody.getRecipientsUserEmails());
                    response = logAndCreateResponse("Message forwarded successfully", forwardId);
                    break;

                case GET_MESSAGES:
                    String userId = request.getBody();
                    List<Message> messages = messageService.getAllUserMessages(
                            userId);
                    response = logAndCreateResponse("Messages retrieved successfully", messages);
                    break;

                case DELETE_MESSAGE:
                    String deleteMessageId = request.getBody();
                    messageService.deleteMessage(
                            deleteMessageId);
                    response = logAndCreateResponse("Message deleted successfully", null);
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

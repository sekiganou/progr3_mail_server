package progr3.mail.server.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
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
import progr3.mail.server.model.MailRequest.GetMessagesWithFiltersBody;
import progr3.mail.server.model.MailRequest.SendMessageBody;
import progr3.mail.server.user.UserService;

public class ClientHandler implements Runnable {

    private ILogger logger;
    private MessageService messageService;
    private UserService userService;
    private Socket clientSocket;
    private final int CONNECTION_TIMEOUT_MS = 30000;

    public ClientHandler(Socket clientSocket, ILogger logger, UserService userService,
            MessageService messageService) {
        this.logger = logger;
        this.clientSocket = clientSocket;
        this.messageService = messageService;
        this.userService = userService;
    }

    private Response logAndCreateResponse(String message, Object body) {
        logger.logInfo(message);
        return ResponseConstructor.success(message, body);
    }

    private Response processRequest(Request request) {
        logger.logInfo("Started processing request: " + request.getCommand());
        ObjectMapper mapper = new ObjectMapper();
        Response response = new Response();
        try {
            switch (request.getCommand()) {
                case HEALTH:
                    response = logAndCreateResponse("Server is healthy", Response.Status.OK);
                    break;

                case LOGIN:
                    String email = request.getBody();

                    User user = userService.login(email);

                    response = logAndCreateResponse("Login successful for email: " + user.getEmail(), user);
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

                case GET_MESSAGES:
                    String userId = request.getBody();
                    List<Message> messages = messageService.getAllUserMessages(
                            userId);
                    response = logAndCreateResponse("Messages retrieved successfully", messages);
                    break;

                case GET_MESSAGES_WITH_FILTERS:
                    GetMessagesWithFiltersBody filtersBody = mapper.readValue(
                            request.getBody(),
                            GetMessagesWithFiltersBody.class);
                    List<Message> filteredMessages = messageService.getUserMessagesWithFilters(
                            filtersBody.getUserId(),
                            filtersBody.getStartDate(),
                            filtersBody.getEndDate());
                    response = logAndCreateResponse("Filtered messages retrieved successfully", filteredMessages);
                    break;

                case DELETE_MESSAGE:
                    DeleteMessageBody deleteMessageBody = mapper.readValue(request.getBody(), DeleteMessageBody.class);
                    messageService.deleteMessage(
                            deleteMessageBody.getMessageId(), deleteMessageBody.getUserId());
                    response = logAndCreateResponse("Message deleted successfully", "Deleted");
                    break;

                case GET_USERS:
                    List<String> userIds = mapper.readValue(
                            request.getBody(),
                            mapper.getTypeFactory()
                                    .constructCollectionType(List.class, String.class));
                    List<User> users = userService.getUsersByIds(userIds);
                    response = logAndCreateResponse("Users retrieved successfully", users);
                    break;

                default:
                    logger.logError("Unknown command: " + request.getCommand());
                    break;
            }
        } catch (MessageNotFoundException e) {
            String message = "Message not found";
            logger.logError(message);
            response = ResponseConstructor.notFound(message, null);
        } catch (BadRequestException e) {
            String message = "Bad request";
            logger.logError(message);
            response = ResponseConstructor.badRequest(message, null);
        } catch (UserNotFoundException e) {
            String message = e.getMessage();
            logger.logError(message);
            response = ResponseConstructor.notFound(message, null);
        } catch (IOException e) {
            String message = "IO Exception while processing request";
            logger.logError(message);
            response = ResponseConstructor.internalServerError(message, null);
        } catch (Exception e) {
            String message = "Unexpected error";
            logger.logError(message);
            response = ResponseConstructor.internalServerError(message, null);
        }

        logger.logInfo("Finished processing request: " + request.getCommand());

        return response;
    }

    private byte[] readInputStream(Socket clientSocket) {

        try {
            InputStream inputStream = clientSocket.getInputStream();
            byte[] inputStreamBytes = inputStream.readAllBytes();
            return inputStreamBytes;
        } catch (IOException e) {
            logger.logError("Error handling client connection");
            return null;
        }
    }

    private Request parseRequest(byte[] inputStreamBytes) {
        ObjectMapper mapper = new ObjectMapper();
        Request request = new Request();

        try {
            request = mapper.readValue(inputStreamBytes, Request.class);

        } catch (Exception e) {
            logger.logError("Failed to parse request");
        }
        return request;
    }

    private void writeOutputStream(Socket clientSocket, Response response) {
        System.out.println("Sending response: " + response.getMessage());

        ObjectMapper mapper = new ObjectMapper();

        try {
            String jsonResponse = mapper.writeValueAsString(response);

            clientSocket.getOutputStream().write(jsonResponse.getBytes());
            clientSocket.getOutputStream().flush();
        } catch (IOException e) {
            logger.logError("Error sending response to client");
        }
    }

    @Override
    public void run() {
        try {
            clientSocket.setSoTimeout(CONNECTION_TIMEOUT_MS);

            logger.startScope();
            logger.logInfo("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            byte[] inputStreamBytes = readInputStream(clientSocket);

            Request request = parseRequest(inputStreamBytes);

            Response response = processRequest(request);

            writeOutputStream(clientSocket, response);
        } catch (SocketException e) {
            logger.logError("Socket timeout: No data received from client");
        }

        try {
            clientSocket.close();
        } catch (Exception e) {
            logger.logError("Error closing client socket");
        } finally {
            logger.logInfo("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            logger.endScope();
        }

    }

}

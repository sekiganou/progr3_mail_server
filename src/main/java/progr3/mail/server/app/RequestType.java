package progr3.mail.server.app;

public enum RequestType {

    // Message-related requests
    GET_MESSAGES,
    GET_MESSAGE_DETAILS,
    SAVE_MESSAGE,
    DELETE_MESSAGE,

    // User-related requests
    LOGIN,
    GET_USER_DETAILS
}

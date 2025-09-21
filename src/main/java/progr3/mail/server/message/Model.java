package progr3.mail.server.message;

import java.util.Map;
import java.util.stream.Collectors;

import progr3.mail.server.model.Message;

public class Model {
    private final IO io;
    private static Map<String, Message> messages = Map.of();

    public Model() {
        this.io = new IO();
        Model.messages = io.loadMessages().stream().collect(Collectors.toMap(Message::getTitle, message -> message));
    }

    public Message getMessageByTitle(String title) {
        return messages.get(title);
    }

}

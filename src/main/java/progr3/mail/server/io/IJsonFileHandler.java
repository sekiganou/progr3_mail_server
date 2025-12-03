package progr3.mail.server.io;

import java.io.IOException;
import java.util.List;

public interface IJsonFileHandler {
    public <T> void saveToFile(T obj, String filename, Class<T> clazz) throws IOException;

    public <T> List<T> loadFromFile(String filename, Class<T> clazz) throws IOException;

    public <T> void updateInFile(T existingObj, T newObj, String filename, Class<T> clazz) throws IOException;

    public <T> void removeFromFile(T obj, String filename, Class<T> clazz) throws IOException;
}

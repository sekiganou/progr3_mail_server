package progr3.mail.server.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileHandler {
    private final ObjectMapper mapper;
    
    public JsonFileHandler() {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    public void saveToFile(Object obj, String filename) throws IOException {
        mapper.writeValue(new File(filename), obj);
    }
    
    public <T> List<T> loadFromFile(String filename, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(filename), mapper.getTypeFactory().constructCollectionType(List.class, clazz)
    );
    }
}
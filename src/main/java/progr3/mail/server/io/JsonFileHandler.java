package progr3.mail.server.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileHandler implements IJsonFileHandler {
    private final ObjectMapper mapper;
    private final Map<String, ReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    public JsonFileHandler() {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> void saveToFile(T obj, String filename, Class<T> clazz) throws IOException {
        ReadWriteLock lock = fileLocks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            File file = new File(filename);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            List<T> data;

            if (file.length() > 0) {
                data = loadFromFile(filename, clazz);
            } else {
                data = new ArrayList<>();
            }

            data.add((T) obj);
            mapper.writeValue(file, data);

        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> List<T> loadFromFile(String filename, Class<T> clazz) throws IOException {
        ReadWriteLock lock = fileLocks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock());
        List<T> result = new ArrayList<>();
        lock.readLock().lock();
        try {
            File file = new File(filename);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            if (file.length() == 0) {
                return new ArrayList<>();
            }

            result = mapper.readValue(file,
                    mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    public <T> void removeFromFile(T obj, String filename, Class<T> clazz) throws IOException {
        ReadWriteLock lock = fileLocks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return;
            }

            List<T> data;

            if (file.exists() && file.length() > 0) {
                data = loadFromFile(filename, clazz);
            } else {
                return;
            }

            data.remove(obj);
            mapper.writeValue(file, data);

        } finally {
            lock.writeLock().unlock();
        }
    }
}
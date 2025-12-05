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
    // Map to hold locks for each file. ConcurrentHashMap to allow concurrent access
    // to the map itself.
    private final Map<String, ReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    public JsonFileHandler() {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> void saveToFile(T obj, String filename, Class<T> clazz) throws IOException {
        // Get or create the lock for this file. It supports reentrancy, meaning the
        // same lock can be acquired multiple times by the same thread without causing a
        // deadlock. ComputeIfAbsent ensures that only one lock is created per filename.
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

    public <T> void updateInFile(T existingObj, T newObj, String filename, Class<T> clazz) throws IOException {
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

            int index = data.indexOf(existingObj);
            if (index != -1) {
                data.set(index, newObj);
                mapper.writeValue(file, data);
            }

        } finally {
            lock.writeLock().unlock();
        }
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
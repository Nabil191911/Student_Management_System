package Student_management_system;

import java.io.*;
import java.util.*;

/*
 * User credential storage and authentication system using users.txt.
 */
public class UserStore {
    private static final String FILE_PATH = "users.txt";
    private static final Map<String, String> memoryUsers = new HashMap<>();

    static {
        loadFromFile();
        migratePropertiesFile();
    }

    // Migrate legacy users.properties file if present
    private static synchronized void migratePropertiesFile() {
        File propFile = new File("users.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                Properties prop = new Properties();
                prop.load(input);
                for (String key : prop.stringPropertyNames()) {
                    memoryUsers.put(key.trim().toLowerCase(), prop.getProperty(key).trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveAllToFile();
            propFile.delete();
        }
    }

    // Load registered users from users.txt
    private static synchronized void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            memoryUsers.put(parts[0].trim().toLowerCase(), parts[1].trim());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Write user credentials to users.txt
    private static synchronized void saveAllToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, String> entry : memoryUsers.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Register new user account
    public static synchronized boolean registerUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            return false;
        }
        memoryUsers.put(username.trim().toLowerCase(), password.trim());
        saveAllToFile();
        return true;
    }

    // Validate login credentials
    public static synchronized boolean validateUser(String username, String password) {
        if (username == null || password == null) return false;
        String storedPassword = memoryUsers.get(username.trim().toLowerCase());
        return storedPassword != null && storedPassword.equals(password.trim());
    }

    // Check if username exists
    public static synchronized boolean userExists(String username) {
        if (username == null) return false;
        return memoryUsers.containsKey(username.trim().toLowerCase());
    }
}

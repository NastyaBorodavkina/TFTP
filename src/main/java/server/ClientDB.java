package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

public class ClientDB {
    Map<String, String> users = new HashMap<String, String>();

    public ClientDB(String filename) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        reader.lines().forEach((line) -> {
            String[] tokens = line.split(" ");
            users.put(tokens[0], tokens[1]);
        });
    }

    public boolean hasUser(String login) {
        return users.containsKey(login);
    }

    public boolean isAuthorized(String login, String passwd) {
        if(hasUser(login)) {
            return users.get(login).equals(passwd);
        }
        return false;
    }

}

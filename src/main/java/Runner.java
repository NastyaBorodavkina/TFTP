import client.Client;
import client.CommandLine;
import org.docopt.Docopt;
import server.ClientDB;
import server.Server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Runner {
    public static boolean verbose = false;
    private final static Logger LOGGER = Logger.getGlobal();
    private final static String USERS_DB_FILENAME = "users.txt";

    // Usage text
    static final String doc =
            "Usage: "
                    + "tftp [-h] [-v | --verbose] (<host> <port> [--login <login> --password <passwd>] | -s <port> [-u <file> | --users <file>])\n"
                    + "\n"
                    + "Options:\n"
                    + "-h --help                  show this\n"
                    + "-s <port>                  server mode\n"
                    + "-u <file>, --users <file>  credentials file\n"
                    + "-v --verbose                  print more info\n"
                    + "\n";

    public static void main(String[] args) {
        // Parse command line options
        Map<String, Object> opts = new Docopt(doc).withVersion("TFTP 0.1").parse(args);

        // Setup logger formatter
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n");
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Set verbosity
        if(opts.get("--verbose").equals(true)) {
            LOGGER.setLevel(Level.FINEST);
        } else {
            LOGGER.setLevel(Level.INFO);
        }

        // Select mode
        if(opts.get("-s").equals(true)) {
            runServer(opts);
        } else {
            runClient(opts);
        }
    }

    public static void runServer(Map<String, Object> opts) {
        int port = Integer.parseInt((String) opts.get("<port>"));
        String file = (String) opts.get("<file>");
        ClientDB clients = null;

        // Try to open database with login-password pairs
        try {
            clients = new ClientDB(file);
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.warning(file + " not found. Falling back to default");
        }

        // If not specified, try to open it by default path
        // On fail, falling back to no checking authorization mode
        if(clients == null) {
            try {
                clients = new ClientDB(USERS_DB_FILENAME);
            } catch (FileNotFoundException e) {
                LOGGER.severe("Default file " + USERS_DB_FILENAME + " not found. Falling back to no-authorization mode.");
            }
        }

        // Initialize and start server
        Server server = new Server(port, clients);
        try {
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runClient(Map<String, Object> opts) {
        String host = (String) opts.get("<host>");
        Integer port = Integer.parseInt((String) opts.get("<port>"));
        String login = (String) opts.get("<login>");
        String passwd = (String) opts.get("<passwd>");

        // Lookup passed address
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Create client
        Client client = null;
        try {
            client = new Client(addr, port, login, passwd);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Launch command interpreter
        CommandLine cl = new CommandLine(client);
        try {
            cl.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

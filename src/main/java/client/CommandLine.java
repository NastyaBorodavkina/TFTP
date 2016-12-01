package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLine {
    private boolean shouldExit = false;
    private Client client;
    private static String helpMessage = "Commands:\n"
            + "  (h)elp                  print this message\n"
            + "  (d)ownload <filename> download file\n"
            + "  (u)pload   <filename> upload file\n"
            + "  (q)uit\n\n";

    public CommandLine(Client client) {
        this.client = client;
    }

    public void run() throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(helpMessage);

        while(!shouldExit) {
            System.out.print("> ");
            String input = stdin.readLine();
            evaluate(input);
        }
    }

    enum Opcode {
        UP,
        DOWN
    };

    private void evaluate(String input) throws IOException {
        Opcode mode = Opcode.DOWN;
        switch (input.charAt(0)) {
            case 'h':
                System.out.println(helpMessage);
                break;
            case 'q':
                shouldExit = true;
                break;
            case 'u':
                mode = Opcode.UP;
            case 'd':
                String filename = input.substring(input.indexOf(" ") + 1);
                perform(mode, filename);
                break;
        }
    }

    private void perform(Opcode mode, String filename) throws IOException {
        filename = "./" + filename;

        long time = System.currentTimeMillis();
        long transfered;

        if(mode.equals(Opcode.UP)) {
            transfered = client.writeFile(filename, filename);
        } else {
            transfered = client.readFile(filename, filename);
        }

        time = System.currentTimeMillis() - time;

        long kylobytes = transfered / 1000;
        double seconds = time / (double) 1000;

        System.out.println("Transfered " + kylobytes + " kylobytes. Speed: " + kylobytes / seconds + "KB/s");
    }
}

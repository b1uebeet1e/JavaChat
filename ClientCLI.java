import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Client
 */
public class ClientCLI {

    private Socket server;

    private String host;

    private int port;

    public ClientCLI(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws UnknownHostException, IOException {

        server = new Socket(host, port);
        System.out.println("Successful connection to server!");

        PrintStream output = new PrintStream(server.getOutputStream());

        System.out.println("Chat intiated:");

        new Thread(new MessageHandler(server.getInputStream())).start();

        Scanner in = new Scanner(System.in);

        while (in.hasNextLine()) {
            output.println(in.nextLine());
        }

        output.close();
        in.close();
        server.close();
    }

    // Main routine
    // Usage: java Client <host> <port>
    public static void main(String[] args) throws UnknownHostException, IOException {
        String host;
        int port;
        try {
            // Get the port # from the command line
            if (args.length != 2) {
                System.out.println("Improper declaration!");
                System.out.println("Proper usage: java Client <host> <port>");
                return;
            }

            host = args[0];
            port = Integer.parseInt(args[1]);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Client <host> <port>");
            return;
        } catch (NumberFormatException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Client <host> <port>");
            return;
        }

        new ClientCLI(host, port).run();
    }
}

class MessageHandler implements Runnable {

    private InputStream server;

    public MessageHandler(InputStream server) {
        this.server = server;
    }

    public void run() {
        String message;

        Scanner input = new Scanner(server);

        while (input.hasNextLine()) {
            message = input.nextLine();
            System.out.println(message);
        }

        input.close();
    }
}

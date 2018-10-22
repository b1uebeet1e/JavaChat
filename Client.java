import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Client
 */
public class Client {

    private Socket client;

    private String host;

    private int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws UnknownHostException, IOException {

        client = new Socket(host, port);
        System.out.println("Successful connection to server!");

        PrintStream output = new PrintStream(client.getOutputStream());

        System.out.println("Chat intiated:");

        new Thread(new MessageHandler(client.getInputStream()).start());

        Scanner in = new Scanner(System.in);

        while (in.hasNextLine()) {
            output.println(in.nextLine());
        }

        output.close();
        in.close();
        client.close();
    }

    public static void main(String[] args) {

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
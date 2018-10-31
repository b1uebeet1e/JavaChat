import java.io.InputStream;
import java.util.Scanner;

/**
 * MessageHandler
 */
public class MessageHandler implements Runnable {

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
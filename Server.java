import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;

/**
 * Server
 */
public class Server {
    // The ServerSocket we'll use for accepting new connections
    private ServerSocket ss;

    //HashMap that contains users with their nicknames as keys;
    private HashMap<String, User> clients;


    // Constructor
    public Server() {
        // Initiate list of clients
        clients = new HashMap<>();
    }

    private void listen(int port) throws IOException {
        // Create the ServerSocket
        ss = new ServerSocket(port);

        // Tell the world we're ready to go
        System.out.println("Listening on " + ss);

        // Keep accepting connections forever
        while (true) {
            // Grab the next incoming connection
            Socket client = ss.accept();
            // Tell the world we've got it
            System.out.println("Connection from " + client);

            //set a nickname for the new user
            String nickname;
            do {
                nickname = ("user" + (int)(Math.random() * 1001));
            } while (clients.containsKey(nickname));

            //create new user
            User newUser = new User(client, nickname);

            //store new user
            clients.put(nickname, newUser);

        }

    }

    // Main routine
    // Usage: java Server <port>
    public static void main(String[] args) {
        try {
            // Get the port # from the command line
            if(args.length != 1){
                System.out.println("Improper declaration!");
                System.out.println("Proper usage: java Server <port>");
                return;
            }
            int port = Integer.parseInt(args[0]);  
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Server <port>");
            return;
        } catch (NumberFormatException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Server <port>");
            return;
        }

        // Create a Server object, which will automatically begin
        // accepting connections.
        try {
            new Server().listen(port);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

Class User {
    private Socket client;
    private String nickname;

    public User(Socket client, String nickname) {
        this.client = client;
        this.nickname = nickname;
    }
}
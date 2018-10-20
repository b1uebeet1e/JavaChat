import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Server
 */
public class Server {
    // The ServerSocket we'll use for accepting new connections
    private ServerSocket server;

    // HashMap that contains users with their nicknames as keys;
    private HashMap<String, User> clients;

    // Constructor
    public Server() {
        // Initiate list of clients
        clients = new HashMap<>();
    }

    private void listen(int port) throws IOException {
        // Create the ServerSocket
        server = new ServerSocket(port);

        // Tell the world we're ready to go
        System.out.println("Listening on " + server);

        // Keep accepting connections forever
        while (true) {
            // Grab the next incoming connection
            Socket client = server.accept();
            // Tell the world we've got it
            System.out.println("Connection from " + client);

            // set a nickname for the new user
            String nickname;
            do {
                nickname = ("user" + (int) (Math.random() * 1001));
            } while (clients.containsKey(nickname));

            // announce the nickname
            System.out.println("nickname was set to " + nickname);

            // create new user
            User newUser = new User(client, nickname);

            // store new user
            clients.put(nickname, newUser);

        }

    }

    public boolean doesMyNicknameExist(String nickname) {
        return clients.containsKey(nickname);
    }

    public boolean updateUserNickname(String old_nickname, String new_nickname) {
        boolean success;
        synchronized (clients) {
            if (doesMyNicknameExists(new_nickname)) {
                success = false;
            } else {
                // Tell the world
                System.out.println("Renaming '" + old_nickname + "' to '" + new_nickname + "'");

                // Replace User key on HashMap
                clients.put(new_nickname, clients.remove(old_nickname));

                // Update user Nickname
                clients.get(new_nickname).changeNickname(new_nickname);

                success = true;
            }
        }

        return success;
    }

    // Main routine
    // Usage: java Server <port>
    public static void main(String[] args) {
        try {
            // Get the port # from the command line
            if (args.length != 1) {
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

class UserHandler extends Thread {
    // The Server that spawned us
    private Server server;

    // The user we are handing
    private User user;

    // Constructor
    public UserHandler(Server server, User user) {
        // Save the parameters
        this.server = server;
        this.user = user;

        // announce online users
        this.server.broadcastOnlineUsers();

        // Start up the thread
        start();
    }

    public void run() {
        try {
            // Create a DataInputStream for communication; the client
            // is using a DataOutputStream to write to us
            DataInputStream din = new DataInputStream(user.getInputStream());

            // Over and over, forever ...
            while (true) {
                // ... read the next message ...
                String message = din.readUTF();

                // ... check if nickname change request ...
                if (message.split(" ")[0].equals("#change_my_nickname_to#")) {

                }

                // ... clean the message ...
                message = cleanMessage(message);

                // ... tell the world ...
                System.out.println("Sending " + message);

                // ... and have the server send it to all clients
                server.broadcastToAll(message);
            }
        } catch (EOFException e) {
            // No error message needed
        } catch (IOException e) {
            // Error message needed
            e.printStackTrace();
        } finally {
            // The connection is closed for one reason or another,
            // so have the server dealing with it
            server.removeConnection(user);
        }
    }

    // Clean contents of the message from offensive
    private String cleanMessage(String message) {
        try {
            URL url = new URL(
                    "https://raw.githubusercontent.com/BlueBeetle97/JavaChat/master/banned_words.txt?token=AWL8DgsfOvf4sn2MlIcKxVBBHIBYWrZxks5b0-UdwA%3D%3D");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                message = message.replace(str, "<family friendly content>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}

class User {
    private Socket client;
    private String nickname;
    private OutputStream output;
    private InputStream input;

    public User(Socket client, String nickname) {
        this.client = client;
        this.nickname = nickname;
        this.output = client.getOutputStream();
        this.input = client.getInputStream();
    }

    public PrintStream getOutStream() {
        return this.input;
    }

    public InputStream getInputStream() {
        return this.output;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Socket getSocket() {
        return this.client;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
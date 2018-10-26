import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Server
 */
public class Server {
    // The ServerSocket we'll use for accepting new connections
    private ServerSocket server;

    // The port number our server will be listening
    private int port;

    // HashMap that contains users with their nicknames as keys;
    private HashMap<String, User> clients;

    // Constructor
    public Server(int port) throws IOException {
        // Initiate list of clients
        clients = new HashMap<>();
        this.port = port;
    }

    private void run() throws IOException {
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
            } while (doesMyNicknameExist(nickname));

            // announce the nickname
            System.out.println("nickname was set to " + nickname);

            // create new user
            User newUser = new User(client, nickname);

            // store new user
            clients.put(nickname, newUser);

            // inform new user about his nickname
            sendToUser("#nickname# " + nickname, newUser);

            // create a new thread for newUser incoming messages handling
            new Thread(new UserHandler(this, newUser)).start();
        }

    }

    // Ban a specific user for a period of time
    public void banUser(User client) {
        // TODO: change this to actual ban...
        sendToUser("#notify# please be more polite or you 'll get banned...", client);
    }

    // Sent a message to a specific User
    public void sendToUser(String message, User client) {
        client.getOutStream().println("#private# " + message);
    }

    // Broadcast messages to every User
    public void broadcastToAll(String message) {
        for (User client : this.clients.values()) {
            client.getOutStream().println("#group# " + message);
        }
    }

    // Broardcast messages from a User to every User
    public void broadcastToAll(String message, String nickname) {
        broadcastToAll("@" + nickname + ": " + message);
    }

    // Broadcast the set of client nicknames all Users
    public void broadcastOnlineUsers() {
        for (User client : this.clients.values()) {
            String message = "#online_users#";
            for (String nickname : this.clients.keySet()) {
                message += " ";
                message += nickname;
            }
            client.getOutStream().println(message);
        }
    }

    // Remove a socket, and it's corresponding User and nickname
    // from the HashMap. This is usually called by a connection thread that has
    // discovered that the connection to the client is dead.
    public void removeConnection(User user) {
        // Synchronize so we don't mess up broadcastToAll() or updateUserNickname
        // while it walks down the HashMap of all the clients
        synchronized (clients) {
            // Tell the world
            System.out.print("Removing connection to " + user.getSocket());
            System.out.println(" with nickname '" + user.getNickname() + "'");

            // Remove user from HashMap
            clients.remove(user.getNickname());

            // Make sure the connection is closed
            try {
                user.getSocket().close();
            } catch (IOException e) {
                System.out.println("Error closing " + user.getSocket());
                e.printStackTrace();
            }
        }
    }

    // Check if a nickname already exists
    public boolean doesMyNicknameExist(String nickname) {
        return clients.containsKey(nickname);
    }

    // Update a user's nickname to a new one, if possible
    public boolean updateUserNickname(String old_nickname, String new_nickname) {
        boolean success;

        // Syncronize to avoid parallel crossing of the clients Hashmap
        synchronized (clients) {
            if (doesMyNicknameExist(new_nickname)) {
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
    public static void main(String[] args) throws Exception {
        int port;
        try {
            // Get the port # from the command line
            if (args.length != 1) {
                System.out.println("Improper declaration!");
                System.out.println("Proper usage: java Server <port>");
                return;
            }
            port = Integer.parseInt(args[0]);
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
        new Server(port).run();
    }
}

class UserHandler implements Runnable {
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
    }

    public void run() {
        String message;

        // Create Scanner for communication; the client
        // is using a PrintStream to write to us
        Scanner in = new Scanner(this.user.getInputStream());

        // Until there is nothing left ...
        while (in.hasNextLine()) {
            // ... read the next message ...
            message = in.nextLine();

            // ... check if nickname change request ...
            if (message.split(" ")[0].equals("#change_my_nickname_to#")) {
                if (message.split(" ").length > 2) {
                    this.server.sendToUser("#error# name cannot contain spaces!!", this.user);
                }

                else {
                    String new_nickname = message.replace("#change_my_nickname_to# ", "");
                    String old_nickname = this.user.getNickname();

                    if (!new_nickname.equals(cleanMessage(new_nickname, this.user))) {
                        this.server.sendToUser("#error# name cannot contain offensive language!!", this.user);
                    }

                    else if (this.server.updateUserNickname(old_nickname, new_nickname)) {
                        this.server.sendToUser("#nickname# " + new_nickname, this.user);
                        this.server.broadcastOnlineUsers();
                        this.server.broadcastToAll(
                                "#notify# '" + old_nickname + "' changed its nickname to '" + new_nickname + "'");
                    }
                }
            }

            else if (message.split(" ")[0].equals("#terminate_connection#")) {
                if (message.split(" ").length > 1) {
                    System.out.println(this.user.getSocket() + " with nickname " + this.user.getNickname()
                            + "sent unexpected request!!");
                }

                else {
                    break;
                }
            }

            else {

                // ... clean the message ...
                message = cleanMessage(message, this.user);

                // ... check if user gets banned ...
                if (this.user.getBanCounter() > 3) {
                    this.server.banUser(this.user);
                }

                // ... tell the world ...
                System.out.println("User '" + this.user.getNickname() + "' sent: " + message);

                // ... and have the server send it to all clients
                this.server.broadcastToAll(message, this.user.getNickname());
            }
        }

        // end of Thread
        this.server.removeConnection(user);
        this.server.broadcastOnlineUsers();
        in.close();
    }

    // Clean contents of the message from offensive
    private String cleanMessage(String message, User user) {
        boolean trigger = false;
        try (BufferedReader in = new BufferedReader(new FileReader("banned_words.txt"))) {
            String str;
            while ((str = in.readLine()) != null) {
                if (message.matches("(?i)\\b" + str + "\\b(?i)")) {
                    trigger = true;
                }
                message = message.replaceAll(("(?i)\\b" + str + "\\b(?i)"), "<family friendly content>");
            }

            if (trigger) {
                user.updateBanCounter();
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
    private PrintStream output;
    private InputStream input;
    private int ban_counter;

    public User(Socket client, String nickname) throws IOException {
        this.client = client;
        this.nickname = nickname;
        this.output = new PrintStream(client.getOutputStream());
        this.input = client.getInputStream();
        this.ban_counter = 0;
    }

    public int getBanCounter() {
        return this.ban_counter;
    }

    public PrintStream getOutStream() {
        return this.output;
    }

    public InputStream getInputStream() {
        return this.input;
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

    public void updateBanCounter() {
        this.ban_counter++;
    }
}

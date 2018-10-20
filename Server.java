import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Server
 */
public class Server {
    // The ServerSocket we'll use for accepting new connections
    private ServerSocket server;

    // HashMap that contains users with their nicknames as keys;
    private HashMap<String, User> clients;

    // Constructor
    public Server(int port) throws IOException {
        // Initiate list of clients
        clients = new HashMap<>();
        listen(port);
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
            } while (doesMyNicknameExist(nickname));

            // announce the nickname
            System.out.println("nickname was set to " + nickname);

            // create new user
            User newUser = new User(client, nickname);

            // store new user
            clients.put(nickname, newUser);

        }

    }

    // Ban a specific user for a period of time
    public void banUser(User client) {

    }

    // Sent a message to a specific User
    public void sendToUser(String message, User client) {
        client.getOutStream().println("#private# " + message);
    }

    // Broadcast messages to every User
    public void broadcastToAll(String message) {
        for (User client : this.clients.values()) {
            client.getOutStream().println("#message# " + message);
        }
    }

    // Broardcast messages from a User to every User
    public void broadcastToAll(String message, String nickname) {
        broadcastToAll("@" + nickname + ": " + message);
    }

    // Remove a socket, and it's corresponding User and nickname
    // from the HashMap. This is usually called by a connection thread that has
    // discovered that the connection to the client is dead.
    public void removeConnection(User user) {
        // Synchronize so we don't mess up broadcastToAll() or updateUserNickname
        // while it walks down the HashMap of all the clients
        synchronized (clients) {
            // Tell the world
            System.out.println("Removing connection to " + user.getSocket());
            System.out.println("With nickname '" + user.getNickname() + "'");

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

    // Broadcast the set of client nicknames all Users
    public void broadcastOnlineUsers() {
        for (User client : this.clients.values()) {
            client.getOutStream().println(this.clients.keySet());
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
        new Server(port);
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
            // Use a DataInputStream for communication; the client
            // is using a DataOutputStream to write to us
            DataInputStream din = user.getInputStream();

            // Over and over, forever ...
            while (true) {
                // ... read the next message ...
                String message = din.readUTF();

                // ... check if nickname change request ...
                if (message.split(" ")[0].equals("#change_my_nickname_to#")) {
                    if (message.split(" ").length > 2) {
                        server.sendToUser("#error# name cannot contain spaces!!", user);
                        continue;
                    }

                    String new_nickname = message.replace("#change_my_nickname_to# ", "");
                    String old_nickname = user.getNickname();

                    if (!new_nickname.equals(cleanMessage(new_nickname, user))) {
                        server.sendToUser("#error# name cannot contain offensive language!!", user);
                        continue;
                    }

                    if (server.updateUserNickname(old_nickname, new_nickname)) {
                        server.broadcastToAll(
                                "#notify# '" + old_nickname + "' changed its nickname to '" + new_nickname + "'");
                        continue;
                    }
                }

                // ... clean the message ...
                message = cleanMessage(message, user);

                // ... check if user gets banned ...
                if (user.getBanCounter() > 3) {
                    server.banUser(user);
                }

                // ... tell the world ...
                System.out.println("Sending " + message);

                // ... and have the server send it to all clients
                server.broadcastToAll(message, user.getNickname());
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
    private String cleanMessage(String message, User user) {
        boolean trigger = false;
        try (BufferedReader in = new BufferedReader(new FileReader("banned_words.txt"))) {
            String str;
            while ((str = in.readLine()) != null) {
                if (message.contains(str)) {
                    trigger = true;
                }
                message = message.replace(str, "<family friendly content>");
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
    private DataInputStream input;
    private int ban_counter;

    public User(Socket client, String nickname) throws IOException {
        this.client = client;
        this.nickname = nickname;
        this.output = new PrintStream(client.getOutputStream());
        this.input = new DataInputStream(client.getInputStream());
        this.ban_counter = 0;
    }

    public int getBanCounter() {
        return this.ban_counter;
    }

    public PrintStream getOutStream() {
        return this.output;
    }

    public DataInputStream getInputStream() {
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
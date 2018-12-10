import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Server
 */
public class Server {
    // The ServerSocket we'll use for accepting new connections
    private ServerSocket unsecure_server;
    private SSLServerSocket ssl_server;

    // The port number our server will be listening
    private int port, ssl_port;

    // The ban timer in milliseconds
    private long ban_timer;

    // Hashmap that contains banned IPs as keys and the time they were banned as
    // values
    private HashMap<String, Long> blacklist;

    // HashMap that contains users with their nicknames as keys;
    private HashMap<String, User> clients, ssl_clients, ssl_unsecure_clients;

    // Constructor
    public Server(int port, int ssl_port, long ban_timer) {
        // Initiate list of clients
        clients = new HashMap<>();
        ssl_clients = new HashMap<>();
        ssl_unsecure_clients = new HashMap<>();

        blacklist = new HashMap<>();

        this.port = port;
        this.ssl_port = ssl_port;
        this.ban_timer = ban_timer;
    }

    public void start() throws IOException {
        runUnsecure();
        runSSL();
    }

    // start unsecure channel
    private void runUnsecure() throws IOException {
        // Create the ServerSocket
        unsecure_server = new ServerSocket(port);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    listen(unsecure_server, false);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }).start();
    }

    // start secure channel
    private void runSSL() throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "ServerKeyStore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "ServerKeyStore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        // Create the ServerSocket
        ssl_server = (SSLServerSocket) ((SSLServerSocketFactory) SSLServerSocketFactory.getDefault())
                .createServerSocket(ssl_port);

        // Check certificate
        ssl_server.setNeedClientAuth(true);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    listen(ssl_server, true);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }).start();
    }

    private void listen(ServerSocket server, boolean ssl) throws IOException {
        // Tell the world we're ready to go
        System.out.println("Listening on " + server);

        // Keep accepting connections forever
        while (true) {
            // Grab the next incoming connection
            Socket client = server.accept();

            // Check if IP is in the blacklist
            if (blacklist.containsKey(client.getInetAddress().toString())) {
                if ((blacklist.get(client.getInetAddress().toString()) - System.currentTimeMillis()) > 0) {
                    System.out.println("IP '" + client.getInetAddress() + "' is banned");
                    new PrintStream(client.getOutputStream()).println("#private# #banned#");
                } else {
                    blacklist.remove(client.getInetAddress().toString());
                }
            }

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
            User newUser = new User(client, nickname, ssl);

            // store new user
            if (!ssl) {
                clients.put(nickname, newUser);
            } else {
                ssl_clients.put(nickname, newUser);
            }

            // inform new user about his nickname
            sendToUser("#nickname# " + nickname, newUser);

            // create a new thread for newUser incoming messages handling
            new Thread(new UserHandler(this, newUser)).start();
        }

    }

    // Ban a specific user for a period of time
    public void banUser(User client) {
        blacklist.put(client.getSocket().getInetAddress().toString(), System.currentTimeMillis() + ban_timer);
        System.out.println("User '" + client.getNickname() + "' got banned");
        broadcastToAll("#notify# user '" + client.getNickname() + "' banned for use of improper language",
                client.isSSL());
        sendToUser("#banned#", client);
    }

    // Sent a message to a specific User
    public void sendToUser(String message, User client) {
        client.getOutStream().println("#private# " + message);
    }

    // Broadcast messages to every unsecure channel User
    public void broadcastToAllUnsecure(String message) {
        for (User client : this.clients.values()) {
            client.getOutStream().println("#group# " + message);
        }

        for (User client : this.ssl_unsecure_clients.values()) {
            client.getOutStream().println("#group# " + message);
        }
    }

    // Broadcast messages to every secure channel User
    public void broadcastToAllSSL(String message) {
        for (User client : this.ssl_clients.values()) {
            client.getOutStream().println("#group# " + message);
        }
    }

    // Broadcast messages to every User on the same channel
    public void broadcastToAll(String message, boolean ssl) {
        if (ssl) {
            broadcastToAllSSL(message);
        } else {
            broadcastToAllUnsecure(message);
        }
    }

    // Broardcast messages from a User to every User on the same channel
    public void broadcastToAll(String message, String nickname, boolean ssl) {
        broadcastToAll("@" + nickname + ": " + message, ssl);
    }

    // Broadcast the set of client nicknames all Users of unsecure channel
    public void broadcastOnlineUnsecureUsers() {
        for (User client : this.clients.values()) {
            String message = "#online_users#";
            for (String nickname : this.clients.keySet()) {
                message += " ";
                message += nickname;
            }

            for (String nickname : this.ssl_unsecure_clients.keySet()) {
                message += " ";
                message += nickname;
            }
            client.getOutStream().println(message);
        }

        for (User client : this.ssl_unsecure_clients.values()) {
            String message = "#online_users#";
            for (String nickname : this.clients.keySet()) {
                message += " ";
                message += nickname;
            }

            for (String nickname : this.ssl_unsecure_clients.keySet()) {
                message += " ";
                message += nickname;
            }
            client.getOutStream().println(message);
        }
    }

    // Broadcast the set of client nicknames all Users of secure channel
    public void broadcastOnlineSSLUsers() {
        for (User client : this.ssl_clients.values()) {
            String message = "#online_users#";
            for (String nickname : this.ssl_clients.keySet()) {
                message += " ";
                message += nickname;
            }
            client.getOutStream().println(message);
        }
    }

    // Broadcast the set of client nicknames all Users the same channel
    public void broadcastOnlineUsers(boolean ssl) {
        if (ssl) {
            broadcastOnlineSSLUsers();
        } else {
            broadcastOnlineUnsecureUsers();
        }
    }

    // Remove a socket, and it's corresponding User and nickname
    // from the HashMap. This is usually called by a connection thread that has
    // discovered that the connection to the client is dead.
    public void removeConnection(User user) {

        // Tell the world
        System.out.print("Removing connection to " + user.getSocket());
        System.out.println(" with nickname '" + user.getNickname() + "'");

        if (clients.containsKey(user.getNickname())) {

            // Synchronize so we don't mess up broadcastToAll() or updateUserNickname
            // while it walks down the HashMap of all the clients
            synchronized (clients) {

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
        } else if (ssl_unsecure_clients.containsKey(user.getNickname())) {

            // Synchronize so we don't mess up broadcastToAll() or updateUserNickname
            // while it walks down the HashMap of all the clients
            synchronized (ssl_unsecure_clients) {

                // Remove user from HashMap
                ssl_unsecure_clients.remove(user.getNickname());

                // Make sure the connection is closed
                try {
                    user.getSocket().close();
                } catch (IOException e) {
                    System.out.println("Error closing " + user.getSocket());
                    e.printStackTrace();
                }
            }
        } else if (ssl_clients.containsKey(user.getNickname())) {

            // Synchronize so we don't mess up broadcastToAll() or updateUserNickname
            // while it walks down the HashMap of all the clients
            synchronized (ssl_clients) {

                // Remove user from HashMap
                ssl_clients.remove(user.getNickname());

                // Make sure the connection is closed
                try {
                    user.getSocket().close();
                } catch (IOException e) {
                    System.out.println("Error closing " + user.getSocket());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Error closing " + user.getSocket());
            System.out.println("User not enlisted!!");
        }
    }

    // Check if a nickname already exists
    public boolean doesMyNicknameExist(String nickname) {
        if (clients.containsKey(nickname)) {
            return true;
        } else if (ssl_clients.containsKey(nickname)) {
            return true;
        } else if (ssl_unsecure_clients.containsKey(nickname)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateUnsecureUserNickname(String old_nickname, String new_nickname) {
        boolean success = false;

        if (doesMyNicknameExist(new_nickname)) {
            return success;
        }

        if (clients.containsKey(old_nickname)) {

            // Syncronize to avoid parallel crossing of the clients Hashmap
            synchronized (clients) {

                // Tell the world
                System.out.println("Renaming '" + old_nickname + "' to '" + new_nickname + "'");

                // Replace User key on HashMap
                clients.put(new_nickname, clients.remove(old_nickname));

                // Update user Nickname
                clients.get(new_nickname).changeNickname(new_nickname);

                success = true;
            }
        } else if (ssl_unsecure_clients.containsKey(old_nickname)) {

            // Syncronize to avoid parallel crossing of the clients Hashmap
            synchronized (ssl_unsecure_clients) {

                // Tell the world
                System.out.println("Renaming '" + old_nickname + "' to '" + new_nickname + "'");

                // Replace User key on HashMap
                ssl_unsecure_clients.put(new_nickname, ssl_unsecure_clients.remove(old_nickname));

                // Update user Nickname
                ssl_unsecure_clients.get(new_nickname).changeNickname(new_nickname);

                success = true;
            }
        }

        return success;
    }

    public boolean updateSSLUserNickname(String old_nickname, String new_nickname) {
        boolean success = false;

        if (doesMyNicknameExist(new_nickname)) {
            return success;
        }

        // Syncronize to avoid parallel crossing of the clients Hashmap
        synchronized (ssl_clients) {

            // Tell the world
            System.out.println("Renaming '" + old_nickname + "' to '" + new_nickname + "'");

            // Replace User key on HashMap
            ssl_clients.put(new_nickname, ssl_clients.remove(old_nickname));

            // Update user Nickname
            ssl_clients.get(new_nickname).changeNickname(new_nickname);

            success = true;
        }

        return success;
    }

    // Update a user's nickname to a new one, if possible
    public boolean updateUserNickname(String old_nickname, String new_nickname, boolean ssl) {
        if (ssl) {
            return updateSSLUserNickname(old_nickname, new_nickname);
        } else {
            return updateUnsecureUserNickname(old_nickname, new_nickname);
        }
    }

    // Swap from Secure to Unsecure channel and reverse
    public boolean swapChannel(User client) {
        boolean success = false;

        if (ssl_unsecure_clients.containsKey(client.getNickname())) {
            synchronized (ssl_unsecure_clients) {
                ssl_unsecure_clients.remove(client.getNickname());
            }

            synchronized (ssl_clients) {
                ssl_clients.put(client.getNickname(), client);
            }

            success = true;
        } else if (ssl_clients.containsKey(client.getNickname())) {
            synchronized (ssl_clients) {
                ssl_clients.remove(client.getNickname());
            }

            synchronized (ssl_unsecure_clients) {
                ssl_unsecure_clients.put(client.getNickname(), client);
            }

            success = true;
        }

        return success;
    }

    // Main routine
    // Usage: java Server <port>
    public static void main(String[] args) throws Exception {
        int port, ssl_port;
        long ban_timer = 600000;
        try {
            // Get the port # from the command line
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                ssl_port = Integer.parseInt(args[0]) + 1;
            }

            else if (args.length == 2) {
                port = Integer.parseInt(args[0]);
                ssl_port = Integer.parseInt(args[1]);
            }

            else if (args.length == 3) {
                port = Integer.parseInt(args[0]);
                ssl_port = Integer.parseInt(args[1]);
                ban_timer = Long.parseLong(args[2]);
                if (ban_timer <= 0) {
                    throw new NumberFormatException("ban timer cannot be less or equal to zero");
                }
            }

            else {
                System.out.println("Improper declaration!");
                System.out.println("Proper usage: java Server <port>");
                System.out.println("Alternative proper usage: java Server <port> <ssl port>");
                System.out.println("Alternative proper usage: java Server <port> <ssl port> <ban timer>");
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Server <port>");
            System.out.println("Alternative proper usage: java Server <port> <ssl port>");
            System.out.println("Alternative proper usage: java Server <port> <ssl port> <ban timer>");
            return;
        } catch (NumberFormatException e) {
            System.out.println("Improper declaration!");
            System.out.println("Proper usage: java Server <port>");
            System.out.println("Alternative proper usage: java Server <port> <ssl port>");
            System.out.println("Alternative proper usage: java Server <port> <ssl port> <ban timer>");
            return;
        }

        // Create a Server object, which will automatically begin
        // accepting connections.

        new Server(port, ssl_port, ban_timer).start();
    }
}
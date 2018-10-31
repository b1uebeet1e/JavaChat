import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

/**
 * InnerUserHandler
 */
public class UserHandler implements Runnable {
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
                if (message.split(" ").length != 2) {
                    this.server.sendToUser("#notify# name cannot contain spaces!!", this.user);
                }

                else {
                    String new_nickname = message.split(" ")[1];
                    String old_nickname = this.user.getNickname();

                    if (!new_nickname.equals(cleanMessage(new_nickname, this.user))) {
                        this.server.sendToUser("#notify# name cannot contain offensive language!!", this.user);
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
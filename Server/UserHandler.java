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

    // Trigger for improper language
    private boolean trigger;

    // Constructor
    public UserHandler(Server server, User user) {
        // Save the parameters
        this.server = server;
        this.user = user;

        // announce online users
        this.server.broadcastOnlineUsers(this.user.isSSL());
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

                    else if (this.server.updateUserNickname(old_nickname, new_nickname, this.user.isSSL())) {
                        this.server.sendToUser("#nickname# " + new_nickname, this.user);
                        this.server.broadcastOnlineUsers(this.user.isSSL());
                        this.server.broadcastToAll(
                                "#notify# '" + old_nickname + "' changed its nickname to '" + new_nickname + "'",
                                this.user.isSSL());
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

            else if (message.split(" ")[0].equals("#swap_connection#")) {
                if (message.split(" ").length > 1) {
                    System.out.println(this.user.getSocket() + " with nickname " + this.user.getNickname()
                            + "sent unexpected request!!");
                }

                else {
                    if (this.server.swapChannel(this.user)) {
                        this.user.switchChannel();
                        this.server.broadcastOnlineUsers(this.user.isSSL());
                        if (this.user.isSSL()) {
                            this.server.sendToUser("#notify# Swapped to Secure channel", this.user);
                        } else {
                            this.server.sendToUser("#notify# Swapped to Unsecure channel", this.user);
                        }
                    }

                    else {
                        this.server.sendToUser("#notify# An error occured while switching channels", this.user);
                    }
                }
            }

            else {

                // ... clean the message ...
                message = cleanMessage(message, this.user);

                // ... tell the world ...
                System.out.println("User '" + this.user.getNickname() + "' sent: " + message);

                // ... and have the server send it to all clients ...
                this.server.broadcastToAll(message, this.user.getNickname(), this.user.isSSL());

                // ... check if user gets banned ...
                if (this.user.getBanCounter() > 3) {
                    this.server.banUser(this.user);
                }

                // ... or give warning if needed
                else if (trigger) {
                    this.server.sendToUser("#notify# please be more polite or you 'll get banned...", this.user);
                }
            }
        }

        // end of Thread
        this.server.removeConnection(user);
        this.server.broadcastOnlineUsers(this.user.isSSL());
        in.close();
    }

    // Clean contents of the message from offensive
    private String cleanMessage(String message, User user) {
        trigger = false;
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
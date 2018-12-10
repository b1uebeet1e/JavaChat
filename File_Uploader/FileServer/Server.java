import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server
 */
public class Server {

    private int port;

    private ServerSocket server;

    private boolean busy;

    private int size_limit;

    public Server(int port, int size_limit) {
        this.port = port;
        this.size_limit = size_limit;
    }

    public void run() throws IOException {
        server = new ServerSocket(port);

        System.out.println("Listening on " + server);

        while (true) {
            Socket client = server.accept();

            System.out.println("Connection from " + client);

            if (!busy) {
                busy = true;
                save(client);
            } else {
                client.close();
            }
        }
    }

    private void save(Socket client) throws IOException {
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());

        String name = input.readUTF();
        int filesize = input.readInt();

        if (filesize <= size_limit) {

            output.writeBoolean(true);
            File file = new File(name);

            while (file.exists() || file.isDirectory()) {
                name = "(1)" + name;
                file = new File(name);
            }

            FileOutputStream file_output = new FileOutputStream(file);
            byte[] buffer = new byte[5000];

            int read = 0;
            int totalRead = 0;
            int remaining = filesize;
            while ((read = input.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes.");
                file_output.write(buffer, 0, read);
            }
            output.writeBoolean(true);
            file_output.close();

        }

        else {
            output.writeBoolean(false);
        }
        input.close();
        busy = false;
    }

    public static void main(String[] args) throws Exception {
        new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1])).run();
    }
}
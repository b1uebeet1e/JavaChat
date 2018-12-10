import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client
 */
public class Client {
    private String host;
    private int port;
    private Socket server;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException {
        server = new Socket(host, port);
        send("text.txt");
    }

    private void send(String file_name) throws IOException {
        DataOutputStream output = new DataOutputStream(server.getOutputStream());

        output.writeUTF(file_name);
        File file = new File(file_name);
        output.writeInt((int)file.length());

        FileInputStream input = new FileInputStream(file);
        byte[] buffer = new byte[5000];

        while (input.read(buffer) > 0) {
            output.write(buffer);
        }

        input.close();
        output.close();
    }

    public static void main(String[] args) throws Exception {
        new Client(args[0], Integer.parseInt(args[1])).run();
    }

}
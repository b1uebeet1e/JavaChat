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

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        server = new ServerSocket(port);

        System.out.println("Listening on " + server);

        Socket client = server.accept();

        System.out.println("Connection from " + client);

        save(client);
    }

    private void save(Socket client) throws IOException {
        DataInputStream input = new DataInputStream(client.getInputStream());
        DataOutputStream output = new DataOutputStream(client.getOutputStream());

        String name = input.readUTF();
        File file = new File(name);
        while (file.exists() || file.isDirectory()) {
            name = "(1)" + name;
            file = new File(name);
        }

        FileOutputStream file_output = new FileOutputStream(file);
        byte[] buffer = new byte[5000];

        int filesize = input.readInt();
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = input.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            file_output.write(buffer, 0, read);
            double persentage = totalRead / (double) filesize;
            if ((int)persentage % 2 == 0) {
                output.writeDouble(persentage);
            }
        }
        output.writeDouble(2);

        try {
            Thread.sleep(5000);

        } catch (Exception e) {
            // TODO

        }
                    
        input.close();
        file_output.close();
    }

    public static void main(String[] args) throws Exception {
        new Server(Integer.parseInt(args[0])).run();
    }
}
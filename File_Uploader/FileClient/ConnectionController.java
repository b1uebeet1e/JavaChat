import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ConnectionController
 */
public class ConnectionController {

    private String host;
    private int port;
    private Socket server;
    private OnionProxyManager tor;
    private DataInputStream input;
    private DataOutputStream output;

    public ConnectionController(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException {
        tor = new OnionProxyManager();
        tor.start();
        Thread.sleep(5000); // Give it some time to create a circuit
        server = tor.openSocket(host, port);

        // server = new Socket();
        // server.connect(new InetSocketAddress(host, port));
        input = new DataInputStream(server.getInputStream());
        output = new DataOutputStream(server.getOutputStream());
    }

    public void send(String file_path) throws IOException {
        File file = new File(file_path);

        output.writeUTF(file.getName());
        output.writeInt((int) file.length());

        if (!input.readBoolean()) {
            throw new IOException("File choosen is too big!!");
        }

        FileInputStream file_input = new FileInputStream(file);
        byte[] buffer = new byte[5000];

        while (file_input.read(buffer) > 0) {
            output.write(buffer);
        }

        file_input.close();
    }

    public DataInputStream getInput() {
        return input;
    }

    public void close() throws IOException {
        input.close();
        output.close();
        server.close();
        if (tor.isProcessAlive()) {
            tor.stop();
        }
    }

    public boolean isAlive() {
        return !server.isClosed();
    }
}
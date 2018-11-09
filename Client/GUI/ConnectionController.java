import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * ConnectionController
 */
public class ConnectionController {

    private Socket server;
    private PrintStream output;
    private BufferedReader input;
    private static OnionProxyManager tor;

    public ConnectionController(String host, int port, int arg)
            throws IOException, SocketTimeoutException, UnknownHostException, InterruptedException {

        if (arg == 0) {
            server = new Socket();
            server.connect(new InetSocketAddress(host, port));
        }

        else if (arg == 2) {
            OnionProxyManager.start();
            Thread.sleep(5000); // Give it some time to create a circuit
            server = OnionProxyManager.openSocket(host, port);
        }

        else {
            throw new IOException("invalid argument");
        }

        output = new PrintStream(server.getOutputStream());
        input = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    public void sendMessage(String msg) throws IOException {
        output.println(msg);
    }

    public boolean isConnected() {
        if (server != null && server.isConnected()) {
            return true;
        }
        return false;
    }

    public BufferedReader getInput() {
        return input;
    }

    public void close() throws IOException {
        output.close();
        input.close();
        server.close();
        tor.stop();
    }
}
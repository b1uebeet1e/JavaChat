import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * ConnectionController
 */
public class ConnectionController {

    private Socket server;
    private PrintStream output;
    private BufferedReader input;
    private OnionProxyManager tor;

    public ConnectionController(String host, int port, int arg)
            throws IOException, SocketTimeoutException, UnknownHostException, InterruptedException, SSLException {

        tor = new OnionProxyManager();

        if (arg == 0) {
            server = new Socket();
            server.connect(new InetSocketAddress(host, port));
        }

        else if (arg == 1) {
            System.setProperty("javax.net.ssl.trustStore", "ClientKeyStore.jks");
            SSLSocket ssl_socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(host,
                    port);
            server = ssl_socket;
        }

        else if (arg == 2) {
            tor.start();
            Thread.sleep(5000); // Give it some time to create a circuit
            server = tor.openSocket(host, port);
        }

        else if (arg == 3) {
            tor.start();
            Thread.sleep(5000); // Give it some time to create a circuit
            System.setProperty("javax.net.ssl.trustStore", "ClientKeyStore.jks");
            server = tor.openSSLSocket(host, port);
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
        if (tor.isProcessAlive()) {
            tor.stop();
        }
    }
}
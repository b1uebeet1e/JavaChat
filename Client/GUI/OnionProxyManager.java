import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * OnionProxyManager
 **/
public class OnionProxyManager {

	// Tor Process
	private Process tor_process;

	// Proxy
	private Proxy proxy;
	private String proxy_host;
	private int proxy_port;

	// Constructor
	public OnionProxyManager() {
		proxy_host = "127.0.0.1";
		proxy_port = 9050;
	}

	// Spawns the Tor proxy process.
	public void start() throws IOException {
		if (System.getProperty("os.name").indexOf("win") >= 0) {
			tor_process = Runtime.getRuntime().exec("win/tor.exe");
		}

		else if (System.getProperty("os.name").indexOf("nux") >= 0
				|| System.getProperty("os.name").indexOf("nix") >= 0) {
			tor_process = Runtime.getRuntime().exec("lin64/tor");
		}

		else if (System.getProperty("os.name").indexOf("mac") >= 0) {
			tor_process = Runtime.getRuntime().exec("osx/tor.real");
		}

		else {
			throw new IOException("invalid os: " + System.getProperty("os.name"));
		}

		proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy_host, proxy_port));
	}

	// Returns the proxy object.
	public Proxy getProxy() {
		return proxy;
	}

	// Opens a plain-text socket connection over the Tor network
	public Socket openSocket(String address, int port) throws IOException {
		Socket socket = new Socket(proxy);
		InetSocketAddress addr = InetSocketAddress.createUnresolved(address, port);
		socket.connect(addr);
		return socket;
	}

	// Opens a secure SSL socket connection over the Tor network.
	public SSLSocket openSSLSocket(String address, int port) throws IOException {
		Socket socket = openSocket(address, port);

		SSLSocket ssl_socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(socket,
				proxy_host, proxy_port, true);

		return ssl_socket;
	}

	// Is the Tor proxy process alive?
	public boolean isProcessAlive() {
		if (tor_process == null) {
			return false;
		}
		return tor_process.isAlive();
	}

	// Stops the Tor proxy process.
	public void stop() {
		tor_process.destroy();
	}
}

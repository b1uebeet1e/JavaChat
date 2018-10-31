import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

class User {
    private Socket client;
    private String nickname;
    private PrintStream output;
    private InputStream input;
    private int ban_counter;

    public User(Socket client, String nickname) throws IOException {
        this.client = client;
        this.nickname = nickname;
        this.output = new PrintStream(client.getOutputStream());
        this.input = client.getInputStream();
        this.ban_counter = 0;
    }

    public int getBanCounter() {
        return this.ban_counter;
    }

    public PrintStream getOutStream() {
        return this.output;
    }

    public InputStream getInputStream() {
        return this.input;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Socket getSocket() {
        return this.client;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateBanCounter() {
        this.ban_counter++;
    }
}

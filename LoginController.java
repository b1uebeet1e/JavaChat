import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * LoginController
 */
public class LoginController implements Initializable {

    private ClientGUI controller;
    private ConnectionController connection;

    @FXML
    private TextField address, port, username;

    @FXML
    private PasswordField password;

    @FXML
    private Label error;

    @FXML
    private Button login;

    @FXML
    private void login() {
        int port_num;
        try {
            port_num = Integer.parseInt(port.getText());
            connection = new ConnectionController(address.getText(), port_num);
            controller.setClientStage(connection);
        } catch (NumberFormatException | NullPointerException e) {
            error.setText("Invalid port number!!");
            System.err.println(e.toString());
        } catch (SocketTimeoutException e) {
            error.setText("Connection timed out...");
            System.err.println(e.toString());
        } catch (UnknownHostException e) {
            error.setText("Host not found: " + e.getMessage());
            System.err.println(e.toString());
        } catch (IOException e) {
            error.setText("I/O Error: " + e.getMessage());
            System.err.println(e.toString());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setContoller(ClientGUI controller) {
        this.controller = controller;
    }
}
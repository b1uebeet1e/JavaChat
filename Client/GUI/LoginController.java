import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javax.net.ssl.SSLException;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
    private ComboBox<String> choice;

    @FXML
    private CheckBox anon, c_port;

    @FXML
    private void setPort() {
        if (c_port.selectedProperty().get()) {
            port.editableProperty().setValue(true);
        }

        else {
            port.editableProperty().setValue(false);
            onChoice();
        }
    }

    @FXML
    private void onChoice() {
        if (c_port.selectedProperty().get()) {
            return;
        }

        if (choice.getSelectionModel().getSelectedIndex() == 0) {
            port.setText("51234");
        }

        else if (choice.getSelectionModel().getSelectedIndex() == 1) {
            port.setText("51235");
        }
    }

    @FXML
    private void login() {
        error.setText("connecting...");

        try {
            int port_num = Integer.parseInt(port.getText());
            int arg = choice.getSelectionModel().getSelectedIndex();
            if (anon.selectedProperty().get()) {
                arg += 2;
            }
            connection = new ConnectionController(address.getText(), port_num, arg);
            if (arg == 0 || arg == 2) {
                controller.setClientStage(connection);
            } else {
                controller.setClientStage(connection, true);
            }
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
        } catch (InterruptedException e) {
            error.setText("Error: " + e.getMessage());
            System.err.println(e.toString());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choice.getItems().addAll("Public Unsecure Channel, PUC", "Public Secure Channel, PSC");
        choice.getSelectionModel().selectFirst();
    }

    public void setContoller(ClientGUI controller) {
        this.controller = controller;
    }

    public void setError(String str) {
        error.setText(str);
    }
}

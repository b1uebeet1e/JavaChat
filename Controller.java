import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXMLDocumentController
 */
public class Controller implements Initializable {

    @FXML
    private TextField username, address, port;

    @FXML
    private PasswordField password;

    @FXML
    private Label error;

    @FXML
    private TextArea message;

    @FXML
    private ScrollPane messages, users;

    @FXML
    private VBox userBox, messageBox;

    @FXML
    private Button login, send, nickname, disconnect;

    @FXML
    protected void actionHandler(ActionEvent event) throws Exception {
        if (event.getSource() == login) {
            Stage stage = (Stage) login.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
            stage.setTitle("Chat Client");
            stage.setScene(new Scene(root));
            stage.setMaxHeight(Double.MAX_VALUE);
            stage.setMaxWidth(Double.MAX_VALUE);
            stage.setWidth(600);
            stage.setHeight(400);
        }

        else if (event.getSource() == send) {
            Text text = new Text(message.getText());
            text.setFont(new Font("Roboto", 14));
            text.setWrappingWidth(messages.getWidth() - 25);
            messageBox.getChildren().add(text);
            message.setText("");
        }

        else if (event.getSource() == disconnect) {
            Stage stage = (Stage) disconnect.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            stage.setTitle("Chat Client Login");
            stage.setScene(new Scene(root));
            stage.setMaxHeight(300);
            stage.setMaxWidth(400);
        }

        else if (event.getSource() == nickname) {

        }
    }

    @FXML
    protected void keyHandler(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) {
            return;
        }

        if (event.isShiftDown()) {
            message.appendText("\n");
            return;
        }
        Text text = new Text(message.getText().substring(0, message.getText().length() - 1));
        text.setFont(new Font("Roboto", 14));
        text.setWrappingWidth(messages.getWidth() - 25);

        // TODO: send message
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

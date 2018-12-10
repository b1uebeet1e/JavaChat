import java.io.DataInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * GUIController
 */
public class GUIController implements Initializable {

    @FXML
    Button upload, select;

    @FXML
    CheckBox c_port;

    @FXML
    ImageView logo;

    @FXML
    Label message;

    @FXML
    TextField host, port, path;

    FileChooser fileChooser;

    FileClientGUI controller;

    ConnectionController connection;

    @FXML
    private void setPort() {
        if (c_port.selectedProperty().get()) {
            port.editableProperty().setValue(true);
        }

        else {
            port.editableProperty().setValue(false);
            port.setText("51234");
        }
    }

    @FXML
    private void selectFile() {
        try {
            path.setText(fileChooser.showOpenDialog(controller.getStage()).getPath());
            path.positionCaret(Integer.MAX_VALUE);
        } catch (NullPointerException e) {
            path.setText("no file path specified");
        }
    }

    @FXML
    private void upload() {
        message.setText("Connecting...");

        try {
            int port_num = Integer.parseInt(port.getText());
            if (path.getText().equals("no file path specified")) {
                throw new Exception("Please select file!!");
            }

            connection = new ConnectionController(host.getText(), port_num);
            connection.run();
            if (!connection.isAlive()) {
                throw new Exception("Server might be busy :(");
            }

            message.setText("Uploading file...");

            connection.send(path.getText());

            DataInputStream input = connection.getInput();

            if (input.readBoolean()) {
                message.setText("File successfully uploaded!");
            }
        } catch (Exception e) {
            message.setText(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
    }

    public void setContoller(FileClientGUI controller) {
        this.controller = controller;
    }
}
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * ClientController
 */
public class ClientController implements Initializable {

    private ClientGUI controller;

    private ConnectionController connection;

    private String my_nickname;

    private Font font;

    @FXML
    Button send, nickname, disconnect;

    @FXML
    ScrollPane messages, users;

    @FXML
    VBox messageBox, userBox;

    @FXML
    TextArea message;

    @FXML
    public void actionHandler(ActionEvent event) {
        if (event.getSource() == send) {
            try {
                connection.sendMessage(new String(message.getText().trim()));
            } catch (IOException e) {
                messageBox.getChildren().add(errorMessage("ERROR"));
            }
            message.setText("");
        }

        else if (event.getSource() == nickname) {

        }

        else if (event.getSource() == disconnect) {

        }
    }

    @FXML
    public void keyPressedHandler(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) {
            return;
        }

        if (event.isShiftDown()) {
            message.appendText("\n");
            return;
        }

        if (message.getText().trim().isEmpty()) {
            return;
        }

        try {
            connection.sendMessage(message.getText().trim());
        } catch (IOException e) {
            messageBox.getChildren().add(errorMessage("ERROR"));
        }
        message.setText("");
    }

    @FXML
    public void keyReleasedHandler(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) {
            return;
        }

        if (event.isShiftDown()) {
            return;
        }

        message.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        my_nickname = "";
        font = new Font("Roboto", 14);
        messageListener();
    }

    public void messageListener() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String msg;
                while (true) {
                    try {
                        msg = connection.getInput().readLine();
                    } catch (Exception e) {
                        System.err.println(e);
                        break;
                    }
                    if (msg.isEmpty()) {
                        continue;
                    }

                    if (msg.split(" ")[0].equals("#online_users#")) {
                        msg = msg.replaceFirst("#online_users# ", "");
                        showUsers(msg);
                        continue;
                    }

                    if (msg.split(" ")[0].equals("#private#")) {
                        msg = msg.replaceFirst("#private# ", "");
                        if (msg.split(" ")[0].equals("#nickname#")) {
                            my_nickname = msg.replaceFirst("#nickname# ", "");
                            continue;
                        }
                    }

                    else if (msg.split(" ")[0].equals("#group#")) {
                        msg = msg.replaceFirst("#group# ", "");
                    }

                    if (msg.split(" ")[0].equals("#notify#")) {
                        showNotification(msg.replaceFirst("#notify# ", ""));
                        continue;
                    }

                    showMessage(msg);
                }
            }
        }).start();
    }

    public void setContoller(ClientGUI controller) {
        this.controller = controller;
    }

    public void setConnection(ConnectionController connection) {
        this.connection = connection;
    }

    private Text textMessage(String str) {
        Text text = new Text(str);
        text.setFont(font);
        text.setWrappingWidth(messages.getWidth() - 25);
        return text;
    }

    private Text errorMessage(String str) {
        Text text = new Text(str);
        text.setFont(font);
        text.setFill(Color.RED);
        text.setWrappingWidth(messages.getWidth() - 25);
        return text;
    }

    private Text userText(String str) {
        Text text = new Text(str);
        text.setFont(font);
        return text;
    }

    private Text myUserText(String str) {
        Text text = new Text(str);
        text.setFont(font);
        text.setFill(Color.DARKCYAN);
        return text;
    }

    public void showUsers(String users) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                userBox.getChildren().clear();
                for (String str : users.split(" ")) {
                    if (str.equals(my_nickname)) {
                        str = str + "(me)";
                        userBox.getChildren().add(myUserText(str));
                        continue;
                    }
                    userBox.getChildren().add(userText(str));
                }
            }
        });
    }

    public void showNotification(String str) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                messageBox.getChildren().add(errorMessage(str.trim()));
            }
        });
    }

    public void showMessage(String str) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                messageBox.getChildren().add(textMessage(str.trim()));
            }
        });
    }
}
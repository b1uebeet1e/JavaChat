import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * ClientGUI
 */
public class ClientGUI extends Application {

    private Stage stage;
    private LoginController login;
    private ClientController client;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        setLoginStage();
        stage.show();
        stage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setLoginStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        login = loader.getController();
        login.setContoller(this);

        stage.setTitle("Chat Client Login");
        stage.setScene(new Scene(root));
        stage.setMinHeight(390);
        stage.setMinWidth(460);
        stage.setMaxHeight(390);
        stage.setMaxWidth(460);
    }

    public void setLoginStage(String error) throws IOException {
        setLoginStage();
        login.setError(error);
    }

    public void setClientStage(ConnectionController connection, boolean ssl) throws IOException {
        setClientStage(connection);
        client.setSwappable(ssl);
    }

    public void setClientStage(ConnectionController connection) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent root = loader.load();

        client = loader.getController();
        client.setContoller(this);
        client.setConnection(connection);

        stage.setMaxHeight(Double.MAX_VALUE);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setWidth(600);
        stage.setHeight(400);
        stage.setTitle("Chat Client");
        stage.setScene(new Scene(root));
    }
}
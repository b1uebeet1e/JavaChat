import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ClientGUI
 */
public class ClientGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        primaryStage.setTitle("Chat Client Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(400);
        primaryStage.setMaxHeight(300);
        primaryStage.setMaxWidth(400);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
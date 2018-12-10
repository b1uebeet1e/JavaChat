import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FileClientGUI
 */
public class FileClientGUI extends Application {

    private GUIController gui;

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("file_uploader.fxml"));
        Parent root = loader.load();

        gui = loader.getController();
        gui.setContoller(this);

        stage.setTitle("Anonynous File Uploader");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });
    }

    protected Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
import Util.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        primaryStage.setTitle("Multiple Database Utilization Tools - " + Utils  .getCurrentVersion());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

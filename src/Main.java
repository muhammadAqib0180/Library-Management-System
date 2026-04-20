import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import database.DatabaseHandler;
import database.DatabaseConnection;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize tables if they don't exist yet
        DatabaseHandler.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
        Parent root = loader.load();

        // Create scene and apply modern theme CSS
        Scene scene = new Scene(root);
        String cssResource = getClass().getResource("/modern-theme.css").toExternalForm();
        scene.getStylesheets().add(cssResource);

        // Branding: title and minimum size
        primaryStage.setTitle("Library Nexus — Sign In");
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(580);
        primaryStage.setScene(scene);

        // Load app icon if it exists in resources
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            if (!icon.isError())
                primaryStage.getIcons().add(icon);
        } catch (Exception ignored) {
        }

        // Close connection pool when window closes
        primaryStage.setOnCloseRequest(e -> DatabaseConnection.close());

        primaryStage.show();
    }

    public static void main(String[] args) {
        // Add shutdown hook to close connection pool
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::close));

        // This launches the 'start' method above
        launch(args);
    }
}
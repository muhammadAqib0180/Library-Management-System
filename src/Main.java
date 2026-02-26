import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DatabaseHandler;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Database Check (Runs when the window starts)
        File dbFile = new File("library.db");
        if (!dbFile.exists()) {
            System.out.println("No database found. Initializing...");
            DatabaseHandler.initializeDatabase();
        }

        // 2. Load the UI from your 'views' folder
        // Note: The path must match exactly where your .fxml is
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
        Parent root = loader.load();

        // 3. Set the Window Title and Size
        primaryStage.setTitle("Library Management System - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // This launches the 'start' method above
        launch(args);
    }
}
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DatabaseHandler;
import database.DatabaseConnection;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Supabase PostgreSQL tables if they don't exist yet
        DatabaseHandler.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Library Management System - Login");
        primaryStage.setScene(new Scene(root));
        
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
package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {
	
	private static Stage state;

	public static Stage getPrimaryStage() {
		return Main.state;
	}

	private void setPrimaryStage(Stage stage) {
		Main.state = stage;
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			setPrimaryStage(primaryStage);
			Parent root = FXMLLoader.load(getClass().getResource("/application/RootLayout.fxml"));
			primaryStage.setTitle("Server");
			primaryStage.setResizable(false);
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

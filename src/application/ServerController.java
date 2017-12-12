package application;

import java.lang.Thread.State;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ServerController implements Initializable {

	@FXML
	private Button startServerBtn;
	@FXML
	public TextArea Screen;

	private Stage primaryStage = Main.getPrimaryStage();
	private Server server;
	private boolean onServer = false;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		server = new Server(this);
		
		primaryStage.setOnCloseRequest(e -> {
				server.closeAllSocket();
				server.interrupt();
		});
	}
	public boolean getOnServer() {
		return this.onServer;
	}

	public void handleStartServer(MouseEvent event) {
		if (server != null) {
			if (!onServer) {
				if (server.getState() == State.NEW)
					server.start();
				Screen.appendText("Đã mở server.\n");
				Screen.appendText("Đang chờ kết nối từ client...\n");
				startServerBtn.setText("Close Server");
				onServer = true;
			} else {
				server.closeAllSocket();
				Screen.appendText("Đã đóng server.\n");
				server.interrupt();

				startServerBtn.setText("Start Server");
				onServer = false;
			}
			server.connectDatabase();
		}
	}
}

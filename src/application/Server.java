package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import application.db.DatabaseConnection;

public class Server extends Thread{

	private ServerController serverController;
	private ServerSocket serverSocket;
	private Socket client;
	private static List<ServerConnection> listClients;
	public boolean running = true;

	public Server(ServerController serverController) {
		this.serverController = serverController;
		initServer();
	}

	public static List<ServerConnection> getListClientThreads() {
		return listClients == null ? new ArrayList<>() : listClients;
	}
	
	public void closeAllSocket() {
		if (listClients.size() > 0) {
			for (ServerConnection serverConnection : listClients) {
				serverConnection.close();
			}
		} 
		
		running = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initServer() {
		try {
			serverSocket = new ServerSocket(Constants.SERVER_PORT);
			listClients = new ArrayList<>();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void connectDatabase() {
		if (serverController.getOnServer()) {
			DatabaseConnection.getInstance().connectDatabase();
			if (DatabaseConnection.getInstance().getConnection() != null) {
				serverController.Screen.appendText("Connected database. \n");
			} else {
				serverController.Screen.appendText("Error connect with database. \n");
			}
		} else {
			DatabaseConnection.getInstance().disconnectDatabase();
			serverController.Screen.appendText("Disconnected database. \n");
		}
	}

	@Override
	public void run() {
		while (running) {
				try {
					client = serverSocket.accept();
					ServerConnection ct = new ServerConnection(client, serverController);
					ct.start();
					listClients.add(ct);
					serverController.Screen.appendText("1 Client đã kết nối tới server.\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
}

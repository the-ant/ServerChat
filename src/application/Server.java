package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{

	private static final int SERVER_PORT = 5151;
	private ServerController serverController;
	private ServerSocket serverSocket;
	private Socket socket;
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
			serverSocket = new ServerSocket(SERVER_PORT);
			listClients = new ArrayList<>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				socket = serverSocket.accept();
				ServerConnection ct = new ServerConnection(socket, serverController);
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

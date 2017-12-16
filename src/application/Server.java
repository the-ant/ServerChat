package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import mysql.ChatAppConnectorDB;

public class Server extends Thread {

	private static final int SERVER_PORT = 5151;
	private static List<ServerConnection> listClients;
	private static Map<Integer, ServerConnection> mapClientConnections;
	
	private ServerController serverController;
	private ServerSocket serverSocket;
	private Socket socket;
	public boolean running = true;
	
	public Server(ServerController serverController) {
		this.serverController = serverController;
		initServer();
	}

	public static List<ServerConnection> getListClientThreads() {
		return listClients == null ? new ArrayList<>() : listClients;
	}

	public static Map<Integer, ServerConnection> getMapClientThreads() {
		return listClients == null ? new HashMap<Integer, ServerConnection>() : mapClientConnections;
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
			ChatAppConnectorDB.getInstance();
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
				System.out.println("close socket");
			}
		}
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}
}

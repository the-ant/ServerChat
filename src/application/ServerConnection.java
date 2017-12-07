package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends Thread {

	private ServerController serverController;
	private Socket socket;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private boolean running = true;

	public ServerConnection(Socket socket, ServerController serverController) {
		this.serverController = serverController;
		this.socket = socket;
		try {
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		try {
			while (running) {
				while (dataIn.available() == 0) {
				}
				
				String msg = dataIn.readUTF();
				if (msg != null) {
					if (!msg.equals("#quit")) {
						if (msg != null && !msg.equals("")) {
							sendToAll(msg);
						}
					} else {
						serverController.Screen.appendText("1 Client đã ngắt kết nối.\n");
						close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeConnection() {
		try {
			dataIn.close();
			dataOut.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendToAll(String msg) {
		for (ServerConnection sc : Server.getListClientThreads()) {
			if (sc.getSocket() != socket) {
				sc.sendMessage(msg);
			}
		}
	}

	private Socket getSocket() {
		return socket;
	}

	private void sendMessage(String msg) {
		try {
			dataOut.writeUTF(msg);
			dataOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		this.running = false;
		closeConnection();
	}

}

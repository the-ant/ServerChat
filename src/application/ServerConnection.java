package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import mysql.ChatAppConnectorDB;
import mysql.ResponseServer;
import pojo.FlagConnection;
import pojo.Group;
import pojo.Relationship;
import pojo.User;
import utils.JSONUtils;

public class ServerConnection extends Thread {

	private Socket socket;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private ServerController controller;
	private ChatAppConnectorDB connectorDB = ChatAppConnectorDB.getInstance();

	private boolean running = true;
	private User me;
	private String username, password;

	public ServerConnection(Socket socket, ServerController controller) {
		this.socket = socket;
		this.controller = controller;
		try {
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		try {
			while (running) {
				while (dataIn.available() == 0)
					;

				String msg = dataIn.readUTF();
				System.out.println("--> Msg To Server: " + msg);

				if (msg != null) {
					int options = handleOptions(msg);
					handleFrameReceive(options, msg);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	private void handleFrameReceive(int options, String msg) {
		if (options != 0) {
			switch (options) {
			case 1:
				alertNotExistUsername(msg);
				break;
			case 2:
				checkPasswordToLogin(msg);
				break;
			case 3:
				handleLoginForm(msg);
				break;
			case 4:
				alertExistUsernameRegister(msg);
				break;
			case 5:
				handleRegisterForm(msg);
				break;
			}
		} else {
			String[] frameMsg = msg.split("[|]");
			decodeFrame(frameMsg);
		}
	}

	public boolean handleLoginAction(User user, String password) {
		if (user != null) {
			if (user.getPassword().equals(password))
				return true;
		}
		return false;
	}

	public int handleOptions(String msg) {
		if (msg != null) {
			if (msg.contains("lgUsername"))
				return 1;

			if (msg.contains("lgPassword"))
				return 2;

			if (msg.contains("lgRequest"))
				return 3;

			if (msg.contains("rgUsername"))
				return 4;

			if (msg.contains("register"))
				return 5;
		}
		return 0;
	}

	private void alertNotExistUsername(String msg) {
		username = msg.substring(msg.indexOf("-") + 1);
		me = connectorDB.checkValidUsername(username);
		if (me == null) {
			sendMessage("alertNotExistUsername-" + ResponseServer.ALERT_INVALID_USERNAME);
		}
	}

	private void checkPasswordToLogin(String msg) {
		password = msg.substring(msg.indexOf("-") + 1);

		if (me != null && me.getPassword().equals(password)) {
			me.setOnline(true);
			connectorDB.updateStatusOnline(me.getUsername(), me.isOnline());

			controller.Screen.appendText(me.getUsername() + " dang nhap thanh cong. \n");

			String relationship = getListInfoOfFriends(me.getId());
			sendMessage("lgResult-true:" + me.getId() + "-" + me.getFullname() + "-" + relationship);
		} else {
			sendMessage("lgResult-false");
		}
	}

	private void handleLoginForm(String msg) {
		String username = msg.substring(msg.indexOf(":") + 1, msg.indexOf("-"));
		String password = msg.substring(msg.indexOf("-") + 1);

		User exUser = connectorDB.checkValidUsername(username);
		if (exUser != null && handleLoginAction(exUser, password)) {
			me.setOnline(true);
			connectorDB.updateStatusOnline(me.getUsername(), me.isOnline());
//			Ser

			String relationship = getListInfoOfFriends(me.getId());
			String response = "lgResult-true:" + exUser.getId() + "-" + exUser.getFullname() + "-" + relationship;

			sendMessage(response);
		} else {
			sendMessage("lgResult-false");
		}
	}

	private void alertExistUsernameRegister(String msg) {
		username = msg.substring(msg.indexOf("-") + 1);
		me = connectorDB.checkValidUsername(username);
		if (me != null) {
			System.out.println(ResponseServer.ALERT_EXIST_USERNAME);
			sendMessage("alertExistUsernameReg-" + ResponseServer.ALERT_EXIST_USERNAME);
		}
	}

	private void handleRegisterForm(String msg) {
		String info = msg.substring(msg.indexOf(":") + 1);
		String[] arrInfoUser = info.split("[-]");
		me = connectorDB.checkValidUsername(arrInfoUser[0]);

		if (me == null) {
			connectorDB.addNewUser(arrInfoUser[0], arrInfoUser[1], arrInfoUser[2]);
			User newUser = connectorDB.checkValidUsername(username);
			sendMessage("lgResult-true:" + newUser.getId() + "-" + newUser.getFullname());

		} else {
			System.out.println(ResponseServer.ALERT_EXIST_USERNAME);
			sendMessage("alertExistUsername-" + ResponseServer.ALERT_EXIST_USERNAME);
		}
	}

	private String getListInfoOfFriends(int userId) {
		Relationship relationship = connectorDB.getRelationshipByUserID(userId);
		String result = "";
		if (relationship != null) {
			List<User> listFriends = connectorDB.getListFriendsByID(relationship.getUserIDStr());
			List<Group> listGroups = connectorDB.getListGroupsByID(relationship.getListGroupsIDStr());

			JSONObject obj = JSONUtils.createJSONObject(listFriends, listGroups);
			result = obj.toString();
		}
		return result;
	}

	private void decodeFrame(String[] frameRequestFromClient) {
		int flag = Integer.parseInt(frameRequestFromClient[0]);
		System.out.println("==> Flag: " + flag);

		switch (flag) {
		case FlagConnection.LOGOUT:
			requestLogout();
			break;

		case FlagConnection.SEND_MESSAGE:
			int groupIDSendMsg = Integer.parseInt(frameRequestFromClient[1]);
			String desId = frameRequestFromClient[2];
			String msg = frameRequestFromClient[3];
			requestSendMessage(groupIDSendMsg, desId, msg);
			break;

		case FlagConnection.GET_MESSAGE:
			int groupIDGetMsg = Integer.parseInt(frameRequestFromClient[1]);
			requestGetMessage(groupIDGetMsg);
			break;

		case FlagConnection.GET_GROUP:
			int groupIDGetGroup = Integer.parseInt(frameRequestFromClient[1]);
			requestGetGroup(groupIDGetGroup);
			break;

		case FlagConnection.GET_USER:
			int userIdGetUser = Integer.parseInt(frameRequestFromClient[1]);
			requestGetUser(userIdGetUser);
			break;

		case FlagConnection.ADD_FRIEND:
			int userIdAddFriend = Integer.parseInt(frameRequestFromClient[1]);
			requestAddFriend(userIdAddFriend);
			break;

		case FlagConnection.ADD_GROUP:
			String groupNameAddGroup = frameRequestFromClient[1];
			String listUsersAddGroup = frameRequestFromClient[2];
			requestAddGroup(groupNameAddGroup, listUsersAddGroup);
			break;
		}
	}

	private void requestAddGroup(String groupNameAddGroup, String listUsersAddGroup) {

	}

	private void requestAddFriend(int userIdAddFriend) {

	}

	private void requestGetUser(int userIdGetUser) {

	}

	private void requestGetGroup(int groupIDGetGroup) {

	}

	private void requestGetMessage(int groupIDGetMsg) {

	}

	private void requestSendMessage(int groupID, String desId, String msg) {
		connectorDB.insertMessage(groupID, me.getId(), msg, new Date());
		sendMessageToFriendById(groupID, desId, msg);
	}

	private void sendMessageToFriendById(int groupID, String desId, String msg) {
		String[] IDarr = desId.split("[,]");
		for (String e : IDarr) {
			int tmpId = Integer.parseInt(e);
			if (tmpId != me.getId()) {
				
			}
		}
//		for (ServerConnection con: Server.getListClientThreads()) {
//			if (con.getSocket() != socket) {
//				User user = con.getUser();
//				for(String s : IDarr) {
//					int id = Integer.parseInt(s);
//					if (id != me.getId() && id == user.getId()) {
//						String response = FlagConnection.GET_MESSAGE + "|" + groupID + "|" + msg;
//						con.sendMessage(response);
//					}
//				}
//			}
//		}
	}

	private void requestLogout() {

	}

	private void closeConnection() {
		try {
			dataIn.close();
			dataOut.close();
			socket.close();
			this.interrupt();
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

	public User getUser() {
		return me;
	}

	public void setUser(User user) {
		this.me = user;
	}
}

package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import mysql.ChatAppConnectorDB;
import mysql.StructureJSON;
import pojo.FlagConnection;
import pojo.Group;
import pojo.Relationship;
import pojo.User;
import utils.JSONUtils;

public class ServerConnection extends Thread {

	private Socket socket;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private boolean running = true;
	private User user;
	private ChatAppConnectorDB connectorDB = ChatAppConnectorDB.getInstance();

	public ServerConnection(Socket socket, ServerController serverController) {
		this.socket = socket;
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
				while (dataIn.available() == 0) {
				}

				String msg = dataIn.readUTF();
				System.out.println("--> Msg To Server: " + msg);
				String[] frameRequestFromClient = msg.split("[|]");
				decodeFrame(frameRequestFromClient);
			}
		} catch (IOException e) {
			e.printStackTrace();
			close();

		}
	}

	private void decodeFrame(String[] frameRequestFromClient) {
		int flag = Integer.parseInt(frameRequestFromClient[0]);
		System.out.println("==> Flag: " + flag);

		switch (flag) {
		case FlagConnection.LOGIN:
			String usernameLogin = frameRequestFromClient[1];
			String passwordLogin = frameRequestFromClient[2];
			requestLogin(usernameLogin, passwordLogin);
			break;

		case FlagConnection.LOGOUT:
			requestLogout();
			break;

		case FlagConnection.REGISTER:
			String usernameRegister = frameRequestFromClient[1];
			String passwordRegister = frameRequestFromClient[2];
			String fullnameRegister = frameRequestFromClient[3];
			requestRegister(usernameRegister, passwordRegister, fullnameRegister);
			break;

		case FlagConnection.SEND_MESSAGE:
			int groupIDSendMsg = Integer.parseInt(frameRequestFromClient[1]);
			String msg = frameRequestFromClient[2];
			requestSendMessage(groupIDSendMsg, msg);
			break;

		case FlagConnection.GET_RELATIONSHIP:
			requestGetRelationship();
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

	private void requestGetRelationship() {
		String result = FlagConnection.GET_RELATIONSHIP + "|";
		int userId = user.getId();
		Relationship relationship = connectorDB.getRelationshipByUserID(userId);
		if (relationship != null) {
			List<Integer> listGroupsID = relationship.getListGroupsID();
			List<Integer> listFriendsID = relationship.getListFriendsID();

			List<User> listFriends = getListFriendsByID(listFriendsID);
			List<Group> listGroups = getListGroupsByID(listGroupsID);
			
			JSONObject obj = JSONUtils.createJSONObject(listFriends, listGroups);
			result += obj.toString();
		}
		sendMessage(result);
	}
	
	private void requestSendMessage(int groupID, String msg) {

	}

	private void requestRegister(String usernameRegister, String passwordRegister, String fullnameRegister) {
	}

	private void requestLogout() {

	}

	private void requestLogin(String username, String password) {
		user = connectorDB.getUserForLogin(username, password);
		if (user != null) {
			sendMessage(FlagConnection.LOGIN + "|" + "1");
			System.out.println("Login successfull");
		} else {
			sendMessage(FlagConnection.LOGIN + "|" + "0");
			System.out.println("Login fail");
		}
	}

	private List<Group> getListGroupsByID(List<Integer> listGroupsID) {
		List<Group> result = new ArrayList<>();
		if (listGroupsID.size() > 0) {
			for (int id : listGroupsID) {
				Group group = connectorDB.getGroupByID(id);
				if (group != null)
					result.add(group);
			}
		}
		return result;
	}

	private List<User> getListFriendsByID(List<Integer> listFriendsID) {
		List<User> result = new ArrayList<User>();
		if (listFriendsID.size() > 0) {
			for (int id : listFriendsID) {
				User user = connectorDB.getUser(id);
				if (user != null)
					result.add(user);
			}
		}
		return result;
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

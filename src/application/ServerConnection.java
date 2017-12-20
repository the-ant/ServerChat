package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
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
				while (dataIn.available() == 0) {
					Thread.sleep(1);
				}

				String msg = dataIn.readUTF();
				System.out.println("--> Msg To Server: " + msg);

				if (msg != null) {
					int options = handleOptions(msg);
					handleFrameReceive(options, msg);
				}
			}
		} catch (IOException | InterruptedException e) {
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
			me = connectorDB.checkValidUsername(arrInfoUser[0]);
			String relationship = getListInfoOfFriends(me.getId());
			sendMessage("lgResult-true:" + me.getId() + "-" + me.getFullname() + "-" + relationship);

		} else {
			System.out.println(ResponseServer.ALERT_EXIST_USERNAME);
			sendMessage("alertExistUsername-" + ResponseServer.ALERT_EXIST_USERNAME);
		}
	}

	private String getListInfoOfFriends(int userId) {
		Relationship relationship = connectorDB.getRelationshipByUserID(userId);
		JSONObject obj = null;
		if (relationship != null) {
			List<User> listFriends = connectorDB.getListFriendsByID(relationship.getUserIDStr());
			List<Group> listGroups = connectorDB.getListGroupsByID(relationship.getListGroupsIDStr());

			obj = JSONUtils.createJSONObject(listFriends, listGroups);
		} else {
			obj = JSONUtils.createEmptyJSONObject();
		}
		return obj.toString();
	}

	private void decodeFrame(String[] frameRequestFromClient) {
		int flag = Integer.parseInt(frameRequestFromClient[0]);
		System.out.println("==> Flag: " + flag);

		switch (flag) {
		case FlagConnection.GET_ALL_USER:
			requestGetAllUser();
			break;

		case FlagConnection.LOGOUT:
			requestLogout();
			break;

		case FlagConnection.SEND_MESSAGE:
			break;

		case FlagConnection.GET_MESSAGE:
			int groupIDGetMsg = Integer.parseInt(frameRequestFromClient[1]);
			requestGetMessage(groupIDGetMsg);
			break;

		case FlagConnection.ADD_FRIEND:
			int userIdRequest = this.me.getId();
			int userIdReceive = Integer.parseInt(frameRequestFromClient[1]);
			insertRequestAddFriend(userIdRequest, userIdReceive);
			requestAddFriend(userIdReceive);
			break;

		case FlagConnection.ADD_GROUP:
			String groupNameAddGroup = frameRequestFromClient[1];
			int userIDCreatedGroup = Integer.parseInt(frameRequestFromClient[2]);
			String listUsersAddGroup = frameRequestFromClient[3];
			requestAddGroup(groupNameAddGroup, userIDCreatedGroup, listUsersAddGroup);
			break;
		case FlagConnection.DELETE_REQUEST_RECORD:
			int userIdRequested = Integer.parseInt(frameRequestFromClient[1]);
			int userIdReceived = this.me.getId();
			deleteRequested(userIdRequested, userIdReceived);
			String listUserId = userIdRequested + "," + userIdReceived;
			requestAddGroup("", userIdReceived, listUserId);
			// get group_id to update list_groups to relationship.
			int groupId = connectorDB.getGroupIdByListUsers(listUserId);
			System.out.println("groupId : " + groupId);
			updateRelationship(userIdRequested, userIdReceived, groupId);
			updateRelationship(userIdReceived, userIdRequested, groupId);
			break;
		case FlagConnection.GET_ALL_REQUESTS:
			int userId = me.getId();
			getAllRequestsAddFriends(userId);
			break;
		}
	}

	private void deleteRequested(int userIdRequested, int userIdReceived) {
		if (connectorDB.deleteRequested(userIdRequested, userIdReceived)) {
			// get all requests.
			getAllRequestsAddFriends(userIdReceived);
		}
	}

	private void updateRelationship(int userIdRequested, int userIdReceived, int groupId) {
		List<Integer> listFriends = null;
		List<Integer> listGroups = null;
		Relationship newRelationship = null;
		// check exists relationship record.
		Relationship relationship = connectorDB.getRelationshipByUserID(userIdRequested);
		if (relationship == null) {
			listFriends = new ArrayList<>();
			listFriends.add(userIdReceived);
			listGroups = new ArrayList<>();
			listGroups.add(groupId);
			newRelationship = new Relationship(userIdRequested, listFriends, listGroups);
			connectorDB.insertRelationship(newRelationship);
		} else {
			System.out.println("relationship of admin: " + relationship.toString());
			// user has relationship into database.
			listFriends = relationship.getListFriendsID();
			listFriends.add(userIdReceived);
			listGroups = relationship.getListGroupsID();
			listGroups.add(groupId);
			newRelationship = new Relationship(userIdRequested, listFriends, listGroups);
			connectorDB.updateRelationship(newRelationship);
		}
		sendFlagUpdateRelationship(userIdRequested);
	}

	private void sendFlagUpdateRelationship(int userId) {
		String recordRelationship = getListInfoOfFriends(userId);
		Server.findConnectionById(userId).sendMessage(FlagConnection.UPDATE_RELATIONSHIP + "|" + recordRelationship);
	}

	private void getAllRequestsAddFriends(int userId) {
		String listOfRequests = connectorDB.getAllRequests(userId);
		System.out.println("list requests: " + listOfRequests);
		sendMessage(FlagConnection.UPDATE_REQUEST_ADD_FRIEND + "|" + listOfRequests);
	}

	private void insertRequestAddFriend(int userIdRequested, int userIdReceive) {
		connectorDB.insertRequestAddFriend(userIdRequested, userIdReceive);
	}

	private void requestGetAllUser() {
		List<User> allUser = connectorDB.getAllUser(me.getId());
		JSONObject result = JSONUtils.createAllUserObject(allUser);
		String response = FlagConnection.GET_ALL_USER + "|" + result.toString();
		sendMessage(response);
	}

	private void requestAddGroup(String groupNameAddGroup, int userIDCreatedGroup, String listUsersAddGroup) {
		String[] listUserString = listUsersAddGroup.split(",");
		List<Integer> listUserID = new ArrayList<Integer>();
		int isChatGroup = 0;
		if (listUserID.size() >= 3) {
			isChatGroup = 1;
		}
		for (String userId : listUserString) {
			listUserID.add(Integer.parseInt(userId));
		}
		try {
			connectorDB.insertGroup(new Group(groupNameAddGroup, userIDCreatedGroup, isChatGroup, listUserID));
			sendMessage(FlagConnection.ADD_GROUP + "|" + "1");
		} catch (SQLException e) {
			sendMessage(FlagConnection.ADD_GROUP + "|" + "0");
			e.printStackTrace();
		}
	}

	private void requestAddFriend(int userIdAddFriend) {
		int resultOnline = connectorDB.getOnlineByUserId(userIdAddFriend);
		if (resultOnline == 1) {
			// online
			ServerConnection sc = Server.findConnectionById(userIdAddFriend);
			if (sc != null) {
				sc.sendMessage(
						FlagConnection.REQUEST_ADD_FRIEND + "|" + this.me.getId() + "|" + this.me.getFullname());
			}
		} else {
			insertRequestAddFriend(me.getId(), userIdAddFriend);
		}
	}

	private void requestGetMessage(int groupIDGetMsg) {

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

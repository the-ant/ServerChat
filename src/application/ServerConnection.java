package application;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import mysql.ChatAppConnectorDB;
import mysql.ResponseServer;
import pojo.FlagConnection;
import pojo.Group;
import pojo.Message;
import pojo.MyFile;
import pojo.Relationship;
import pojo.User;
import utils.JSONUtils;

public class ServerConnection extends Thread {

	private static final String DIRECTORY_SAVE_FILES = "D:\\Java\\WS\\ServerChatSDT\\save_files\\";

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
			notifyOnlineToOnlineFriends();

			String relationship = getListInfoOfFriends(me.getId());
			sendMessage("lgResult-true:" + me.getId() + "-" + me.getFullname() + "-" + relationship);
		} else {
			sendMessage("lgResult-false");

		}
	}

	private void handleLoginForm(String msg) {
		String username = msg.substring(msg.indexOf(":") + 1, msg.indexOf("-"));
		String password = msg.substring(msg.indexOf("-") + 1);

		me = connectorDB.checkValidUsername(username);
		if (me != null && handleLoginAction(me, password)) {
			me.setOnline(true);
			connectorDB.updateStatusOnline(me.getUsername(), me.isOnline());

			String relationship = getListInfoOfFriends(me.getId());
			String response = "lgResult-true:" + me.getId() + "-" + me.getFullname() + "-" + relationship;

			sendMessage(response);

			String notification = FlagConnection.NOTIFY_ONLINE + "|" + me.getId();
			notifyToOnlineFriends(notification);
		} else {
			sendMessage("lgResult-false");
		}
	}

	private void alertExistUsernameRegister(String msg) {
		username = msg.substring(msg.indexOf("-") + 1);
		me = connectorDB.checkValidUsername(username);
		if (me != null) {
			sendMessage("alertExistUsernameReg-" + ResponseServer.ALERT_EXIST_USERNAME);
		}
	}

	private void handleRegisterForm(String msg) {
		String info = msg.substring(msg.indexOf(":") + 1);
		String[] arrInfoUser = info.split("[-]");
		me = connectorDB.checkValidUsername(arrInfoUser[0]);

		if (me == null) {
			connectorDB.addNewUser(arrInfoUser[0], arrInfoUser[1], arrInfoUser[2]);
			me = connectorDB.checkValidUsername(username);
			
			String relationship = getListInfoOfFriends(me.getId());
			sendMessage("lgResult-true:" + me.getId() + "-" + me.getFullname() + "-" + relationship);

		} else {
			System.out.println(ResponseServer.ALERT_EXIST_USERNAME);
			sendMessage("alertExistUsername-" + ResponseServer.ALERT_EXIST_USERNAME);
		}
	}

	private void notifyOnlineToOnlineFriends() {
		Relationship relationship = connectorDB.getRelationshipByUserID(me.getId());
		List<User> listFriends = connectorDB.getListFriendsByID(relationship.getUserIDStr());

		for (User user : listFriends) {
			ServerConnection conn = Server.findConnectionById(user.getId());
			if (conn != null && user.isOnline()) {
				String notification = FlagConnection.NOTIFY_ONLINE + "|" + me.getId();
				conn.sendMessage(notification);
			}
		}
	}

	private String getListInfoOfFriends(int userId) {
		Relationship relationship = connectorDB.getRelationshipByUserID(userId);
		String result = "";
		JSONObject obj = null;

		if (relationship != null) {
			List<User> listFriends = connectorDB.getListFriendsByID(relationship.getUserIDStr());
			List<Group> listGroups = connectorDB.getListGroupsByID(relationship.getListGroupsIDStr());

			obj = JSONUtils.createRelationshipObj(listFriends, listGroups);
		} else {
			obj = JSONUtils.createEmptyJSONObject();
		}
		result = obj.toString();
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

		case FlagConnection.ADD_FRIEND:
			int userIdAddFriend = Integer.parseInt(frameRequestFromClient[1]);
			requestAddFriend(userIdAddFriend);
			break;

		case FlagConnection.ADD_GROUP:
			String groupNameAddGroup = frameRequestFromClient[1];
			String listUsersAddGroup = frameRequestFromClient[2];
			requestAddGroup(groupNameAddGroup, listUsersAddGroup);
			break;

		case FlagConnection.SEND_FILE:
			requestSendFile(frameRequestFromClient);
			break;

		case FlagConnection.DOWN_LOAD_FILE:
			int groupId = Integer.parseInt(frameRequestFromClient[1]);
			String fileName = frameRequestFromClient[2];
			requestDLFile(groupId, fileName);
			break;
		}
	}

	private void requestDLFile(int groupId, String fileName) {
		System.out.println("requestSendFile: " + fileName);

		File folder = new File(DIRECTORY_SAVE_FILES + "\\" + groupId);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				System.out.println("File: " + file.getName());
				if (file.isFile() && fileName.equals(file.getName())) {
					try {
						System.out.println("Write nek: " + file.getName());
						InputStream is = new FileInputStream(file);
						OutputStream os = socket.getOutputStream();
						BufferedInputStream bufferedInputStream = new BufferedInputStream(is);

						byte[] buffer = new byte[16 * 1024];
						int count;
						while ((count = bufferedInputStream.read(buffer)) > 0) {
							os.write(buffer, 0, count);
						}
						os.flush();
						os.close();
						bufferedInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void requestSendFile(String[] frameRequestFromClient) {
		try {
			int mId = Integer.parseInt(frameRequestFromClient[1]);
			int group_id = Integer.parseInt(frameRequestFromClient[2]);
			String usersGroup = frameRequestFromClient[3];

			MyFile file = new MyFile();
			String[] fileInfo = frameRequestFromClient[4].split("[,]");
			String fileName = fileInfo[0];
			long fileSize = Long.parseLong(fileInfo[1]);

			file.setName(fileName);
			file.setSize(fileSize);
			String filePath = DIRECTORY_SAVE_FILES + "\\" + group_id;

			File saveFile = new File(filePath);
			if (!saveFile.exists()) {
				saveFile.mkdir();
			}
			file.setPath(filePath + "\\" + fileName);

			System.out.println("requestSendFile: " + mId + " - " + group_id + " - " + fileName);
			connectorDB.insertMessage(group_id, mId, true, fileName, new Date());

			InputStream fileIS = socket.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileIS);
			FileOutputStream fos = new FileOutputStream(file.getPath());

			byte[] buffer = new byte[16 * 1024];
			int count;
			while ((count = bufferedInputStream.read(buffer)) != -1) {
				fos.write(buffer, 0, count);
			}
			fos.flush();
			fos.close();

			sendMessageToFriendById(group_id, mId, usersGroup, fileName, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void requestAddGroup(String groupNameAddGroup, String listUsersAddGroup) {

	}

	private void requestAddFriend(int userIdAddFriend) {

	}

	private void requestGetMessage(int groupIDGetMsg) {
		List<Message> listMsgs = connectorDB.getMessagesByGroupID(groupIDGetMsg);
		JSONObject obj = JSONUtils.createMessagesObj(listMsgs);
		String respone = FlagConnection.GET_MESSAGE + "|" + groupIDGetMsg + "|" + obj.toString();
		System.out.println("requestGetMessage: " + respone);
		sendMessage(respone);
	}

	private void requestSendMessage(int groupID, String desId, String msg) {
		connectorDB.insertMessage(groupID, me.getId(), false, msg, new Date());
		sendMessageToFriendById(groupID, me.getId(), desId, msg, false);
	}

	private void sendMessageToFriendById(int groupID, int mId, String desId, String msg, boolean isFileName) {
		String[] IDarr = desId.split("[,]");

		for (String e : IDarr) {
			int tmpId = Integer.parseInt(e);
			if (tmpId != mId) {
				ServerConnection desConnection = Server.findConnectionById(tmpId);
				if (desConnection != null) {
					boolean online = connectorDB.getUserOnline(tmpId);
					if (online) {
						String response = FlagConnection.RECEIVE_MESSAGE + "|" + groupID + "|" + mId + "|" + msg + "|"
								+ isFileName;
						desConnection.sendMessage(response);
					}
				}
			}
		}
	}

	private void requestLogout() {
		boolean logoutSuccessfull = connectorDB.updateStatusOnline(me.getId(), false);
		String responseLogout = FlagConnection.LOGOUT + "|";
		if (logoutSuccessfull) {
			responseLogout += 1;
			String notificationLogout = FlagConnection.NOTIFY_LOGOUT + "|" + me.getId();
			notifyToOnlineFriends(notificationLogout);
		} else {
			responseLogout += 0;
		}
		sendMessage(responseLogout);
	}

	private void notifyToOnlineFriends(String notification) {
		Relationship relationship = connectorDB.getRelationshipByUserID(me.getId());
		if (relationship != null) {
			List<User> listFriends = connectorDB.getListFriendsByID(relationship.getUserIDStr());
			for (User user : listFriends) {
				ServerConnection conn = Server.findConnectionById(user.getId());
				if (conn != null && user.isOnline()) {
					conn.sendMessage(notification);
				}
			}
		}
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

	public Socket getSocket() {
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

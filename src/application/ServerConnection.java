package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import application.db.DatabaseConnection;
import application.entities.Group;
import application.entities.Relationship;
import application.entities.Users;

public class ServerConnection extends Thread {
	private ServerController serverController;
	private Socket client;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private boolean running = true;
	private Connection conn;
	private Users user = null;
	private String username = null, password = null;

	public ServerConnection(Socket client, ServerController serverController) {
		this.serverController = serverController;
		this.client = client;
		try {
			dataIn = new DataInputStream(client.getInputStream());
			dataOut = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.conn = DatabaseConnection.getInstance().getConnection();
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Users checkValidUsername(String username) {
		PreparedStatement preStatement = null;
		Users user = null;
		try {
			preStatement = conn.prepareStatement(Constants.GET_CLIENT_BY_USERNAME);
			preStatement.setString(1, username);
			ResultSet result = preStatement.executeQuery();
			if (result != null) {
				while (result.next()) {
					user = new Users(result.getInt(1), result.getString(2), result.getString(3), result.getInt(4),
							result.getString(5));
				}
				return user;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preStatement != null) {
				try {
					preStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void updateStatusOnline(String username) {
		PreparedStatement preStatement = null;
		try {
			preStatement = conn.prepareStatement(Constants.UPDATE_STATUS_ONLINE);
			preStatement.setInt(1, 1);
			preStatement.setString(2, username);
			preStatement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			if (preStatement != null) {
				try {
					preStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addNewUser(String username, String password, String fullName) {
		PreparedStatement preStatement = null;
		try {
			preStatement = conn.prepareStatement(Constants.INSERT_NEW_USER);
			preStatement.setString(1, username);
			preStatement.setString(2, password);
			preStatement.setInt(3, 1); // if client registers successfully, account of client will be logged in.
			preStatement.setString(4, fullName);
			preStatement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			if (preStatement != null) {
				try {
					preStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean handleLoginAction(Users user, String password) {
		if (user != null) {
			if (user.getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}

	public int handleOptions(String msg) {
		if (msg != null) {
			if (msg.contains("lgUsername")) {
				return 1;
			}
			if (msg.contains("lgPassword")) {
				return 2;
			}
			if (msg.contains("lgRequest")) {
				return 3;
			}
			if (msg.contains("rgUsername")) {
				return 4;
			}
			if (msg.contains("register")) {
				return 5;
			}
		}
		return 0;
	}

	private void alertNotExistUsername(String msg) {
		username = msg.substring(msg.indexOf("-") + 1);
		user = checkValidUsername(username);
		if (user == null) {
			// notify invalid username.
			sendToCurrentClient("alertNotExistUsername-" + Constants.ALERT_INVALID_USERNAME);
		}
	}

	private void checkPasswordToLogin(String msg) {
		password = msg.substring(msg.indexOf("-") + 1);
		if (user != null) {
			if (user.getPassword().equals(password)) {
				// update status online.
				updateStatusOnline(user.getUsername());
				serverController.Screen.appendText(user.getUsername() + " dang nhap thanh cong. \n");
				// login successfully
				String relationships = getListInfoOfFriends(user.getUserId());
				sendToCurrentClient("lgResult-true:" + user.getUserId() + "-" + user.getFullName() + "-" + relationships);
			} else {
				serverController.Screen.appendText(" Dang nhap khong thanh cong. \n");
				sendToCurrentClient("lgResult-false");
			}
		} else {
			serverController.Screen.appendText(" Dang nhap khong thanh cong. \n");
			sendToCurrentClient("lgResult-false");
		}
	}

	private void handleLoginForm(String msg) {
		String username = msg.substring(msg.indexOf(":") + 1, msg.indexOf("-"));
		String password = msg.substring(msg.indexOf("-") + 1);
		Users exUser = checkValidUsername(username);
		if (exUser != null) {
			if (handleLoginAction(exUser, password)) {
				updateStatusOnline(username);
				String relationships = getListInfoOfFriends(exUser.getUserId());
				sendToCurrentClient("lgResult-true:" + exUser.getUserId() + "-" + exUser.getFullName() + "-" + relationships);
			} else {
				sendToCurrentClient("lgResult-false");
			}
		} else {
			sendToCurrentClient("lgResult-false");
		}
	}

	private void alertExistUsernameRegister(String msg) {
		username = msg.substring(msg.indexOf("-") + 1);
		user = checkValidUsername(username);
		if (user != null) {
			System.out.println(Constants.ALERT_EXIST_USERNAME);
			sendToCurrentClient("alertExistUsernameReg-" + Constants.ALERT_EXIST_USERNAME);
		}
	}

	private void handleRegisterForm(String msg) {
		String info = msg.substring(msg.indexOf(":") + 1);
		String[] arrInfoUser = info.split("[-]");
		user = checkValidUsername(arrInfoUser[0]);
		if (user == null) {
			addNewUser(arrInfoUser[0], arrInfoUser[1], arrInfoUser[2]);
			Users newUser = checkValidUsername(username);
			sendToCurrentClient("lgResult-true:" + newUser.getUserId() + "-" + newUser.getFullName());
		} else {
			System.out.println(Constants.ALERT_EXIST_USERNAME);
			sendToCurrentClient("alertExistUsername-" + Constants.ALERT_EXIST_USERNAME);
		}
	}

	// getListFriendsById function
	private Relationship getRelationships(int userId) {
		PreparedStatement preStatement = null;
		try {
			preStatement = conn.prepareStatement(Constants.GET_LIST_FRIENDS_BY_USERID);
			preStatement.setInt(1, userId);
			ResultSet result = preStatement.executeQuery();
			while (result.next()) {
				String friendsStr = result.getString(StructureDB.RELATIONSHIP_LIST_FRIENDS);
				String groupsStr = result.getString(StructureDB.RELATIONSHIP_LIST_GROUPS);
				List<Integer> listFriendsID = new ArrayList<>();
				for (String e : friendsStr.split(",")) {
					listFriendsID.add(Integer.parseInt(e));
				}
				List<Integer> listGroupsID = new ArrayList<>();
				for (String e : groupsStr.split(",")) {
					listGroupsID.add(Integer.parseInt(e));
				}
				Relationship relationships = new Relationship(userId, listFriendsID, listGroupsID);
				relationships.setListUserIds(friendsStr);
				return relationships;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (preStatement != null) {
				try {
					preStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private List<Users> getListFriendsById(String listUserIds) {
		System.out.println("list userids : " + listUserIds);
		Statement statement = null;
		String[] arrUserIds = listUserIds.split(",");
		List<Users> users = new ArrayList<Users>();
		try {
			statement = conn.createStatement();
			ResultSet result = statement.executeQuery(Constants.GET_LIST_INFO_USER_BY_USERID + listUserIds + ")");
			int i = 0;
			while (result.next()) {
				String fullname = result.getString(StructureDB.USER_FULLNAME);
				boolean online = result.getBoolean(StructureDB.ONLINE);
				int formatOnline = 0;
				if (online) {
					formatOnline = 1;
				}
				int id = Integer.parseInt(arrUserIds[i++]);
				System.out.println("id => " + id);
				users.add(new Users(id, fullname, formatOnline));
			}
			return users;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private String getListInfoOfFriends(int userId) {
		Relationship relationships = getRelationships(userId);
		if (relationships != null) {
			List<Integer> listGroupIds = relationships.getListGroupsID();
			List<Users> listFriends = getListFriendsById(relationships.getListUserIds());
			List<Group> listGroups = getListGroupsByID(listGroupIds);
			JSONObject obj = JSONUtils.createJSONObject(listFriends, listGroups);
			return obj.toString();
		}
		return null;
	}

	private List<Group> getListGroupsByID(List<Integer> listGroupsID) {
		List<Group> result = new ArrayList<>();
		if (listGroupsID.size() > 0) {
			for (int id : listGroupsID) {
				Group group = getGroupByID(id);
				if (group != null)
					result.add(group);
			}
		}
		return result;
	}

	public Group getGroupByID(int id) {
		Group result = null;
		Statement statement = null;
		String query = "Select * from " + StructureDB.TABLE_GROUPS + " where " + StructureDB.GROUP_ID + " = " + id;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(query);
			while (rs.next()) {
				String name = rs.getString(StructureDB.GROUP_NAME);
				int userIDCreated = rs.getInt(StructureDB.GROUP_USER_ID_CREATED);
				boolean isChatGroup = rs.getBoolean(StructureDB.GROUP_IS_CHAT_GROUP);
				String usersStr = rs.getString(StructureDB.GROUP_LIST_USERS);

				List<Integer> listUsersID = new ArrayList<>();
				for (String e : usersStr.split(",")) {
					listUsersID.add(Integer.parseInt(e));
				}

				result = new Group(id, name, userIDCreated, isChatGroup, listUsersID);
				System.out.println("----------getGroupByID----------");
				System.out.println("name: " + name);
				System.out.println("userIDCreated: " + userIDCreated);
				System.out.println("usersStr: " + usersStr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	@Override
	public void run() {

		try {
			while (running) {
				while (dataIn.available() == 0) {
				}
				String msg = dataIn.readUTF();
				if (msg != null) {
					int options = handleOptions(msg);
					switch (options) {
					/* Handle login form */
					case 1:
						alertNotExistUsername(msg);
						break;
					case 2:
						checkPasswordToLogin(msg);
						break;
					case 3:
						handleLoginForm(msg);
						break;
					/* Handle register form */
					case 4:
						alertExistUsernameRegister(msg);
						break;
					case 5:
						handleRegisterForm(msg);
						break;
					/* Main controller handler. */
					default:
						break;
					}
					continue;
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
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// private void sendToAll(String msg) {
	// for (ServerConnection sc : Server.getListClientThreads()) {
	// if (sc.getCLient() != client) {
	// sc.sendMessage(msg);
	// }
	// }
	// }
	private void sendToCurrentClient(String msg) {
		for (ServerConnection sc : Server.getListClientThreads()) {
			if (sc.getCLient() == client) {
				sc.sendMessage(msg);
			}
		}
	}

	private Socket getCLient() {
		return client;
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

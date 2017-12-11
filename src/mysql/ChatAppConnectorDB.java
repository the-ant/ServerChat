package mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pojo.Group;
import pojo.Message;
import pojo.Relationship;
import pojo.User;

public class ChatAppConnectorDB {

	private Connection mConnection;
	private Statement mStatement;

	private static class ChatAppConnectorDBHelper {
		private static final ChatAppConnectorDB INSTANCE = new ChatAppConnectorDB();
	}

	public static ChatAppConnectorDB getInstance() {
		return ChatAppConnectorDBHelper.INSTANCE;
	}

	private ChatAppConnectorDB() {
		mConnection = MySQLConnUtils.getConnection();
		if (mConnection != null) {
			try {
				mStatement = mConnection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Message> getMessagesByGroupID(int groupID) {
		List<Message> result = new ArrayList<>();

		String query = "Select * from " + StructureDB.TABLE_MESSAGE + " where " + StructureDB.MESSAGE_GROUP_ID + " = "
				+ groupID;
		ResultSet rs = null;
		try {
			rs = mStatement.executeQuery(query);
			while (rs.next()) {
				int messageID = rs.getInt(StructureDB.MESSAGE_ID);
				String message = rs.getString(StructureDB.MESSAGE);
				int userID = rs.getInt(StructureDB.MESSAGE_USER_ID);
				Date dateTime = rs.getTimestamp(StructureDB.MESSAGE_DATE_TIME);

				System.out.println("----------getMessagesByGroupID----------");
				System.out.println("message_id: " + messageID);
				System.out.println("group_id: " + groupID);
				System.out.println("message: " + message);
				System.out.println("user_id: " + userID);
				System.out.println("date_time :" + DateTimeUtils.formatDateToStringDB(dateTime));

				Message msg = new Message(groupID, userID, message, dateTime);
				result.add(msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}

		return result;
	}

	public Group getGroupByID(int id) {
		Group result = null;

		String query = "Select * from " + StructureDB.TABLE_GROUPS + " where " + StructureDB.GROUP_ID + " = " + id;
		ResultSet rs = null;
		try {
			rs = mStatement.executeQuery(query);
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

	public User getUserForLogin(String username, String password) {
		System.out.println("--> Request login: " + username + " - " + password);
		User result = null;
		String query = "Select * from " + StructureDB.TABLE_USERS + " where " + StructureDB.USERNAME + " = '" + username
				+ "' and " + StructureDB.PASSWORD + " = '" + password + "'";

		System.out.println("-> Query: " + query);
		try {
			ResultSet rs = mStatement.executeQuery(query);
			while (rs.next()) {
				int id = rs.getInt(StructureDB.USER_ID);
				String name = rs.getString(StructureDB.USERNAME);
				boolean online = rs.getBoolean(StructureDB.ONLINE);

				result = new User();
				result.setId(id);
				result.setUsername(name);
				result.setOnline(online);

				System.out.println("----------getUser----------");
				System.out.println("name: " + name);
				System.out.println("online: " + online);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	public User getUser(int id) {
		User result = null;
		String query = "Select * from " + StructureDB.TABLE_USERS + " where " + StructureDB.USER_ID + " = " + id;
		try {
			ResultSet rs = mStatement.executeQuery(query);
			while (rs.next()) {
				String name = rs.getString(StructureDB.USERNAME);
				boolean online = rs.getBoolean(StructureDB.ONLINE);

				System.out.println("----------getUser----------");
				System.out.println("name: " + name);
				System.out.println("online: " + online);
				result = new User(id, name, online);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	public Relationship getRelationshipByUserID(int id) {
		Relationship result = null;
		String query = "Select * from " + StructureDB.TABLE_RELATIONSHIP + " where " + StructureDB.RELATIONSHIP_USER_ID
				+ " = " + id;
		try {
			ResultSet rs = mStatement.executeQuery(query);
			while (rs.next()) {
				String friendsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_FRIENDS);
				String groupsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_GROUPS);

				System.out.println("----------getRelationshipByUserID----------");
				List<Integer> listFriendsID = new ArrayList<>();
				for (String e : friendsStr.split(",")) {
					System.out.println("friend: " + e);
					listFriendsID.add(Integer.parseInt(e));
				}

				List<Integer> listGroupsID = new ArrayList<>();
				for (String e : groupsStr.split(",")) {
					System.out.println("group: " + e);
					listGroupsID.add(Integer.parseInt(e));
				}

				result = new Relationship(id, listFriendsID, listGroupsID);
				System.out.println("user_id: " + id);
				System.out.println("friends: " + friendsStr);
				System.out.println("groups: " + groupsStr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	public void insertMessage(Message msg) {
		String insert = "Insert into " + StructureDB.TABLE_MESSAGE + " (" + StructureDB.MESSAGE_GROUP_ID + ", "
				+ StructureDB.MESSAGE + ", " + StructureDB.MESSAGE_USER_ID + ", " + StructureDB.MESSAGE_DATE_TIME + ") "
				+ " values (" + msg.getGroupID() + ", '" + msg.getMessage() + "', " + msg.getUserID() + ", '"
				+ DateTimeUtils.formatDateToStringDB(msg.getDate()) + "') ";

		int rowCount = 0;
		try {
			rowCount = mStatement.executeUpdate(insert);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(insert);
		}
		System.out.println("Row Count affected = " + rowCount);
	}

	public void insertUser(User user) {
		String insert = "Insert into " + StructureDB.TABLE_USERS + " (" + StructureDB.USERNAME + ", "
				+ StructureDB.PASSWORD + ", " + StructureDB.ONLINE + ") " + " values ('" + user.getUsername() + "', '"
				+ user.getPassword() + "', " + user.isOnline() + ") ";

		int rowCount = 0;
		try {
			rowCount = mStatement.executeUpdate(insert);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(insert);
		}
		System.out.println("Row Count affected = " + rowCount);
	}

	public void insertGroup(Group group) {
		String insert = "Insert into " + StructureDB.TABLE_GROUPS + " (" + StructureDB.GROUP_NAME + ", "
				+ StructureDB.GROUP_USER_ID_CREATED + ", " + StructureDB.GROUP_LIST_USERS + ") " + " values ('"
				+ group.getName() + "', " + group.getUserIDCreated() + ", '" + group.getListUserIDStr() + "')";

		int rowCount = 0;
		try {
			rowCount = mStatement.executeUpdate(insert);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(insert);
		}
		System.out.println("Row Count affected = " + rowCount);
	}

	public void insertRelationship(Relationship relationship) {
		String insert = "Insert into " + StructureDB.TABLE_RELATIONSHIP + " (" + StructureDB.RELATIONSHIP_USER_ID + ", "
				+ StructureDB.RELATIONSHIP_LIST_FRIENDS + ", " + StructureDB.RELATIONSHIP_LIST_GROUPS + ") "
				+ " values (" + relationship.getUserID() + ", '" + relationship.getUserIDStr() + "', '"
				+ relationship.getListGroupsIDStr() + "')";

		int rowCount = 0;
		try {
			rowCount = mStatement.executeUpdate(insert);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(insert);
		}
		System.out.println("Row Count affected = " + rowCount);
	}
}

package mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import org.json.JSONObject;

import pojo.Group;
import pojo.Message;
import pojo.Relationship;
import pojo.User;
import utils.DateTimeUtils;
import utils.JSONUtils;

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

	public boolean getUserOnline(int tmpId) {
		boolean result = false;
		String query = "Select online From users where user_id = " + tmpId;
		ResultSet rs = null;
		try {
			rs = mStatement.executeQuery(query);
			while (rs.next()) {
				result = rs.getBoolean(StructureDB.ONLINE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	public List<Message> getMessagesByGroupID(int groupID) {
		List<Message> result = new ArrayList<>();

		String query = "Select * from " + StructureDB.TABLE_MESSAGE + " where " + StructureDB.MESSAGE_GROUP_ID + " = "
				+ groupID;
		ResultSet rs = null;
		try {
			rs = mStatement.executeQuery(query);
			while (rs.next()) {
				String message = rs.getString(StructureDB.MESSAGE);
				int userID = rs.getInt(StructureDB.MESSAGE_USER_ID);
				boolean isFile = rs.getBoolean(StructureDB.MESSAGE_IS_FILE);
				String sender = rs.getString(StructureDB.MESSAGE_SENDER);

				Message msg = new Message(groupID, userID, sender, message, isFile);

				if (isFile && isImage(groupID, message)) {
					File image = new File(getDirectorySaveFiles() + groupID);
					msg.setImage(image);
				}
				result.add(msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}

		return result;
	}

	public boolean isImage(int groupId, String fileName) {
		File folder = new File(getDirectorySaveFiles() + groupId);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile() && fileName.equals(file.getName())) {
					String mimetype = new MimetypesFileTypeMap().getContentType(file);
					String type = mimetype.split("/")[0];
					if (type.equals("image"))
						return true;
				}
			}
		}
		return false;
	}

	public String getUserName() {
		String username = System.getProperty("user.name");
		return username;
	}

	private String getDirectorySaveFiles() {
		return "C:\\Users\\" + getUserName() + "\\Documents\\" + "ChatApplication\\";
	}

	public List<Group> getListGroupsByID(String listGroupsIDStr) {
		List<Group> result = new ArrayList<Group>();
		if (!listGroupsIDStr.equals("")) {
			try {
				ResultSet rs = mStatement.executeQuery(MySQLQuery.GET_LIST_GROUP + listGroupsIDStr + ")");
				while (rs.next()) {

					int id = rs.getInt(StructureDB.GROUP_ID);
					String name = rs.getString(StructureDB.GROUP_NAME);
					int userIDCreated = rs.getInt(StructureDB.GROUP_USER_ID_CREATED);
					boolean isChatGroup = rs.getBoolean(StructureDB.GROUP_IS_CHAT_GROUP);
					String usersStr = rs.getString(StructureDB.GROUP_LIST_USERS);

					List<Integer> listUsersID = addAllIDs(usersStr);
					Group group = new Group(id, name, userIDCreated, isChatGroup, listUsersID);

					result.add(group);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public List<User> getListFriendsByID(String friendIdsStr) {
		List<User> result = new ArrayList<User>();
		if (!friendIdsStr.equals("")) {
			try {
				ResultSet rs = mStatement.executeQuery(MySQLQuery.GET_LIST_INFO_USER_BY_USERID + friendIdsStr + ")");
				while (rs.next()) {
					int id = rs.getInt(StructureDB.USER_ID);
					String fullname = rs.getString(StructureDB.USER_FULLNAME);
					boolean online = rs.getBoolean(StructureDB.ONLINE);

					result.add(new User(id, fullname, online));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public User checkValidUsername(String username) {
		PreparedStatement preStatement = null;
		User user = null;

		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_CLIENT_BY_USERNAME);
			preStatement.setString(1, username);
			ResultSet result = preStatement.executeQuery();

			while (result.next()) {
				user = new User(result.getInt(1), result.getString(2), result.getString(3), result.getString(4),
						result.getBoolean(5));
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
		return user;
	}

	public Group getGroupByID(int id) {
		Group result = null;

		String query = "Select * from " + StructureDB.TABLE_GROUPS + " where " + StructureDB.GROUP_ID + " = " + id;
		try {
			ResultSet rs = mStatement.executeQuery(query);
			while (rs.next()) {
				String name = rs.getString(StructureDB.GROUP_NAME);
				int userIDCreated = rs.getInt(StructureDB.GROUP_USER_ID_CREATED);
				boolean isChatGroup = rs.getBoolean(StructureDB.GROUP_IS_CHAT_GROUP);
				String usersStr = rs.getString(StructureDB.GROUP_LIST_USERS);

				List<Integer> listUsersID = addAllIDs(usersStr);

				result = new Group(id, name, userIDCreated, isChatGroup, listUsersID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isExistGroupName(Group group) {

		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_ALL_GROUP);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				String fullname = rs.getString(StructureDB.GROUP_NAME);
				boolean isChatGroup = rs.getBoolean(StructureDB.GROUP_IS_CHAT_GROUP);

				if (isChatGroup && group.getName().equals(fullname)) {
					return true;
				}
			}
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public User getUser(int id) {
		User result = null;
		String query = "Select * from " + StructureDB.TABLE_USERS + " where " + StructureDB.USER_ID + " = " + id;
		try {
			ResultSet rs = mStatement.executeQuery(query);
			while (rs.next()) {
				String fullname = rs.getString(StructureDB.USER_FULLNAME);
				boolean online = rs.getBoolean(StructureDB.ONLINE);

				result = new User(id, fullname, online);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}
		return result;
	}

	public Relationship getRelationshipByUserID(int userId) {
		Relationship result = null;
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_RELATIONSHIP_BY_USERID);
			preStatement.setInt(1, userId);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				String friendsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_FRIENDS);
				String groupsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_GROUPS);

				System.out.println("----------getRelationshipByUserID----------");
				List<Integer> listFriendsID = addAllIDs(friendsStr);
				List<Integer> listGroupsID = addAllIDs(groupsStr);

				result = new Relationship(userId, listFriendsID, listGroupsID);
				System.out.println(
						"getRelationshipByUserID: " + result.getUserIDStr() + " - " + result.getListGroupsIDStr());
			}
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean updateStatusOnline(int userId, boolean online) {
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.UPDATE_STATUS_ONLINE);
			preStatement.setInt(1, (online ? 1 : 0));
			preStatement.setInt(2, userId);
			preStatement.executeUpdate();
			preStatement.close();
			System.out.println("updateStatusOnline: " + userId + " - " + online);

			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void addNewUser(String username, String password, String fullName) {
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.INSERT_NEW_USER);
			preStatement.setString(1, username);
			preStatement.setString(2, password);
			preStatement.setString(3, fullName);
			preStatement.setInt(4, 1);
			preStatement.executeUpdate();
			preStatement.close();
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

	public void insertMessage(int groupId, int senderId, String sender, boolean isFile, String msg, Date date) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.INSERT_NEW_MESSAGE);
			preparedStatement.setInt(1, groupId);
			preparedStatement.setString(2, msg);
			preparedStatement.setInt(3, senderId);
			preparedStatement.setString(4, DateTimeUtils.formatDateToStringDB(date));
			preparedStatement.setBoolean(5, isFile);
			preparedStatement.setString(6, sender);

			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertGroup(Group group) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.INSERT_GROUP);
			preparedStatement.setString(1, group.getName());
			preparedStatement.setInt(2, group.getUserIDCreated());
			preparedStatement.setBoolean(3, group.isChatGroup());
			preparedStatement.setString(4, group.getListUserIDStr());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void insertRelationship(Relationship relationship) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.INSERT_RELATIONSHIP);
			preparedStatement.setInt(1, relationship.getUserID());
			preparedStatement.setString(2, relationship.getUserIDStr());
			preparedStatement.setString(3, relationship.getListGroupsIDStr());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private List<Integer> addAllIDs(String idsStr) {
		List<Integer> result = new ArrayList<>();
		if (!idsStr.equals(""))
			for (String e : idsStr.split(","))
				result.add(Integer.parseInt(e));
		return result;
	}

	public void insertRequestAddFriend(int userIdRequested, int userIdReceive) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.INSERT_NEW_REQUEST);
			preparedStatement.setInt(1, userIdRequested);
			preparedStatement.setInt(2, userIdReceive);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public int getGroupIdByGroupName(Group gr) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.GET_GROUP_ID_BY_GROUP_NAME);
			preparedStatement.setString(1, gr.getName());
			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				return result.getInt(StructureDB.GROUP_ID);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	public int getGroupIdByListUsers(String listUserId) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.GET_GROUP_ID_BY_LIST_USERS);
			preparedStatement.setString(1, listUserId);
			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				return result.getInt(StructureDB.GROUP_ID);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	public void updateRelationship(Relationship newRelationship) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.UPDATE_RELATIONSHIP);
			preparedStatement.setString(1, newRelationship.getUserIDStr());
			preparedStatement.setString(2, newRelationship.getListGroupsIDStr());
			preparedStatement.setInt(3, newRelationship.getUserID());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public boolean deleteRequested(int userIdRequested, int userIdReceived) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.DELETE_REQUESTED);
			preparedStatement.setInt(1, userIdRequested);
			preparedStatement.setInt(2, userIdReceived);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	public String getAllRequests(int userIdReceived) {
		PreparedStatement preparedStatement = null;
		List<User> users = new ArrayList<>();
		try {
			System.out.println(MySQLQuery.GET_ALL_REQUESTED);
			preparedStatement = mConnection.prepareStatement(MySQLQuery.GET_ALL_REQUESTED);
			preparedStatement.setInt(1, userIdReceived);
			ResultSet result = preparedStatement.executeQuery();
			while (result.next()) {
				User user = new User(result.getInt(StructureDB.USER_ID), result.getString(StructureDB.USER_FULLNAME));
				users.add(user);
			}
			if (users.size() > 0) {
				JSONObject jsonObject = JSONUtils.createAllRequestUsetObject(users);
				return jsonObject.toString();
			} else {
				return JSONUtils.createEmptyJSONObject().toString();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public List<User> getAllUser(int mId) {
		List<User> result = new ArrayList<>();
		String friendIds = getFriendIds(mId);
		System.out.println("getAllUser - friend_ids: " + friendIds);

		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_ALL_USER);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(StructureDB.USER_ID);
				String fullname = rs.getString(StructureDB.USER_FULLNAME);
				boolean online = rs.getBoolean(StructureDB.ONLINE);

				if (!friendIds.contains("" + id) && id != mId) {
					User user = new User(id, fullname, online);
					result.add(user);
				}
			}
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getFriendIds(int mId) {
		String result = "";
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_LIST_FRIEND_IDS_BY_USERID);
			preStatement.setInt(1, mId);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				result = rs.getString(StructureDB.RELATIONSHIP_LIST_FRIENDS);
			}
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}

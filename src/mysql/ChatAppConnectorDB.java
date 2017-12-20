package mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import utils.DateTimeUtils;

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

				Message msg = new Message(groupID, userID, message, isFile);
				result.add(msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(query);
		}

		return result;
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
			preStatement = mConnection.prepareStatement(MySQLQuery.GET_LIST_FRIENDS_BY_USERID);
			preStatement.setInt(1, userId);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				String friendsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_FRIENDS);
				String groupsStr = rs.getString(StructureDB.RELATIONSHIP_LIST_GROUPS);

				System.out.println("----------getRelationshipByUserID----------");
				List<Integer> listFriendsID = addAllIDs(friendsStr);
				List<Integer> listGroupsID = addAllIDs(groupsStr);

				result = new Relationship(userId, listFriendsID, listGroupsID);
			}
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void updateStatusOnline(String username, boolean online) {
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.UPDATE_STATUS_ONLINE);
			preStatement.setInt(1, online ? 1 : 0);
			preStatement.setString(2, username);
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

	public boolean updateStatusOnline(int userId, boolean online) {
		PreparedStatement preStatement = null;
		try {
			preStatement = mConnection.prepareStatement(MySQLQuery.UPDATE_STATUS_ONLINE_BY_USER_ID);
			preStatement.setInt(1, online ? 1 : 0);
			preStatement.setInt(2, userId);
			preStatement.executeUpdate();
			preStatement.close();
			
			return true;
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

	public void insertMessage(int groupId, int senderId, boolean isFile,String msg, Date date) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = mConnection.prepareStatement(MySQLQuery.INSERT_NEW_MESSAGE);
			preparedStatement.setInt(1, groupId);
			preparedStatement.setBoolean(2, isFile);
			preparedStatement.setString(3, msg);
			preparedStatement.setInt(4, senderId);
			preparedStatement.setString(5, DateTimeUtils.formatDateToStringDB(date));

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
}

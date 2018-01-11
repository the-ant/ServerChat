package mysql;

public class MySQLQuery {

	public static final String GET_CLIENT_BY_USERNAME = "SELECT * FROM " + StructureDB.TABLE_USERS + " WHERE "
			+ StructureDB.USERNAME + " = ?";

	public static final String UPDATE_STATUS_ONLINE = "UPDATE " + StructureDB.TABLE_USERS + " SET " + StructureDB.ONLINE
			+ " = ? WHERE " + StructureDB.USER_ID + " = ?";

	public static final String GET_RELATIONSHIP_BY_USERID = "SELECT * FROM " + StructureDB.TABLE_RELATIONSHIPS
			+ " WHERE " + StructureDB.RELATIONSHIP_USER_ID + " = ?";

	public static final String GET_LIST_FRIEND_IDS_BY_USERID = "SELECT " + StructureDB.RELATIONSHIP_LIST_FRIENDS
			+ " FROM " + StructureDB.TABLE_RELATIONSHIPS + " WHERE " + StructureDB.RELATIONSHIP_USER_ID + " = ?";

	public static final String GET_LIST_INFO_USER_BY_USERID = "SELECT " + StructureDB.USER_ID + ","
			+ StructureDB.USER_FULLNAME + "," + StructureDB.ONLINE + " FROM " + StructureDB.TABLE_USERS + " WHERE "
			+ StructureDB.USER_ID + " IN (";

	public static final String GET_LIST_GROUP = "SELECT * FROM " + StructureDB.TABLE_GROUPS + " WHERE "
			+ StructureDB.GROUP_ID + " IN (";

	public static final String GET_ALL_USER = "Select * from " + StructureDB.TABLE_USERS;

	public static final String GET_ONLINE_BY_USERID = "Select " + StructureDB.ONLINE + " FROM "
			+ StructureDB.TABLE_USERS + " WHERE " + StructureDB.USER_ID + " = ?";

	public static final String GET_USER_FROM_USERNAME_AND_PASSWORD = "Select * from " + StructureDB.TABLE_USERS
			+ " WHERE " + StructureDB.USERNAME + " = ? AND " + StructureDB.PASSWORD + " = ? ";

	public static final String INSERT_NEW_USER = "INSERT INTO " + StructureDB.TABLE_USERS + "(" + StructureDB.USERNAME
			+ "," + StructureDB.PASSWORD + "," + StructureDB.USER_FULLNAME + "," + StructureDB.ONLINE
			+ ") VALUES (?,?,?,?)";

	public static final String INSERT_NEW_MESSAGE = "INSERT INTO  " + StructureDB.TABLE_MESSAGE + "("
			+ StructureDB.MESSAGE_GROUP_ID + "," + StructureDB.MESSAGE + "," + StructureDB.MESSAGE_USER_ID + ","
			+ StructureDB.MESSAGE_DATE_TIME + "," + StructureDB.MESSAGE_IS_FILE + "," + StructureDB.MESSAGE_SENDER
			+ ") VALUES(?,?,?,?,?,?)";

	public static final String INSERT_RELATIONSHIP = "INSERT INTO  " + StructureDB.TABLE_RELATIONSHIPS + "("
			+ StructureDB.RELATIONSHIP_USER_ID + "," + StructureDB.RELATIONSHIP_LIST_FRIENDS + ","
			+ StructureDB.RELATIONSHIP_LIST_GROUPS + ") VALUES(?,?,?)";

	public static final String INSERT_GROUP = "INSERT INTO  " + StructureDB.TABLE_GROUPS + "(" + StructureDB.GROUP_NAME
			+ "," + StructureDB.GROUP_USER_ID_CREATED + "," + StructureDB.GROUP_IS_CHAT_GROUP + ","
			+ StructureDB.GROUP_LIST_USERS + ") VALUES(?,?,?,?)";

	public static final String INSERT_NEW_REQUEST = "INSERT INTO " + StructureDB.TABLE_WAITING_REQUESTS + "("
			+ StructureDB.USERID_REQUESTED + "," + StructureDB.USERID_RECEIVE + ") VALUES (?,?)";

	public static final String DELETE_REQUESTED = "DELETE FROM " + StructureDB.TABLE_WAITING_REQUESTS + " WHERE "
			+ StructureDB.USERID_REQUESTED + " = ? AND " + StructureDB.USERID_RECEIVE + " = ?";

	public static final String GET_ALL_REQUESTED = "SELECT " + StructureDB.USER_ID + ", " + StructureDB.USER_FULLNAME
			+ " FROM " + StructureDB.TABLE_USERS + " WHERE " + StructureDB.USER_ID + " IN (SELECT "
			+ StructureDB.USERID_REQUESTED + " FROM " + StructureDB.TABLE_WAITING_REQUESTS + " WHERE "
			+ StructureDB.USERID_RECEIVE + " = ?)";

	public static final String GET_GROUP_ID_BY_LIST_USERS = "SELECT " + StructureDB.GROUP_ID + " FROM "
			+ StructureDB.TABLE_GROUPS + " WHERE " + StructureDB.GROUP_LIST_USERS + " = ? ";

	public static final String GET_GROUP_ID_BY_GROUP_NAME = "SELECT " + StructureDB.GROUP_ID + " FROM "
			+ StructureDB.TABLE_GROUPS + " WHERE " + StructureDB.GROUP_NAME + " = ? ";

	public static final String UPDATE_RELATIONSHIP = "UPDATE " + StructureDB.TABLE_RELATIONSHIPS + " SET "
			+ StructureDB.RELATIONSHIP_LIST_FRIENDS + " = ?, " + StructureDB.RELATIONSHIP_LIST_GROUPS + " = ? WHERE "
			+ StructureDB.RELATIONSHIP_USER_ID + " = ? ";

	public static final String GET_ALL_GROUP = "Select * from " + StructureDB.TABLE_GROUPS;
}
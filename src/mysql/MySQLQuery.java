package mysql;

public class MySQLQuery {

	public static final String GET_CLIENT_BY_USERNAME = "SELECT * FROM users where username = ?";
	
	public static final String UPDATE_STATUS_ONLINE = "UPDATE users SET online = ? where username = ?";
	
	public static final String GET_LIST_FRIENDS_BY_USERID = "SELECT * FROM relationship where user_id = ?";
	
	public static final String GET_LIST_INFO_USER_BY_USERID = "SELECT user_id, fullname, online FROM users where user_id IN (";
	
	public static final String GET_LIST_GROUP = "SELECT * FROM groups where group_id IN (";
	
	public static final String GET_USER_FROM_USERNAME_AND_PASSWORD = "Select * from users where username = ? and password = ?";
	
	public static final String INSERT_NEW_USER = "INSERT INTO users(username,password,fullname,online) VALUES(?, ?, ?, ?)";

	public static final String INSERT_NEW_MESSAGE = "INSERT INTO  " + StructureDB.TABLE_MESSAGE + "(" 
											+ StructureDB.MESSAGE_GROUP_ID + ","
											+ StructureDB.MESSAGE + ","
											+ StructureDB.MESSAGE_USER_ID + ","
											+ StructureDB.MESSAGE_DATE_TIME 
							+ ") VALUES(?,?,?,?)";

	public static final String INSERT_RELATIONSHIP = "INSERT INTO  " + StructureDB.TABLE_RELATIONSHIP + "(" 
											+ StructureDB.RELATIONSHIP_USER_ID + ","
											+ StructureDB.RELATIONSHIP_LIST_FRIENDS + ","
											+ StructureDB.RELATIONSHIP_LIST_GROUPS
							+ ") VALUES(?,?,?)";

	public static final String INSERT_GROUP = "INSERT INTO  " + StructureDB.TABLE_GROUPS + "(" 
											+ StructureDB.GROUP_NAME + ","
											+ StructureDB.GROUP_USER_ID_CREATED + ","
											+ StructureDB.GROUP_IS_CHAT_GROUP + ","
											+ StructureDB.GROUP_LIST_USERS
							+ ") VALUES(?,?,?,?)";



}

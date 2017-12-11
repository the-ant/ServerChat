package mysql;

public class StructureDB {

	/*
	 * Table Message
	 */
	public static final String TABLE_MESSAGE = "message";
	public static final String MESSAGE_ID = "message_id";
	public static final String MESSAGE_GROUP_ID = "group_id";
	public static final String MESSAGE = "message";
	public static final String MESSAGE_USER_ID = "user_id";
	public static final String MESSAGE_DATE_TIME= "date_time";

	/*
	 * Table Groups
	 */
	public static final String TABLE_GROUPS = "groups";
	public static final String GROUP_ID = "group_id";
	public static final String GROUP_NAME = "group_name";
	public static final String GROUP_USER_ID_CREATED = "user_id_created";
	public static final String GROUP_IS_CHAT_GROUP = "is_chat_group";
	public static final String GROUP_LIST_USERS = "list_users";

	/*
	 * Table Relationship
	 */
	public static final String TABLE_RELATIONSHIP = "relationship";
	public static final String RELATIONSHIP_ID = "relationship_id";
	public static final String RELATIONSHIP_USER_ID = "user_id";
	public static final String RELATIONSHIP_LIST_FRIENDS = "list_friends";
	public static final String RELATIONSHIP_LIST_GROUPS = "list_groups";

	/*
	 * Table Users
	 */
	public static final String TABLE_USERS = "users";
	public static final String USER_ID = "user_id";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String ONLINE = "online";
}

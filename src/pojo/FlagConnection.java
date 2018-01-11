package pojo;

public class FlagConnection {

	public static final int LOGIN = 1;

	public static final int LOGOUT = LOGIN + 1;

	public static final int REGISTER = LOGOUT + 1;

	public static final int SEND_MESSAGE = REGISTER + 1;

	public static final int GET_RELATIONSHIP = SEND_MESSAGE + 1;

	public static final int GET_MESSAGE = GET_RELATIONSHIP + 1;

	public static final int GET_GROUP = GET_MESSAGE + 1;

	public static final int GET_USER = GET_GROUP + 1;

	public static final int ADD_FRIEND = GET_USER + 1;

	public static final int ADD_GROUP = ADD_FRIEND + 1;

	public static final int RECEIVE_MESSAGE = ADD_GROUP + 1;

	public static final int NOTIFY_ONLINE = RECEIVE_MESSAGE + 1;

	public static final int NOTIFY_LOGOUT = NOTIFY_ONLINE + 1;

	public static final int SEND_FILE = NOTIFY_LOGOUT + 1;

	public static final int DOWN_LOAD_FILE = SEND_FILE + 1;

	public static final int GET_ALL_USER = DOWN_LOAD_FILE + 1;
	
	public static final int REQUEST_ADD_FRIEND = GET_ALL_USER + 1;
	
	public static final int DELETE_REQUEST_RECORD = REQUEST_ADD_FRIEND + 1;
	
	public static final int UPDATE_REQUEST_ADD_FRIEND = DELETE_REQUEST_RECORD + 1;
	
	public static final int GET_ALL_REQUESTS = UPDATE_REQUEST_ADD_FRIEND + 1;
	
	public static final int UPDATE_RELATIONSHIP = GET_ALL_REQUESTS + 1;
}

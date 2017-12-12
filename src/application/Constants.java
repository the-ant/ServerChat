package application;

public class Constants {
	/* PORT OF SERVER */
	public static final int SERVER_PORT = 5151;
	/* MYSQL QUERY*/
	public static final String GET_CLIENT_BY_USERNAME = "SELECT * FROM users where username = ?";
	public static final String UPDATE_STATUS_ONLINE = "UPDATE users SET online = ? where username = ?";
	public static final String INSERT_NEW_USER = "INSERT INTO users(username,password,online, fullname) VALUES(?, ?, ?, ?)";
	public static final String GET_LIST_FRIENDS_BY_USERID = "SELECT * FROM relationship where user_id = ?";
	public static final String GET_LIST_INFO_USER_BY_USERID = "SELECT fullname, online FROM users where user_id IN (";
	public static final String GET_USER_FROM_USERNAME_AND_PASSWORD = "Select * from users where username = ? and password = ?";
	/* ALERT */
	public static final String ALERT_INVALID_USERNAME = "Username is invalid";
	public static final String ALERT_EXIST_USERNAME = "That username is taken. Try another!";
	public static final String ALERT_EMPTY_STRING = "Your can't leave this empty.";
}

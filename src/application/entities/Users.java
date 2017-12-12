package application.entities;

public class Users {
	private int userId;
	private String username;
	private String password;
	private int online;
	private String fullName;
	
	public Users(int userId, String username, String password, int online, String fullName) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.online = online;
		this.fullName = fullName;
	}
	
	public Users(int userId, String fullname, int online) {
		this.userId = userId;
		this.fullName = fullname;
		this.online = online;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public String toString() {
		return userId + " - " + username + " - " + password + " - " + online;
	}
}

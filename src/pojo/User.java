package pojo;

public class User {

	private int id;
	private String username;
	private String password;
	private String fullname;
	private boolean online;

	public User() {
	}

	public User(int id, String username, String password, String fullname, boolean online) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.setFullname(fullname);
		this.online = online;
	}

	public User(String username, String password, boolean online) {
		this.username = username;
		this.password = password;
		this.online = online;
	}

	public User(int id, String fullname, boolean online) {
		this.id = id;
		this.fullname = fullname;
		this.online = online;
	}

	public User(int id, String fullname) {
		this.setId(id);
		this.setFullname(fullname);
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isOnline() {
		return online;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

}

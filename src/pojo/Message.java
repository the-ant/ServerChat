package pojo;

import java.util.Date;

public class Message {

	private int id;
	private int groupID;
	private int userID;
	private String message;
	private Date date;

	public Message() {
	}

	public Message(int id, int groupID, int userID, String message, Date date) {
		this.id = id;
		this.groupID = groupID;
		this.userID = userID;
		this.message = message;
		this.date = date;
	}

	public Message(int groupID, int userID, String message, Date date) {
		this.groupID = groupID;
		this.userID = userID;
		this.message = message;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public int getGroupID() {
		return groupID;
	}

	public int getUserID() {
		return userID;
	}

	public String getMessage() {
		return message;
	}

	public Date getDate() {
		return date;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}

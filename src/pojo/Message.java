package pojo;

import java.io.File;
import java.util.Date;

public class Message {

	private int id;
	private int groupID;
	private int userID;
	private String message;
	private String sender;
	private Date date;
	private boolean isMe = false;
	private boolean isFile = false;
	private File image;

	public Message(int groupID, int userID, String message, boolean isMe, boolean isFile) {
		this.groupID = groupID;
		this.userID = userID;
		this.message = message;
		this.isMe = isMe;
		this.isFile = isFile;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public Message(int groupID, int userID, String message, boolean isFile) {
		this.groupID = groupID;
		this.userID = userID;
		this.message = message;
		this.isFile = isFile;
	}

	public Message(int groupID, int userID, String sender, String message, boolean isFile) {
		this.groupID = groupID;
		this.userID = userID;
		this.message = message;
		this.isFile = isFile;
		this.sender = sender;
	}

	public Message() {
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

	public boolean isMe() {
		return isMe;
	}

	public void setMe(boolean isMe) {
		this.isMe = isMe;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}
}

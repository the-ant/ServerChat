package pojo;

import java.util.List;

public class Group {

	private int id;
	private String name;
	private int userIDCreated;
	private boolean isChatGroup;
	private List<Integer> listUserID;

	public Group() {
	}

	public Group(int id, String name, int userIDCreated, boolean isChatGroup, List<Integer> listUserID) {
		this.id = id;
		this.name = name;
		this.userIDCreated = userIDCreated;
		this.isChatGroup = isChatGroup;
		this.listUserID = listUserID;
	}

	public Group(String name, int userIDCreated, List<Integer> listUserID) {
		this.name = name;
		this.userIDCreated = userIDCreated;
		this.listUserID = listUserID;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getUserIDCreated() {
		return userIDCreated;
	}

	public List<Integer> getListUserID() {
		return listUserID;
	}

	public String getListUserIDStr() {
		String result = "";
		for (Integer i : listUserID) {
			result += i + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUserIDCreated(int userIDCreated) {
		this.userIDCreated = userIDCreated;
	}

	public void setListUserID(List<Integer> listUserID) {
		this.listUserID = listUserID;
	}

	public boolean isChatGroup() {
		return isChatGroup;
	}

	public void setChatGroup(boolean isChatGroup) {
		this.isChatGroup = isChatGroup;
	}

}

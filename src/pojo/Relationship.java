package pojo;

import java.util.List;

public class Relationship {

	private int id;
	private int userID;
	private List<Integer> listFriendsID;
	private List<Integer> listGroupsID;

	public Relationship() {
	}

	public Relationship(int id, int userID, List<Integer> listFriendsID, List<Integer> listGroupsID) {
		this.id = id;
		this.userID = userID;
		this.listFriendsID = listFriendsID;
		this.listGroupsID = listGroupsID;
	}

	public Relationship(int userID, List<Integer> listFriendsID, List<Integer> listGroupsID) {
		this.userID = userID;
		this.listFriendsID = listFriendsID;
		this.listGroupsID = listGroupsID;
	}

	public int getId() {
		return id;
	}

	public int getUserID() {
		return userID;
	}

	public List<Integer> getListFriendsID() {
		return listFriendsID;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getUserIDStr() {
		String result = "";
		for (Integer i : listFriendsID) {
			result += i + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	public void setListFriendsID(List<Integer> listFriendsID) {
		this.listFriendsID = listFriendsID;
	}

	public List<Integer> getListGroupsID() {
		return listGroupsID;
	}

	public String getListGroupsIDStr() {
		String result = "";
		for (Integer i : listGroupsID) {
			result += i + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	public void setListGroupsID(List<Integer> listGroupsID) {
		this.listGroupsID = listGroupsID;
	}

}

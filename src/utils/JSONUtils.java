package utils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import mysql.StructureDB;
import pojo.Group;
import pojo.Message;
import pojo.User;

public class JSONUtils {

	public static JSONObject createMessagesObj(List<Message> msgs) {
		JSONObject obj = new JSONObject();
		JSONArray friendsArray = createMessagesArray(msgs);
		obj.put("messages", friendsArray);
		return obj;
	}
	
	private static JSONArray createMessagesArray(List<Message> msgs) {
		JSONArray result = new JSONArray();
		for (Message msg : msgs) {
			result.put(createMessageJSONObj(msg));
		}
		return result;
	}

	private static JSONObject createMessageJSONObj(Message msg) {
		JSONObject item = new JSONObject();
		item.put(StructureDB.MESSAGE_ID, msg.getId());
		item.put(StructureDB.MESSAGE_GROUP_ID, msg.getGroupID());
		item.put(StructureDB.MESSAGE, msg.getMessage());
		item.put(StructureDB.MESSAGE_USER_ID, msg.getUserID());
		item.put(StructureDB.MESSAGE_IS_FILE, msg.isFile());
		return item;
	}

	public static JSONObject createRelationshipObj(List<User> listFriends, List<Group> listGroups) {
		JSONObject obj = new JSONObject();
		JSONArray friendsArray = createFriendsArray(listFriends);
		JSONArray groupsArray = createGroupsArray(listGroups);
		obj.put("friends", friendsArray);
		obj.put("groups", groupsArray);
		return obj;
	}
	
	public static JSONArray createGroupsArray(List<Group> listGroups) {
		JSONArray array = new JSONArray();
		for (Group group : listGroups) {
			array.put(createGroupJSONObj(group));
		}
		return array;
	}

	public static JSONObject createGroupJSONObj(Group group) {
		JSONObject item = new JSONObject();
		item.put(StructureDB.GROUP_ID, group.getId());
		item.put(StructureDB.GROUP_NAME, group.getName());
		item.put(StructureDB.GROUP_USER_ID_CREATED, group.getUserIDCreated());
		item.put(StructureDB.GROUP_IS_CHAT_GROUP, group.isChatGroup());
		item.put(StructureDB.GROUP_LIST_USERS, group.getListUserIDStr());
		return item;
	}

	public static JSONArray createFriendsArray(List<User> listFriends) {
		JSONArray array = new JSONArray();
		for (User user : listFriends) {
			array.put(createUserJSONObj(user));
		}
		return array;
	}

	public static JSONObject createUserJSONObj(User user) {
		JSONObject item = new JSONObject();
		item.put(StructureDB.USER_ID, user.getId());
		item.put(StructureDB.USER_FULLNAME, user.getFullname());
		item.put(StructureDB.ONLINE, user.isOnline());
		return item;
	}

	public static JSONObject createEmptyJSONObject() {
		JSONObject obj = new JSONObject();
		JSONArray friendsArray = new JSONArray();
		JSONArray groupsArray = new JSONArray();
		obj.put("friends", friendsArray);
		obj.put("groups", groupsArray);
		return obj;
	}
}
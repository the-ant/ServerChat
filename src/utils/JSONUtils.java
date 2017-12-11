package utils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import mysql.StructureJSON;
import pojo.Group;
import pojo.User;

public class JSONUtils {

	public static JSONObject createJSONObject(List<User> listFriends, List<Group> listGroups) {
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
		item.put(StructureJSON.GROUP_ID, group.getId());
		item.put(StructureJSON.GROUP_NAME, group.getName());
		item.put(StructureJSON.GROUP_USER_ID_CREATED, group.getUserIDCreated());
		item.put(StructureJSON.GROUP_IS_CHAT_GROUP, group.isChatGroup());
		item.put(StructureJSON.GROUP_LIST_USERS, group.getListUserIDStr());
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
		item.put(StructureJSON.USER_ID, user.getId());
		item.put(StructureJSON.USERNAME, user.getUsername());
		item.put(StructureJSON.ONLINE, user.isOnline());
		return item;
	}
}

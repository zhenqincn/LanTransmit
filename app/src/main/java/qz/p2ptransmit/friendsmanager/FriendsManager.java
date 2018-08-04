/*
 * 好友信息的存储格式为：
 * 键：姓名     值：ip/tcp接收文件端口号
 * 存储在friends中
 *
 *
 *
 *
 */

package qz.p2ptransmit.friendsmanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FriendsManager {
	private LinkedHashMap<String, String> friends;
	private List<String> onlineUsername;
	
	public FriendsManager()
	{
		friends = new LinkedHashMap<String, String>();
		onlineUsername = new ArrayList<String>();
	}


	public String getFriendInfo(String name) {              //通过返回的值来指定键映射，获取name的信息
        return friends.get(name);
    }


	public void removeFriend(String name) {                 //将一个对象从哈希图中移除
        friends.remove(name);
		for(int i = 0; i < onlineUsername.size(); i++)
		{
			if(name.equals(onlineUsername.get(i)))
			{
				onlineUsername.remove(i);
				break;
			}
		}
    }


	public void addFriend(String name, String info)
	{
		if(!onlineUsername.contains(name))
		{
			onlineUsername.add(name);
			friends.put(name, info);
		}
	}


	public List<String> getOnlineNames()
	{
		return onlineUsername;
	}

}

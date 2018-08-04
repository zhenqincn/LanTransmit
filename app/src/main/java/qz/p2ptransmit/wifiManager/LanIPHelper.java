package qz.p2ptransmit.wifiManager;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


// =====================
// 获取当前设备所处网络环境的ip地址
//

public class LanIPHelper {
	public static String getLocalIpAddress(boolean isApState, Context context){
		//热点模式下主机默认ip为192.168.43.1
		//因为热点模式下通过常规方式获取ip会获得0.0.0.0
		if(isApState)
		{
			return "192.168.43.1";
		}
		//非热点模式，通过WifiManager获取int型的当前ip地址，再通过位运算将int型转为字符串类型
		WifiManager wifiManager = (WifiManager) (context.getSystemService(Context.WIFI_SERVICE));
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int i = wifiInfo.getIpAddress();
		return (i & 0xFF) + "." +
				((i >> 8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF)+ "." +
				((i >> 24 ) & 0xFF );
	}
}

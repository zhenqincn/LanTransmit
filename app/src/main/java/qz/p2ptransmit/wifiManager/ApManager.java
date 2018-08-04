package qz.p2ptransmit.wifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class ApManager {
    public static boolean isWifiApEnabled(WifiManager wifiManager)
    {
    	try
    	{
    		Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
    		method.setAccessible(true);
    		return (Boolean)method.invoke(wifiManager);
    	}catch (NoSuchMethodException e){
    		e.printStackTrace();
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }
    
    
    public static String closeWifiAp(WifiManager wifiManager)
    {
        try {  
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");  
            method.setAccessible(true);  
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);  
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);  
            method2.invoke(wifiManager, config, false);  
        } catch (NoSuchMethodException e) {  
        	return e.toString();    
        } catch (IllegalArgumentException e) {  
        	return e.toString();  
        } catch (IllegalAccessException e) {  
        	return e.toString();   
        } catch (InvocationTargetException e) {  
            return e.toString();  
        }  
    	return "Success";
    }
}

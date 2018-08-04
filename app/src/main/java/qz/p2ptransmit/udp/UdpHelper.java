package qz.p2ptransmit.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpHelper {
	private int port = 9888;
	private DatagramSocket ds = null;
    private DatagramPacket dp = null;
    private byte[] buf = new byte[1024];   //存储发来的消息
    private String host;                   //广播地址
    
    public UdpHelper(String LocalIP) {
    	try {
			ds = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	    dp = new DatagramPacket(buf, buf.length);
	    String[] IP = LocalIP.split("\\.");         //注意此处的转义字符!!!!!!
	    host = IP[0] + "." + IP[1] + "." + IP[2] + "." + "255";
    }
    
    public String SendMessage(String msg) {
        String message = msg;
        try {
            InetAddress adds = InetAddress.getByName(host);
            DatagramSocket ds = new DatagramSocket();
            byte[] byte_message = message.getBytes("UTF-8");
            DatagramPacket dp = new DatagramPacket(byte_message,
            		byte_message.length, adds, port);
            ds.send(dp);
            ds.close();
        } catch (UnknownHostException e) {
        	return e.getMessage();
        } catch (SocketException e) {
        	return e.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
        return "Success";
    }
    
    public String ReceiveMessage()
    {
    	String RecMsg;
    	try {
			ds.receive(dp);
			RecMsg = new String(dp.getData(),0,dp.getLength(),"UTF-8");
            buf = new byte[1024];
            dp = new DatagramPacket(buf, buf.length);
		} catch (IOException e) {
			return e.getMessage();
		}
    	return RecMsg;
    }

}
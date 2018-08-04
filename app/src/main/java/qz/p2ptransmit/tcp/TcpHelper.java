package qz.p2ptransmit.tcp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
/*
 * 
 * 
 * 
 * 9876端口监听TCP消息，传输用户信息以及操作请求，连接超时时间为1000ms
 */
public class TcpHelper {
	private ServerSocket server;
	private static String TAG = "TcpHelper";
	public TcpHelper(int port)
	{
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TcpHelper()
	{

	}

	public String Receive()
	{
		String message = "";
		try{
			Socket rec = server.accept();
			InputStream nameStream = rec.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(nameStream);
			BufferedReader br = new BufferedReader(streamReader);
			message = br.readLine();
			message = URLDecoder.decode(message, "UTF-8");
			br.close();
			streamReader.close();
			nameStream.close();
			rec.close();
		}catch(Exception e){
			return "接收TCP消息错误:\n" + e.getMessage();
		}
		return message;
	}

	public String Send(String ip,String message)
	{
		try {
			Socket se = new Socket();
			se.connect(new InetSocketAddress(ip, 9876), 1000);
			OutputStream outputName = se.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
			BufferedWriter bwName = new BufferedWriter(outputWriter);
			message = URLDecoder.decode(message, "UTF-8");
			bwName.write(message);
			bwName.close();
			outputWriter.close();
			outputName.close();
			se.close();
		} catch (Exception e) {
			return "发送TCP消息错误:\n" + e.getMessage();
		}
		return "Success";
	}


	public void close()
	{
		try {
			this.server.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
	}
}
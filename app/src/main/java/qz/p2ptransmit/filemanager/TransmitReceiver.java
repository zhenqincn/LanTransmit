package qz.p2ptransmit.filemanager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Map;

import android.os.Environment;
import android.util.Log;

public class TransmitReceiver {
	private ServerSocket server;
	private Socket socket;
	private InputStream inputstream;
	private InputStreamReader streamReader;
	private BufferedReader br;
	private String fileName,SenderName;
	private double fileLength;
	public TransmitReceiver(ServerSocket server){
		this.server = server;
	}

	//接收文件名
	public void ReceiveName(Map<String,Integer> transmitProgress){
		try {
			socket = server.accept();
			this.inputstream = socket.getInputStream();
			streamReader = new InputStreamReader(this.inputstream);
			br = new BufferedReader(streamReader);
			String[] fileInfo = br.readLine().split("/");
			this.fileName = fileInfo[0];
			this.fileName = URLDecoder.decode(fileName, "UTF-8"); 
			this.SenderName = fileInfo[1];
			this.fileLength = Double.parseDouble(fileInfo[2]);
			
		} catch (IOException e) {
			Log.i("log",e.toString());
		}
	}

	//接收文件内容
	public String ReceiveData(Map<String,Integer> transmitProgress)
	{
		try{		
			String savePath = Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived/" + this.fileName;
			FileOutputStream fileOS = new FileOutputStream(savePath, false);
			byte[] buffer = new byte[1048576];
			int size = -1;
			double receiveSize = 0;         //已经接收的大小
			while ((size = this.inputstream.read(buffer)) != -1){
				fileOS.write(buffer, 0 ,size);
				receiveSize += size;
				Log.i("log","接收了" + size + "字节");
			}
			fileOS.close();
			this.br.close();
			this.streamReader.close();
			this.inputstream.close();
			new Thread()     //创建线程，判断对方是否已经完成发送并且关闭了socket
			{
				@Override
				public void run()
				{
					try {
						//通过之前建立的socket对象发送一个字节的数据
						socket.sendUrgentData(0xFF);
					} catch (IOException e) {
						//如果发送失败，说明发送方确实已经关闭了socket
						try {
							//关闭socket
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				}
			}.start();
			return "来自" + this.SenderName + "的文件" + this.fileName + "接收完成";
		}catch(Exception e){
			return "接收错误:" + e.getMessage() + "\n";
		}
	};

}

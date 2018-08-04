package qz.p2ptransmit.filemanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;

import android.util.Log;

public class TransmitSender {
	public TransmitSender()
	{
		//........
	}

	public String sendFile(String fileName, String path, String ipAddress, int port , String myName){
		try {
			File file = new File(path);
			Socket name = new Socket();
			name.connect(new InetSocketAddress(ipAddress, port), 1000);
			OutputStream outputName = name.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
			BufferedWriter bwName = new BufferedWriter(outputWriter);
			String fileInfo = fileName + "/" + myName + "/" + String.valueOf(file.length()) + "\n";
			Log.i("log", fileName + "的大小为" + String.valueOf(file.length()));

			fileInfo = URLDecoder.decode(fileInfo, "UTF-8");
			bwName.write(fileInfo);
			bwName.flush();
			//bwName.close();
			//outputWriter.close();
			//outputName.close();
			//name.close();

			//Socket data = new Socket(ipAddress, port);
			//OutputStream outputData = data.getOutputStream();
			FileInputStream fileInput = new FileInputStream(path);
			int size = -1;
			byte[] buffer = new byte[1048576];
			while((size = fileInput.read(buffer, 0, 1048576)) != -1){
				outputName.write(buffer, 0, size);
				Log.i("log","发送了" + size + "字节");
			}
			bwName.close();
			outputWriter.close();
			outputName.close();
			fileInput.close();
			name.close();
			return fileName + " 发送完成";
		} catch (Exception e) {
			return "发送错误:\n" + e.getMessage();
		}
	}
}
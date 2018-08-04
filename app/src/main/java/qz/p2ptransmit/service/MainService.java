package qz.p2ptransmit.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import qz.p2ptransmit.MainActivity;
import qz.p2ptransmit.filemanager.TransmitReceiver;
import qz.p2ptransmit.filemanager.TransmitSender;
import qz.p2ptransmit.friendsmanager.FriendsManager;
import qz.p2ptransmit.tcp.TcpHelper;
import qz.p2ptransmit.transmitcontacts.TransmitContactsActivity;
import qz.p2ptransmit.udp.UdpHelper;
import qz.p2ptransmit.wifiManager.LanIPHelper;

public class MainService extends Service{
	private Map<String,Integer> transmitProgress;
	private TransmitReceiver transmitReceiver;    
	private String userName = "";                  //本机的文件传输码和用户名
	private String message = null;
	private UdpHelper uh;
	private TcpHelper th;
	private int port;                                                  //文件传输模块的TCP端口号
	private boolean isApState;                                         //判断当前设备是否是热点模式
	public FriendsManager friendsManager;
	public List<String> onlineUser = new ArrayList<String>();             //当前在线用户列表
	private ServerSocket server;
	private	boolean isRunning;

	@SuppressLint("HandlerLeak") @Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		if(intent.getStringExtra("ifap").equals("isap")){
			isApState = true;
		}
		else{
			isApState = false;
		}
        isRunning = true;
		userName = intent.getStringExtra("username");
        onlineUser.add("请选择");
		uh = new UdpHelper(LanIPHelper.getLocalIpAddress(isApState, MainService.this));
		th = new TcpHelper(9876);
		friendsManager = new FriendsManager();
		Thread fileListener = new Thread(new Runnable() {
			@Override
			public void run() {
				port = 9999;       //绑定端口
				while (port > 9000) {
					try {
						server = new ServerSocket(port);
						if (server != null)
							break;
					} catch (Exception e) {
						port--;
					}
				}
				if (server != null) {
					transmitReceiver = new TransmitReceiver(server);
					transmitProgress = new HashMap<String, Integer>();
					while (MainService.this.isRunning)
                    {//接收文件
						transmitReceiver.ReceiveName(transmitProgress);
						//通知activity显示文件正在接收的图标
						Intent intentReceiving = new Intent();
						intentReceiving.setAction(MainActivity.ACTION_FILERECEIVEING);
						sendBroadcast(intentReceiving);
						String result = transmitReceiver.ReceiveData(transmitProgress);
						Intent intentReceived = new Intent();
						intentReceived.setAction(MainActivity.ACTION_FILERECEIVED);
						intentReceived.putExtra("msg", result);
						sendBroadcast(intentReceived);
						//通知activity让文件正在接收的图标消失
						if (transmitProgress.size() == 0) {
							//mhandler.sendEmptyMessage(4);
						}
					}
				} else {
					//Message.obtain(handler, 1, "未能绑定端口").sendToTarget();
				}
			}
		});
		fileListener.start();                                        //接收文件
		
		SetUdpListenerThread udpListener = new SetUdpListenerThread();  //启动线程，监听局域网内的UDP广播
		udpListener.start();
		SetTcpListenerThread tcpListener = new SetTcpListenerThread();  //启动线程，接收TCP消息
		tcpListener.start();
		BroadcastInfo bci = new BroadcastInfo();                     //定时在局域网内广播本机的IP以及用户名
		bci.start();
		//Toast.makeText(this, "Service 启动", Toast.LENGTH_LONG).show();
		return START_REDELIVER_INTENT;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		//Toast.makeText(this, "Service 解除绑定", Toast.LENGTH_LONG).show();
		return super.onUnbind(intent);
	}
	

	@Override
	public void onDestroy()
	{
		//Toast.makeText(this, "Service 销毁", Toast.LENGTH_LONG).show();
        MainService.this.isRunning = false;
        try {
            server.close();
        } catch (IOException e) {
            Log.e("关闭ServerSocket:", e.toString());
            e.printStackTrace();
        }
        super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		//Toast.makeText(this, "绑定Service", Toast.LENGTH_LONG).show();
		return new MsgBinder();
	}
	
	
	public class MsgBinder extends Binder{
		public MainService getService()
		{
			return MainService.this;
		}
	}
	
	
	private class SetUdpListenerThread extends Thread{
		@Override
		public void run(){
			while(MainService.this.isRunning)
			{
				message = uh.ReceiveMessage();
				if(message.startsWith("T&I"))
				{
					final String[] msg = message.split("/");
					if(!friendsManager.getOnlineNames().contains(msg[1]))
					{
						friendsManager.addFriend(msg[1], msg[2] + "/" + msg[3]);
						onlineUser.add(msg[1]);
					}
				}
			}
		}
	}//end SetUdpListenerThread
	
	
	private class SetTcpListenerThread extends Thread{
		@Override
		public void run(){
			while(MainService.this.isRunning)
			{
				final String tcpMessage = th.Receive();
				Log.i("log", tcpMessage);
				if(tcpMessage.startsWith("Response"))
				{	
					final String[] temp = tcpMessage.split("/");
					friendsManager.addFriend(temp[1], temp[2] + "/" + temp[3] + "/" + temp[4]);
				}
				else if(tcpMessage.startsWith("PrepareTransmitContacts"))
				{
					Log.i("Mainservice", "收到准备接收通讯录消息的提示");
					Intent intent=new Intent(MainService.this, TransmitContactsActivity.class);
					intent.putExtra("recFromIP", tcpMessage.split("/")[1]);
					intent.putExtra("recFromName", tcpMessage.split("/")[2]);
					intent.putExtra("command","receive");
					intent.putExtra("num", tcpMessage.split("/")[3]);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
				else if(tcpMessage.startsWith("StartSend"))
				{

				}
			}
		}
	}//end SetTcpListenerThread
	
	
	private class BroadcastInfo extends Thread{
		@Override
		public void run()
		{
			Timer t = new Timer();
			class MyTask extends TimerTask {
				@Override
				public void run() {
					uh.SendMessage("T&I/" + userName + "/" + LanIPHelper.getLocalIpAddress(isApState, MainService.this) + "/" + String.valueOf(port));
				}
			}
			t.schedule(new MyTask(), 200, 2000);
		}
	}// end Broadcast
	
	
	public void sendFile(String sendname, String sendpath, String curreceiverinfo)
	{
		final String sendName = sendname;
		final String sendPath = sendpath;
		final String curReceiverInfo = curreceiverinfo;
		Thread sendThread = new Thread(new Runnable(){
			@Override
			public void run() {
				String targetIP = curReceiverInfo.split("/")[0];
				Log.i("Receiver IP", targetIP);
				int targetPort =  Integer.valueOf(curReceiverInfo.split("/")[1]).intValue();
				TransmitSender transmitSender = new TransmitSender();
				String result = transmitSender.sendFile(sendName, sendPath, targetIP, targetPort, userName);
				Intent intent = new Intent();
				intent.setAction(MainActivity.ACTION_FILESEND);
				intent.putExtra("msg", result);
				sendBroadcast(intent);
			}
		});
		sendThread.start();
	}
	
}

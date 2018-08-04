package qz.p2ptransmit.transmitcontacts;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import qz.p2ptransmit.R;
import qz.p2ptransmit.tcp.TcpHelper;

/*
 * 
 * 
 * 
 * 
 * 9888端口用于接收通讯录内容
 * 发送方作为server，等待接收方告诉发送方“已准备好接收”后发送方开始发送数据
 */
public class TransmitContactsActivity extends Activity {
	private final static int UPDATE_PROCESS = 100;


	private ServerSocket server;
	private InputStream inputStream;
	private InputStreamReader streamReader;
	private OutputStream outputStream;
	private BufferedReader br;
	private String target,recFrom;
	private String localIP;
	private Handler handler;
	private int count;                  //统计已经发送/接收的联系人的个数
	private String myName,recFromName;
	private int totalNum;                 //统计需要接收的个数
	@SuppressLint("HandlerLeak") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transmit_contact);
		count = 0;//计数器归零
        final TextView txtStatus = (TextView)findViewById(R.id.trans_prog_title);
        final TextView txtProcess = (TextView)findViewById(R.id.txt_progress);
		final ProgressBar progProcess = (ProgressBar)findViewById(R.id.progressBar_contacts);
        Intent intent = getIntent();  
        Bundle bundle = intent.getExtras();
        String cmd = bundle.getString("command");
  
        handler = new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			switch(msg.what){
					case 0:
						txtStatus.append(msg.obj.toString());
						break;
					case 2:
						AlertDialog.Builder builder = new AlertDialog.Builder(TransmitContactsActivity.this);
						builder.setTitle("是否接收通讯录信息")
						.setMessage(msg.obj.toString() + "想要将他的通讯录数据传输给您")
						.setPositiveButton("接收", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface arg0, int arg1){
								RecContactsThread rct = new RecContactsThread();
								rct.start();
							}
						})
						.setNegativeButton("不接收",new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface arg0, int arg1){
								new Thread(){            //启动线程,发送拒绝消息
									@Override
									public void run()
									{
										try {
											Socket mSocket = new Socket(recFrom, 9888);
											outputStream = mSocket.getOutputStream();
											outputStream.write("Refuse\n".getBytes("UTF-8"));
											outputStream.flush();

										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}.start();
								finish();
							}
						});
						AlertDialog ad = builder.create();
						ad.show();
						break;
					case 3:
						AlertDialog.Builder builder2 = new AlertDialog.Builder(TransmitContactsActivity.this);
						builder2.setTitle(msg.obj.toString())
						.setMessage("是否重新发送")
						.setPositiveButton("是", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface arg0, int arg1){
								SendContactsThread sct = new SendContactsThread();
								sct.start();
							}
						})
						.setNegativeButton("否",new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface arg0, int arg1){
								finish();
							}
						});
						AlertDialog ad2 = builder2.create();
						ad2.show();
						break;
					case UPDATE_PROCESS:
						txtProcess.setText(msg.obj.toString());
						progProcess.setProgress((100 * count) / totalNum);
						break;
    			}
    		}
    	};
    	
        if("send".equals(cmd))
        {
        	target = bundle.getString("sendto");
            localIP = bundle.getString("localIP");
            myName = bundle.getString("myName");
			totalNum = getContactsNum();
        	SendContactsThread sct = new SendContactsThread();
        	sct.start();
        	txtStatus.append("Sender\n联系人个数为:" + String.valueOf(getContactsNum()) + "\n");
        }
        else
        {
			totalNum = Integer.valueOf(bundle.getString("num"));
        	recFrom = bundle.getString("recFromIP");
        	recFromName = bundle.getString("recFromName");
        	Message.obtain(handler, 2, recFromName).sendToTarget();
        }
    }


    private void addMyContact(String mName, List<String> numbers) {
		//创建一个空的ContentValues
        ContentValues values=new ContentValues();
		//向ContactsContract.RawContacts.CONTENT_URI执行一个空值插入
		//目的是获取系统返货的rawContactId，以便添加联系人名字和电话使用同一个id
        Uri rawContactUri=getContentResolver().insert(
                ContactsContract.RawContacts.CONTENT_URI,values);
        long rawContactId= ContentUris.parseId(rawContactUri);
		//清空values，设置id，设置内容类型，设置联系人姓名
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, mName);
		//向联系人URI添加联系人姓名
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
        int num = numbers.size();
        int i = 0;
        while(i < num)   //如果该联系人有多个号码
        {	
        	String type = numbers.get(i).split("/")[0];
        	String number = numbers.get(i).split("/")[1];
			//清空values，设置id，设置内容类型，设置联系人电话
	        values.clear();
	        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
	        values.put(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
	        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,number);
			//设置电话类型
	        if(type.equals("M")) {        //手机
        		values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
	        }
	        else if(type.equals("H")){    //家庭
        		values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
	        }
	        else if(type.equals("W"))
	        {                             //工作
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
	        }
	        else if(type.equals("FH"))
	        {                             //传真（家庭）
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME);
	        }
	        else if(type.equals("FW"))
	        {                             //传真（工作）
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);
	        }
	        else if(type.equals("CM"))
	        {                             //公司（主要）
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN);
	        }
	        else if(type.equals("OF"))
	        {                             //传真（其他）
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX);
	        }
	        else if(type.equals("T"))
	        {
	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_TELEX);
	        }
	        else
			{                             //其他
        		values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        	}
	        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);   //在数据库中插入条目
	        i += 1;     //已接收的数量加1
        }

    }
    
    
    public class SendContactsThread extends Thread{
    	@Override
		public void run(){
			TcpHelper th = new TcpHelper();
    		th.Send(target, "PrepareTransmitContacts/" + localIP + "/" + myName + "/" + String.valueOf(totalNum));
			Log.i("sendContacts", target + "PrepareTransmitContacts/" + localIP + "/" + myName + "/" + String.valueOf(totalNum));
			try {
				server = new ServerSocket(9888);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
			try {
				Message.obtain(handler, 0, "等待接收方建立链接\n").sendToTarget();
	    		Socket msocket = server.accept();
	    		Message.obtain(handler, 0, "接收方已建立链接\n").sendToTarget();
				inputStream = msocket.getInputStream();
				streamReader = new InputStreamReader(inputStream);
				br = new BufferedReader(streamReader);
				String recMessage = br.readLine();
				outputStream = msocket.getOutputStream();
				if(recMessage.equals("Ready"))
				{
					Message.obtain(handler, 0, "StartTransmit\n").sendToTarget();
				}
				else if(recMessage.equals("Refuse"))
				{
					Message.obtain(handler, 3, "对方拒绝接收您的通讯录信息").sendToTarget();
					this.interrupt();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			//使用ContentResolver查找联系人数据
            Cursor cursor=getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,null,null, null,null);
			//获取系统中的所有人

            while (cursor.moveToNext()){
            	try {
					Thread.sleep(5);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				//获取联系人id
                String contactId=cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts._ID));
				//获取联系人姓名
                final String name=cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                try {
					outputStream.write(("NC/" + name + "\n").getBytes("UTF-8"));
					outputStream.flush();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//使用ContentResolver通过id查找联系人的电话
                Cursor phones=getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId
                        , null, null);
                while(phones.moveToNext())
                {
                	String number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
                	String phoneNumberType = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                	String Type = "";
                	switch (Integer.parseInt(phoneNumberType)) {  
                	case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                		Type = "M/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                		Type = "H/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                		Type = "W/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                		Type = "FH/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                		Type = "FW/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
                		Type = "CM/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                		Type = "OF/";
                		break;
                	case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
                		Type = "T/";
                		break;
                	default:
                		Type = "O/";
                		break;
                	}
                	try {
    					outputStream.write((Type + number + "\n").getBytes("UTF-8"));
    					outputStream.flush();
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}	
                }
				//关闭Cursor
                phones.close();
                try {
    				outputStream.write("EN\n".getBytes("UTF-8"));      //"EN" means "EndNewContact"
    				outputStream.flush();
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                count += 1;
				Message.obtain(handler, UPDATE_PROCESS, "已发送了" + String.valueOf(count) + "个联系人信息").sendToTarget();
        	}
			//关闭Cursor
			cursor.close();
            try {
				outputStream.write("EA\n".getBytes("UTF-8"));    //"EA" means "EndAll"
				outputStream.flush();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            Message.obtain(handler, 0, "联系人已全部同步到对方手机").sendToTarget();
        }

    }//End SendContactsThread
    
    
    public class RecContactsThread extends Thread
    {
    	@Override
		public void run(){
    		try {
				Socket mSocket = new Socket(recFrom, 9888);
				outputStream = mSocket.getOutputStream();
				inputStream = mSocket.getInputStream();
				streamReader = new InputStreamReader(inputStream);
				br = new BufferedReader(streamReader);
				outputStream.write("Ready\n".getBytes("UTF-8"));
				outputStream.flush();
				Message.obtain(handler, 0, "告诉对方已经准备好接收通讯录\n").sendToTarget();
				String message;
				while(!(message = br.readLine()).equals("EA"))
				{
					if(message.startsWith("NC"))
					{	
						String name = message.split("/")[1];
						String msg;
						final List<String> numbers = new ArrayList<String>();
						while( !(msg = br.readLine()).equals("EN"))
						{
							numbers.add(msg);
						}
						addMyContact(name ,numbers);
					}
					count += 1;
					Message.obtain(handler, UPDATE_PROCESS, "共接收了" + String.valueOf(count) + "个联系人信息").sendToTarget();
				}
				Message.obtain(handler, 0, "接收联系人完成\n").sendToTarget();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }//End RecContactsThread




	private int getContactsNum()                //获取联系人个数
	{
		int num = 0;
		//使用ContentResolver查找联系人数据
		Cursor cursor=getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,null,null, null,null);
		//获取系统中的所有人
		while (cursor.moveToNext()){
			num += 1;
		}
		//关闭Cursor
		cursor.close();
		return num;
	}



}
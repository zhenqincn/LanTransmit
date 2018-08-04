package qz.p2ptransmit.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import qz.p2ptransmit.MainActivity;
import qz.p2ptransmit.R;
import qz.p2ptransmit.service.MainService;
import qz.p2ptransmit.wifiManager.OpenApActivity;
import qz.p2ptransmit.wifiManager.WifiAutoConnectManager;

public class LoginActivity extends Activity{
	private Handler handler;
	private String username;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
		final EditText editUserName = (EditText)findViewById(R.id.edit_name);
		ImageButton btnOpenHotSpot = (ImageButton) findViewById(R.id.imgbtn_open_hotspot);
		ImageButton btnEnter = (ImageButton) findViewById(R.id.imgbtn_enter);

		PackageManager packageManager = getPackageManager();
		List<PackageInfo> mAllPackages = new ArrayList<PackageInfo>();
		mAllPackages = packageManager.getInstalledPackages(0);
		for(int i = 0; i < mAllPackages.size(); i++)
		{
			PackageInfo packageInfo = mAllPackages.get(i);
			Log.i("package path", packageInfo.applicationInfo.sourceDir);
			Log.i("apk name", packageInfo.applicationInfo.loadLabel(packageManager).toString());
		}

		File appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived");
		if(! appDir.exists())
		{
			appDir.mkdirs();
		}
		File configDir = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived" + "/Config");
		final File configFile = new File(Environment.getExternalStorageDirectory().getPath() + "/LanTransReceived" + "/Config/init.conf");
		if(! configDir.exists())
		{
			configDir.mkdirs();
			if(!configFile.exists())
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		else
		{
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(configFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String lastUserName = bufferedReader.readLine();
				fileReader.close();
				bufferedReader.close();
				if(lastUserName != null)
				{
					editUserName.setText(lastUserName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case 0:
					Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
					break;

				}
			}
		};

		btnOpenHotSpot.setOnClickListener(new OnClickListener(){        //�����ȵ�
			@Override
			public void onClick(View v) {
				username = editUserName.getText().toString();
				if(username.equals(""))
				{
					Message.obtain(handler, 0, "请输入用户名后再创建热点").sendToTarget();
				}
				else
				{
					try {
						FileWriter fileWriter = new FileWriter(configFile, false);
						fileWriter.write(username);
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Intent intent=new Intent(LoginActivity.this,OpenApActivity.class);
					intent.putExtra("SSID",username);
					startActivityForResult(intent,0);
				}
			}
		});

		btnEnter.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				username = editUserName.getText().toString();
				if(username.equals(""))
				{
					Message.obtain(handler, 0, "用户名不能为空").sendToTarget();
				}
				else
				{
					try {
						FileWriter fileWriter = new FileWriter(configFile, false);
						fileWriter.write(username);
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Intent intent=new Intent(LoginActivity.this,MainActivity.class);
					intent.putExtra("username",username);
					intent.putExtra("ifap", "notap");
					startActivity(intent);
				}
			}
		});
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 0)
		{
			String APName = data.getStringExtra("apname");
			String preSharedKey	= data.getStringExtra("presharedkey");
			Log.i("log",APName + "     " + preSharedKey);
			
			//Intent serviceIntent = new Intent(LoginActivity.this,MainService.class);
			//serviceIntent.putExtra("username",username);
			//serviceIntent.putExtra("trans_password",trans_password);
			//serviceIntent.putExtra("ifap", "isap");
			//startService(serviceIntent);
			Intent intent=new Intent(LoginActivity.this,MainActivity.class);
			intent.putExtra("username",username);
			intent.putExtra("apname", APName);
			intent.putExtra("presharedkey", preSharedKey);
			intent.putExtra("ifap", "isap");
			startActivity(intent);
		}
	}
	
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }
	
    
    @Override
    public void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        final String apinfo_all = new String(msg.getRecords()[0].getPayload());
        Log.i("log",apinfo_all);
        final String[] apinfo = apinfo_all.split("/");
        Log.i("log",String.valueOf(apinfo.length));
        if(apinfo[0].equals("APINFO"))
        {
            new Thread()
            {
            	@Override
            	public void run()
            	{
            		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            		WifiAutoConnectManager wacm = new WifiAutoConnectManager(wifiManager);
            		wacm.connect(apinfo[1], apinfo[2], WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
            		Log.i("log","已成功连接WIFI");
					Message.obtain(handler, 0, "用户名不能为空").sendToTarget();
            	}
            }.start();
        }
    }

}

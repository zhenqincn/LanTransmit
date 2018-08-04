package qz.p2ptransmit;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import qz.p2ptransmit.filemanager.FilesViewActivity;
import qz.p2ptransmit.service.MainService;
import qz.p2ptransmit.transmitcontacts.TransmitContactsActivity;
import qz.p2ptransmit.wifiManager.LanIPHelper;

public class MainActivity extends Activity  implements CreateNdefMessageCallback{
    public static String ACTION_FILERECEIVED = "qz.filereceived";
    public static String ACTION_FILERECEIVEING = "qz.filereceiving";
    public static String ACTION_FILESEND = "qz.filesend";
    public static String ACTION_FILESENDERROR = "qz.filesenderror";
    private final static int SHOWPROGRESS = 30;
    private final static int HIDEPROGRESS = 31;
    private final static int SHOWMESSAGE = 10;
    private final static int SETTITLE = 11;
    private final static int SHOWTOAST = 12;

    private TransmitStatusReceiver transmitStatusReceiver;
    private boolean isApState = false;                                         //判断当前设备是否是热点模式
    private String APName,PreSharedKey;                                //热点名称，热点key
    private NfcAdapter mNfcAdapter;
    private MainService mainService;
    private TextView tvMsg;
    private EditText txtEt;
    private Spinner mySpinner;                                         //在线用户下拉列表
    private ArrayAdapter<String> adapter;                              //在线用户列表适配器
    private String curReceiver;
    private Handler handler;
    private ImageButton imgbtnShowProgress;

    @SuppressLint("HandlerLeak") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final String userName = bundle.getString("username");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(bundle.getString("ifap").equals("isap") && mNfcAdapter != null)
        {
            isApState = true;
            APName = bundle.getString("apname");
            PreSharedKey = bundle.getString("presharedkey");
            mNfcAdapter.setNdefPushMessageCallback(this, this);           //设置NFC发送消息的回调函数
        }
        Intent bindServIntent = new Intent(MainActivity.this, MainService.class);
        bindServIntent.putExtra("username", userName);
        bindServIntent.putExtra("ifap", bundle.getString("ifap"));
        startService(bindServIntent);
        bindService(bindServIntent, servConn, 0);
        tvMsg = (TextView)findViewById(R.id.tvMsg);
        mySpinner = (Spinner)findViewById(R.id.selectTarget);
        txtEt = (EditText)findViewById(R.id.et);
        ImageButton btnSend = (ImageButton)findViewById(R.id.imgbtn_send_all_file);
        ImageButton btnSendPicture = (ImageButton)findViewById(R.id.imgbtn_send_album);
        ImageButton btnSendContacts = (ImageButton)findViewById(R.id.imgbtn_send_contacts);
        imgbtnShowProgress = (ImageButton)findViewById(R.id.imagebtn_show_progress);
        imgbtnShowProgress.setVisibility(View.INVISIBLE);

        handler = new Handler(){                               //用于将消息显示在主界面上
            @SuppressLint("SimpleDateFormat") @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mainService.onlineUser);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mySpinner.setAdapter(adapter);
                        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                curReceiver = adapter.getItem(arg2);
                                Log.i("curReceiver", curReceiver);
                                arg0.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                if(curReceiver == null)
                                {
                                    Message.obtain(handler, 10, "未选择文件接收者").sendToTarget();
                                }
                                arg0.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    case SHOWMESSAGE:    //界面上显示运行结果文字
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        String message = msg.obj.toString();
                        if(!message.startsWith("Success"))
                        {
                            txtEt.append("\n[" + format.format(new Date()) + "]" + message);
                        }
                        break;
                    case SETTITLE:    //界面最上方显示提示消息
                        tvMsg.setText(msg.obj.toString());
                        break;
                    case SHOWTOAST:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case SHOWPROGRESS:
                        imgbtnShowProgress.setVisibility(View.VISIBLE);
                        break;
                    case HIDEPROGRESS:
                        imgbtnShowProgress.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter();            //注册广播
        intentFilter.addAction(MainActivity.ACTION_FILERECEIVEING);
        intentFilter.addAction(MainActivity.ACTION_FILERECEIVED);
        intentFilter.addAction(MainActivity.ACTION_FILESEND);
        intentFilter.addAction(MainActivity.ACTION_FILESENDERROR);
        transmitStatusReceiver = new TransmitStatusReceiver();
        registerReceiver(transmitStatusReceiver, intentFilter);


        btnSend.setOnClickListener(new OnClickListener(){                 //发送SD卡中任意文件的按钮
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilesViewActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btnSendPicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);    //****4.4以上版本不能用这个方法
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        btnSendContacts.setOnClickListener(new OnClickListener(){       //发送文件的按钮
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, TransmitContactsActivity.class);
                intent.putExtra("command", "send");
                intent.putExtra("sendto", mainService.friendsManager.getFriendInfo(curReceiver).split("/")[0] );
                intent.putExtra("localIP", LanIPHelper.getLocalIpAddress(isApState, MainActivity.this));
                intent.putExtra("myName", userName);
                startActivity(intent);
            }
        });


        new Thread(){
            @Override
            public void run()
            {
                while(mainService == null)
                {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("log", "service完成初始化");
                handler.sendEmptyMessage(0);
            }
        }.start();
        //显示用户资料提示栏
        Message.obtain(handler, SETTITLE , "本机IP：" + LanIPHelper.getLocalIpAddress(isApState, MainActivity.this) + "用户名：" + userName).sendToTarget();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Time time = new Time();
        time.setToNow();
        String text = ("APINFO/" + APName + "/" + PreSharedKey);
        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
                "application/com.example.android.beam", text.getBytes()  )  );
        return msg;
    }


    ServiceConnection servConn = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName arg0){

        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mainService = ((MainService.MsgBinder)service).getService();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     //用于接收其他Activity发来的消息
        if(requestCode == 1)
        {

        }
        else if(resultCode == RESULT_OK){     //选择了发送文件
            String sendName = data.getStringExtra("FileName");
            String sendPath = data.getStringExtra("FilePath");
            mainService.sendFile(sendName, sendPath, mainService.friendsManager.getFriendInfo(curReceiver) );
        }
    }


    @Override
    public void onDestroy()
    {
        unbindService(servConn);
        Intent bindServIntent = new Intent(MainActivity.this, MainService.class);
        stopService(bindServIntent);
        unregisterReceiver(transmitStatusReceiver);
        super.onDestroy();
    }


    class TransmitStatusReceiver extends BroadcastReceiver           //接收到广播，查看是否有app的使用时长超出了限制
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(MainActivity.ACTION_FILERECEIVEING.equals(intent.getAction()))
            {
                handler.sendEmptyMessage(SHOWPROGRESS);
            }
            else if(MainActivity.ACTION_FILERECEIVED.equals(intent.getAction()))
            {
                handler.sendEmptyMessage(HIDEPROGRESS);
                Message.obtain(handler, SHOWMESSAGE , intent.getStringExtra("msg")).sendToTarget();
            }
            else if(MainActivity.ACTION_FILESEND.equals(intent.getAction()))
            {
                Message.obtain(handler, SHOWMESSAGE , intent.getStringExtra("msg")).sendToTarget();
            }
        }
    }
}

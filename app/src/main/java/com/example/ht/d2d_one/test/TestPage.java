package com.example.ht.d2d_one.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ht.d2d_one.R;
import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.bisicWifiDirect.MainActivity;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.communication.MyServerSocket;
import com.example.ht.d2d_one.interGroupCommunication.MultiCast;
import com.example.ht.d2d_one.interGroupCommunication.SocketReuse;
import com.example.ht.d2d_one.interGroupCommunication.Unicast;
import com.example.ht.d2d_one.util.FileTransfer;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.File;
import java.net.Socket;
import java.util.List;

public class TestPage extends Activity {
    private String p2pWifiAddr;
    private String wlanAddr;
    private boolean isGW;
    private boolean isGO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            p2pWifiAddr = bundle.getString("p2pAddress");
            wlanAddr = bundle.getString("wifiAddress");
            isGO = bundle.getBoolean("isGO");
            isGW = bundle.getBoolean("isGW");
            if(wlanAddr==null){
                Log.d("wlanAddress","wlanAddress 为空值");
            }
            if(p2pWifiAddr==null){
                Log.d("p2pWifiAddr","p2pWifiAddr 为空值");
            }
        }
        /**
         * 组播实验
         */
        //p2p组播发送
        Button button = findViewById(R.id.intraGroupMultiCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.1.2.5","p2pmultiCastTest");
                myMulticastSocketThread.start();
            }
        });
        //p2p组播接收
        button = findViewById(R.id.intraGroupMultiCastReceive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"recv","239.1.2.5",true,p2pWifiAddr);
                myMulticastSocketThread.start();
            }
        });

        //wlan0组播发送
        button = findViewById(R.id.interGroupMultiCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.1.2.5","wlan0multiCastTest");
//                myMulticastSocketThread.start();
                MultiCast multicast = new MultiCast(50000,"send","239.1.2.5",wlanAddr,"wlan0multiCastTest");
                multicast.start();
            }
        });
        //wlan0组播接收
        button = findViewById(R.id.interGroupMultiCastReceive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"recv","239.1.2.5",true,wlanAddr);
                myMulticastSocketThread.start();
            }
        });


        //不绑定网卡发送组播
        button = findViewById(R.id.MultiSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiCast multiCast = new MultiCast(50000,"send","239.1.2.5","test","wlan0multiCastTest");
                multiCast.start();
            }
        });


        /**
         * 网关节点单播实验,网关发送单播，LC组主接收单播
         */
        button = findViewById(R.id.interGroupGWUniCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientSocket clientSocket = new ClientSocket("192.168.49.199",50003,"test","duotiao");
                clientSocket.start();
//                Unicast unicast = new Unicast("192.168.49.199",50003,"192.168.49.123","p2p0test","write");
//                unicast.start();
            }
        });
        button = findViewById(R.id.interGroupGOUniCastReceive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyServerSocket myServerSocket = new MyServerSocket(50003,true);
                myServerSocket.start();
            }
        });

        button = findViewById(R.id.interGroupGOUniCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                //从arp中获取网关节点wlan口的IP地址，当网关通过WiFi加入到LC组中时，LC组的组信息是会发生更新的，因此可以拿到MAC信息
//                List<String> macOfGWS= BasicWifiDirectBehavior.icnOfGO.chooseGW(BasicWifiDirectBehavior.icnOfGO.getGM());
//                for(int i = 0; i<macOfGWS.size();i++){
//                    String ip = GetIpAddrInP2pGroup.getGWWlanIP(macOfGWS.get(i));
//                    ClientSocket clientSocket = new ClientSocket(ip,50004,"write","来自LC组主的单播信息");
//                    clientSocket.start();
//                }
                /**
                 * 对于网关节点和LC组主节点的操作有一些稍微的区别，因为两者存储socket的方式不同，需要不同的方式，复用socket传递数据需要开子线程
                 * LC组主在复用socket时需要指明socket连接的网关mac地址，根据回溯信息中的path来判断
                 */
                if(isGO){
                    Socket socket = BasicWifiDirectBehavior.icnOfGO.getISI().get("1");
                    SocketReuse socketReuse = new SocketReuse(socket,"write","1111111111111");
                    socketReuse.start();
                }else if(isGW){
                    Socket socket = BasicWifiDirectBehavior.icnOfGW.getISI().get("2");
                    SocketReuse socketReuse = new SocketReuse(socket,"write","222222222222222");
                    socketReuse.start();
                }
            }
        });
        button = findViewById(R.id.fileTransfer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String head = "1234+22322+111+/storage/emulated/0/ScreenRecord/b.mp4+hh";
//                FileTransfer fileTransfer = new FileTransfer(head);
//                ClientSocket clientSocket = new ClientSocket("192.168.49.1",30003,"writeFile",fileTransfer);
//                clientSocket.start();
                String head = "/storage/emulated/0/ScreenRecord/h.mp4";
                FileTransfer fileTransfer = new FileTransfer(head);
                ClientSocket clientSocket = new ClientSocket("192.168.49.1",30003,"onlyWriteFile",fileTransfer);
                clientSocket.start();
            }
        });
//        button = findViewById(R.id.RSSI);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                wifiManager.
//
//            }
//        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d("hh","start啦啦啦");
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d("hh","Resume啦啦啦");
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("hh","Pause啦啦啦");
    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.d("hh","stop啦啦啦");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("hh","Destroy啦啦啦");

    }
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("hh","Restart啦啦啦");
    }
}

package com.example.ht.d2d_one.test;

import android.app.Activity;
import android.content.Intent;
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
import com.example.ht.d2d_one.interGroupCommunication.SocketReuse;
import com.example.ht.d2d_one.interGroupCommunication.Unicast;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

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
        //组内组播p2p实验
        Button button = findViewById(R.id.intraGroupMultiCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.1.2.5","组内组播测试！！！");
                myMulticastSocketThread.start();
            }
        });
        button = findViewById(R.id.intraGroupMultiCastReceive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"recv","239.1.2.5",true,p2pWifiAddr);
                myMulticastSocketThread.start();
            }
        });
        //wlan0组播实验，组员使用wlan0接口接收组播数据
        button = findViewById(R.id.interGroupMultiCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50001,"send","239.1.2.6","组主p2p组播");
                myMulticastSocketThread.start();
            }
        });
        button = findViewById(R.id.interGroupMultiCastReceive);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50001,"recv","239.1.2.6",true,wlanAddr);
                myMulticastSocketThread.start();
            }
        });
        //网关节点wlan发送组播，LC组主接收组播实验
        button = findViewById(R.id.GWMultiSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50002,"send","239.1.2.7","message from GW","wlan0");
                myMulticastSocketThread.start();
            }
        });
        button = findViewById(R.id.GOMultiRecv);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50002,"recv","239.1.2.7",true,"192.168.49.1");
                myMulticastSocketThread.start();
            }
        });
        //网关节点单播实验,网关发送单播，LC组主接收单播
        button = findViewById(R.id.interGroupGWUniCastSending);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Unicast unicast = new Unicast("192.168.49.1",50003,wlanAddr,"来自网关的单播信息","write");
                unicast.start();
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
                //对于网关节点和LC组主节点的操作有一些稍微的区别，因为两者存储socket的方式不同，需要不同的方式，复用socket传递数据需要开子线程
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

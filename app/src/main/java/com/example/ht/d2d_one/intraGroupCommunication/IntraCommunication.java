package com.example.ht.d2d_one.intraGroupCommunication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ht.d2d_one.R;
import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.communication.MyServerSocket;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.interGroupCommunication.Unicast;
import com.example.ht.d2d_one.test.TestPage;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.util.ArrayList;
import java.util.List;

public class IntraCommunication extends Activity {
    //此处应该是资源查询结果，包括资源名称+对应的设备地址
    WifiManager wifiManager;
    private ArrayList<String> sourceResultList = new ArrayList<>();
    private boolean isGO = false;
    private boolean isGW = false;
    private boolean alreadeBeginClientQueryServer =false;
    private String deviceAddress;
    private String wifiAddress;
    private String goMAC;
    private String mDeviceIpAddress;
    private String resultQurryFromGO;
    //最多达到第四个组
    private int RRTTL = 8;
    private String RRMAC;
    private String pathInfo;
    private String tag;
    private String resourceName;
    private String typeOfResourceName;
    public boolean isClickQurrySourceButton = false;
    public static Main2ActivityMessagHandler main2ActivityMessagHandler = new Main2ActivityMessagHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try{
            WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
            multicastLock.acquire();
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.d("组播锁的开启","组播锁开启出现的异常");
        }
        setContentView(R.layout.activity_main2);
        Button button = findViewById(R.id.quitCluster);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("看这里","dddddddddddddddddddddddddis");
                finish();
            }
        });
        Button buttonQuery = findViewById(R.id.findSource);
        Button buttonQueryResult = findViewById(R.id.showResult);
        Button buttonReveiveMultiCast = findViewById(R.id.receiveMultiCast);
        Button buttonSendUnicast = findViewById(R.id.sendUnicast);
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            isGO = bundle.getBoolean("isGO");
            isGW = bundle.getBoolean("isGW");
            deviceAddress = bundle.getString("deviceAddress");
            if(!isGO){
                goMAC = bundle.getString("goMAC");
            }
        }
        buttonQuery.setOnClickListener(new View.OnClickListener(){
            EditText editTextSourceQueried = findViewById(R.id.resourceNameQurried);
            Spinner spinnerSourceType = findViewById(R.id.typeOfSource);
            @Override
            public void onClick(View v){
                //如果输入信息合法，则将查询信息发送给组主，并开启组员设备的socket服务监听模式
                //还需要判断本设备是否为组主！！！！
                if(editTextSourceQueried!=null&&spinnerSourceType!=null&&!spinnerSourceType.toString().equals("资源类别")){
                    if(!isGO){
                        /**
                         * RR的生成,向组主发送RR,销毁RR,组员节点的cache添加
                         */
                        RRMAC = deviceAddress;
                        resourceName = editTextSourceQueried.getText().toString();
                        typeOfResourceName = spinnerSourceType.getSelectedItem().toString();
                        ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(RRTTL,RRMAC,pathInfo,resourceName,typeOfResourceName);
                        Log.d("查询信息：",resourceName);
                        Log.d("RR信息",resourceRequestPacket.toString());
                        isClickQurrySourceButton = true;
                        new ClientSocket("192.168.49.1",30000,"query",resourceRequestPacket.toString()).start();
                        BasicWifiDirectBehavior.getGmCacheInformation().addCacheRecommend(System.currentTimeMillis(),resourceName+typeOfResourceName);
                        resourceRequestPacket.destory(resourceRequestPacket);
                        Log.d("开启服务端的mac地址是：",deviceAddress);
                        if(!alreadeBeginClientQueryServer){
                            MyServerSocket myServerSocketTwo = new MyServerSocket(deviceAddress,30001,"read", "client");
                            myServerSocketTwo.start();
                            alreadeBeginClientQueryServer = true;
                        }
                        Toast.makeText(IntraCommunication.this, "资源查询成功",Toast.LENGTH_SHORT).show();
                    }else{
                        //如果是组主，本地查询
                        isClickQurrySourceButton = true;
                    }
                }
            }
        });
        /**
         * 查询结果展示按钮，用来显示查询结果，也可以用来刷新
         */
        buttonQueryResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isClickQurrySourceButton){
                    if(main2ActivityMessagHandler.resultQueryFromGO!=null){
                        sourceResultList = (ArrayList<String>) StringToList(main2ActivityMessagHandler.resultQueryFromGO);
                        Log.d("点击查看查询结果为：：：：：", sourceResultList.toString());
                        ListView listView = (ListView) findViewById(R.id.list_Main2Activity);
                        ResultSourceAdapter resultSourceAdapter = new ResultSourceAdapter(IntraCommunication.this,R.layout.service_list,sourceResultList);
                        listView.setAdapter(resultSourceAdapter);
                    }else{
                        Log.d("查询结果为空值","hh"+main2ActivityMessagHandler.resultQueryFromGO);
                        Toast.makeText(IntraCommunication.this,"查询结果为空值",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            /**
             *
             * @param stringResult 具体格式为：MAC-NAME*MAC-NAME...
             * @return 其具体的格式为：MAC-NAME,MAC-NAME,
             */
            public List<String> StringToList(String stringResult){
                List<String> resultSource = new ArrayList<>();
                String[] singleResult = stringResult.split("\\*");
                int i=0;
                for(;i<=singleResult.length-1;i++){
                    resultSource.add(singleResult[i]);
                }
                Log.d("resultSource",resultSource.toString());
                return resultSource;
            }
        });
        /**
         * 开启组播接收，localIp 是本设备使用Wi-Fi Direct连接到组主上所得到的IP地址。
         * ipAddress 是本设备通过Wi-Fi连接到组后得到的IP地址
         * 仅网关节点可以接收组播
         */
        buttonReveiveMultiCast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGW){
                    String p2pIp = GetIpAddrInP2pGroup.getLocalIPAddress();
                    if(p2pIp!=null){
                        Log.d("本设备的本地IP地址为",p2pIp);
                    }
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //Log.d("wifiInfo",wifiInfo.toString());
                    int i = wifiInfo.getIpAddress();
                    wifiAddress = (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;
                    Log.d("ip地址是", wifiAddress);
                    MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread
                            (goMAC,deviceAddress,GetIpAddrInP2pGroup.getWlanMac(),40000,"recv","239.1.2.3",0,true);
                    myMulticastSocketThread.start();
                    MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread
                            (goMAC,GetIpAddrInP2pGroup.getWlanMac(),deviceAddress,40000,"recv","239.1.2.3",1,true);
                    myMulticastSocketThread1.start();
                    Log.d("开启组播接听","组播开始接收信息了!!!");
                    //根据接收到的信息，更新RR, 并向外组转发
                }
            }
        });
        buttonSendUnicast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //Log.d("wifiInfo",wifiInfo.toString());
                int i = wifiInfo.getIpAddress();
                wifiAddress = (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;
                if(isGW){
                    Unicast unicast = new Unicast("192.168.49.1",30000,wifiAddress,"7777777777777777777","write");
                    unicast.start();
                }
            }
        });
        //this.setListAdapter(new ResultSourceAdapter(this,R.layout.service_list,sourceResultList));
        Button buttonTest = findViewById(R.id.test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(),TestPage.class);
                intent1.putExtra("p2pAddress",GetIpAddrInP2pGroup.getLocalIPAddress());
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int i = wifiInfo.getIpAddress();
                wifiAddress = (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;
                intent1.putExtra("wifiAddress",wifiAddress);
                intent1.putExtra("isGO",isGO);
                intent1.putExtra("isGW",isGW);
                startActivity(intent1);
            }
        });
    }
    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onStop(){
        super.onStop();
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    public static class Main2ActivityMessagHandler extends Handler{
        private String resultQueryFromGO;
        public String getResultQurryFromGO() {
            return resultQueryFromGO;
        }
        private String multiCastInfo;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==3){
                resultQueryFromGO = (String)msg.obj;
                Log.d("查询返回信息",resultQueryFromGO);
            }else if(msg.what == 7){
                multiCastInfo = msg.obj.toString();
            }
        }
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processExtraData();
        Log.d("device_______________","1111111111111111111111");
    }

    private void processExtraData(){
        Intent intent = getIntent();
        String string = intent.getStringExtra("device");
        Log.d("device_______________",string);
    }

    public class ResultSourceAdapter extends ArrayAdapter<String> {
        private List<String> options;
        private String [] resultSource;
        public ResultSourceAdapter(Context context, int resource, List<String> items){
            super(context,resource,items);
            options = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.service_list,null);
            }
            Log.d("options是否为空",options.toString());
                String singleResult = options.get(position);
                if(singleResult!=null){
                    TextView result = (TextView) v.findViewById(R.id.ron_MAC_Name);
                    if(result!=null){
                        Log.d("单个结果为",singleResult.toString());
                        result.setText(singleResult.toString());
                    }
                }
            return v;
        }

        @Override
        public void add(String s){
        }

        @Override
        public void remove(String s) {
        }
    }
}

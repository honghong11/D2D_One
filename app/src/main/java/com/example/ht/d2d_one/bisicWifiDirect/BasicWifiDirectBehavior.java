package com.example.ht.d2d_one.bisicWifiDirect;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.IpPrefix;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ht.d2d_one.R;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.communication.MyServerSocket;
import com.example.ht.d2d_one.icn.CacheInformation;
import com.example.ht.d2d_one.icn.DoubleLinkedList;
import com.example.ht.d2d_one.icn.IcnOfNode;
import com.example.ht.d2d_one.icn.LRUCache;
import com.example.ht.d2d_one.interGroupCommunication.GateWay;
import com.example.ht.d2d_one.interGroupCommunication.MultiCast;
import com.example.ht.d2d_one.interGroupCommunication.SocketReuse;
import com.example.ht.d2d_one.interGroupCommunication.UnicastClient;
import com.example.ht.d2d_one.interGroupCommunication.UnicastSever;
import com.example.ht.d2d_one.intraGroupCommunication.IntraCommunication;
import com.example.ht.d2d_one.util.FindResources;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;
import com.example.ht.d2d_one.util.WifiDeviceWithLabel;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BasicWifiDirectBehavior extends ListFragment implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,WifiP2pManager.GroupInfoListener{
    public int i=1;
    private String goMAC;
    private boolean firstInterGroup = true;
    private boolean alreadyEnabelSeveralSocket =false;
    private boolean alreadyBeginInterGroupMultiRecv = false;
    private ClientSocket gmSocket;
    private WifiP2pGroup wifiP2pGroup;
    private MyServerSocket GMServer;
    private boolean isPublished = false;
    View mContentView = null;
    ProgressDialog processDialog =null;
    //icnOfnode三种节点形式
    //GO
    private static Map<String,Socket> ISI = new HashMap<>();
    private static DoubleLinkedList goCacheRecommend = new DoubleLinkedList();
    private static LRUCache goStorageCache = new LRUCache(30);
    private static Map<String,String> RN = new HashMap<>();
    private static Map<String,String > GM = new HashMap<>();
    private static List<Map<String,String>> QR = new ArrayList<>();
    private static CacheInformation goCacheInformation = new CacheInformation(goCacheRecommend,goStorageCache);
    public static CacheInformation getGoCacheInformation() {
        return goCacheInformation;
    }
    public static IcnOfNode icnOfGO = new IcnOfNode(ISI,RN,QR,GM,goCacheInformation,"kong");
    //GM
    private static DoubleLinkedList gmCacheRecommend = new DoubleLinkedList();
    private static LRUCache gmStorageCache = new LRUCache(15);
    public static CacheInformation getGmCacheInformation() {
        return gmCacheInformation;
    }
    private static CacheInformation gmCacheInformation = new CacheInformation(gmCacheRecommend,gmStorageCache);
    public static IcnOfNode icnOfGM = new IcnOfNode(gmCacheInformation,"kong");
    //GW
    private static Map<String,List<String>> GOT = new HashMap<>();
    private static DoubleLinkedList gwCacheRecommend = new DoubleLinkedList();
    private static LRUCache gwStorageCache = new LRUCache(15);
    private static CacheInformation gwCacheInformation = new CacheInformation(gwCacheRecommend,gwStorageCache);
    public static IcnOfNode icnOfGW = new IcnOfNode(GOT,ISI,gwCacheInformation,"kong",false);

    private WifiManager wifiManager;
    private final static int IS_RESOURCE = 1 ;
    private String allSourceFromClient = null;
    private  WifiP2pDevice mWifiP2pDevice;
    private WifiP2pGroup mWifiP2pGroup;
    private  WifiP2pInfo mWifiP2pInfo;
    public String allSourceOfThisGroup;
    private int countOfFindResouce = 0;
    private int groupSize =0;
    private List<String> clientMacList = new ArrayList<>();
    public List<WifiP2pDevice> peers = new ArrayList<>();
    MyServerSocket myServerSocket;
    public WifiP2pDevice getmWifiP2pDevice() {
        return mWifiP2pDevice;
    }

    public void setmWifiP2pDevice(WifiP2pDevice mWifiP2pDevice) {
        this.mWifiP2pDevice = mWifiP2pDevice;
    }

    public WifiP2pInfo getmWifiP2pInfo() {
        return mWifiP2pInfo;
    }

    public void setmWifiP2pInfo(WifiP2pInfo mWifiP2pInfo) {
        this.mWifiP2pInfo = mWifiP2pInfo;
    }

    public WifiP2pGroup getmWifiP2pGroup() {
        return mWifiP2pGroup;
    }

    public void setmWifiP2pGroup(WifiP2pGroup mWifiP2pGroup) {
        this.mWifiP2pGroup = mWifiP2pGroup;
    }

    WifiP2pManager manager;
    String label = "";
    ProgressDialog progressDialog = null;
    boolean isGroupMemberNull = true;
    boolean isGO = false;
    private boolean isGW = false;
    public List<WifiDeviceWithLabel> ePeers = new ArrayList<>();
    private List<WifiDeviceWithLabel> test = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();
    private String string = "/";
    private String string1 = "";
    private int updateTime =0;
    public static MessageHandler messageHandler = new MessageHandler();
    private WifiManager.MulticastLock multicastLock;
    WifiServiceAdapter wifiServiceAdapter;

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        wifiServiceAdapter = new WifiServiceAdapter(getActivity(),R.layout.row_device,ePeers);
        this.setListAdapter(wifiServiceAdapter);
        //this.setListAdapter(new WifiServiceAdapter(getActivity(),R.layout.row_device,ePeers));
        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //释放组播锁
        multicastLock = wifiManager.createMulticastLock("multiCast.test");
        multicastLock.acquire();
        //multicastLock.release();

        Map<String,String> map = new HashMap<>();
        map = messageHandler.getQurryMovieMap();
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        mContentView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //组员在断开连接后，对组员进行管理
                if(!isGO){
                    if(isGW){
                        /**
                         * TODO 关闭相关服务线程，
                         * 清除相关流表
                         * 清除本网关节点的流表：ISI和cache
                         * 更新p2p组主的网关节点表，RN表
                         * 更新wifi组主的网关节点表，RN表
                         * 断开WiFi
                         */
                        GMServer.close();
                        icnOfGW.destoryROT(icnOfGW.getGOT());
                        icnOfGW.destroyInterGroupSocketInfo();
                        wifiManager.removeNetwork(messageHandler.netID);
                        Log.d("组员设备断开WiFi","wifi断开啦啦啦啦啦");
                    }else{
                        icnOfGM.destroyInterGroupSocketInfo();
                    }

                    firstInterGroup = true;
                    countOfFindResouce = 0;
                }

                if(isGO){
                    icnOfGO.destroyInterGroupSocketInfo();
                    icnOfGO.clearGMTable();
                    isPublished =false;
                    alreadyEnabelSeveralSocket= false;
                    alreadyBeginInterGroupMultiRecv = false;
                    myServerSocket.close();
                    //关闭服务socket
                    if(myServerSocket!=null){
                        try{
                            myServerSocket.close();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    ((DeviceActionListener)getActivity()).removeLocalService();
                }
                /**
                 *调用disconnect()
                 在调用disconnect 之前停留一秒钟，让网关节点或者组主节点间发送"leave"的信息可以正常发送
                 */
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                ((DeviceActionListener)getActivity()).disconnect();
                //更新service adapter
                wifiServiceAdapter.clear();
                //如果是组主关闭连接，关闭服务器socket
            }
        });
        mContentView.findViewById(R.id.createGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            //建组，发布服务，设置组主设备主页面
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View DialogView = layoutInflater.inflate(R.layout.creategroup,null);
                AlertDialog dlg = new AlertDialog.Builder(getActivity()).
                        setTitle("建组").setIcon(R.mipmap.ic_launcher).setView(DialogView).
                        setPositiveButton("publish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((DeviceActionListener)getActivity()).createGroup(mWifiP2pDevice);
                        if(wifiP2pGroup!=null){
                            String netWorkID = wifiP2pGroup.getNetworkName();
                            Log.d("组信息",wifiP2pGroup.toString());
                        }
                        EditText editText = (EditText)DialogView.findViewById(R.id.label);
                        label = editText.getText().toString();
                        //publish在onGroupInfoAvailable中调用以便获取组信息
                        //((MainActivity)getActivity()).publishService(label);
                    }
                }).create();
                dlg.show();
                mContentView.findViewById(R.id.text3).setVisibility(View.GONE);
                mContentView.findViewById(R.id.groupOwnerList).setVisibility(View.GONE);
                mContentView.findViewById(R.id.nameOfResource).setVisibility(View.VISIBLE);
                mContentView.findViewById(R.id.resouce).setVisibility(View.VISIBLE);
            }
        });
        return mContentView;
    }
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo){
        //每有设备进组，都会触发onConnectionInfoAvailable函数
        mWifiP2pInfo = wifiP2pInfo;
        isGO = mWifiP2pInfo.isGroupOwner;
        Log.d("本设备是不是组主------","mWifiP2pInfo.isGroupOwner"+"="+String.valueOf(isGO));
        Log.d("mWifiP2pInfo的信息好吧",mWifiP2pInfo.toString());
        /**
         * 2018-11-20 修改之前的MyServerSocket构造器，添加一个IcnOfX(icnOfGO,IcnOfClient,IcnOfGW等)，
         * 因为像GM等表需要在子线程中使用
         * 2018-12-03 将组主服务端的开启移动到updateThisDevice
         * 2018-12-10 再添加一个cacheinformation 对象到socket中
         */
        Log.d("什么都没有发生吗",String.valueOf(isGO));
        if(isGO){
            Log.d("什么都没有发生吗","发生了什么");
            if(!alreadyEnabelSeveralSocket){
                icnOfGO.setGoMAC(mWifiP2pDevice.deviceAddress);
                myServerSocket = new MyServerSocket(mWifiP2pDevice.deviceAddress,30000,"read");
                myServerSocket.start();
                Log.d("服务myServersocket","组主开启服务端myServersocket");
                alreadyEnabelSeveralSocket = true;
            }
            if(!alreadyBeginInterGroupMultiRecv){
                /**
                 * 开启组间接收组播线程
                 */
                MultiCast multiCast = new MultiCast(40001,"recv","239.1.2.4");
                multiCast.start();
                Log.d("开启组间组播接听","组间组播开始接收信息了RR转发");
                alreadyBeginInterGroupMultiRecv = true;
            }
        }
    }
    public static class MessageHandler extends Handler{
        //messageList用于页面显示用
        private Map<String,String> resultResourceMap = new HashMap<>();
        private String messageFromClient = null;
        private String mSource = "电影-音乐-安装包-文字";
        //macOfAll用来存放所有设备的mac地址信息，目前没有用处
        private List<String> macOfAll = new ArrayList<>();
        private String[] splitMessage;
        private String macToDevice = null;
        private String sourceOfDevice = null;
        private String lcGOMAC;
        private String disconnectResult;
        private String multiCastInfo;
        //这四个Map用来存储组员发来的资源名称信息
        /**
         * 这四个map代替icnOfGO中的RN表。2018-12-09
         * 结构类型： key:RON mac地址 value: 存储路径+名称(名称是包含格式信息的)
         */
        private Map<String,String> movieMap = new HashMap<>();
        private Map<String,String> musicMap = new HashMap<>();
        private Map<String,String> packageMap = new HashMap<>();
        private Map<String,String> wordMap = new HashMap<>();

        public Map<String, String> getMovieMap() {
            return movieMap;
        }

        public Map<String, String> getMusicMap() {
            return musicMap;
        }

        public Map<String, String> getPackageMap() {
            return packageMap;
        }

        public Map<String, String> getWordMap() {
            return wordMap;
        }

        //这四个map用来存储组内的资源信息方便进行匹配以及结果的返回
        private Map<String,String> qurryMovieMap = new HashMap<>();

        public Map<String, String> getQurryMusicMap() {
            return qurryMusicMap;
        }

        public Map<String, String> getQurryPackageMap() {
            return qurryPackageMap;
        }

        public Map<String, String> getQurryWordMap() {
            return qurryWordMap;
        }

        public Map<String, String> getQurryMovieMap() {
            Log.d("movieMap中的内容信息",qurryMovieMap.toString());
            return qurryMovieMap;
        }
        private Map<String,String> qurryMusicMap = new HashMap<>();
        private Map<String,String> qurryPackageMap = new HashMap<>();
        private Map<String,String> qurryWordMap = new HashMap<>();
        private String[] splitSource ;
        private String isGateWay = "";
        private int netID;
        @Override
        public void handleMessage(Message message){
            if(message.what ==IS_RESOURCE){
                mSource = (String)message.obj;
                Log.d("子线程发来的message",mSource);
            }else if(message.what == 2){
                messageFromClient = (String)message.obj;
                if(messageFromClient!=null){
                    Log.d("messageFromClient",messageFromClient);
                    splitMessage = messageFromClient.split("\\*");
                    Log.d("消息分裂为及部分：",String.valueOf(splitMessage.length));
                    Log.d("组员设备发来的资源信息",splitMessage[1]);
                    macToDevice = splitMessage[0];
                    macOfAll.add(macToDevice);
                    if(splitMessage.length>1){
                        sourceOfDevice = splitMessage[1];
                    }
                    Log.d("设备的mac地址：",macToDevice);
                    //资源名称信息数据结构
                    splitSource = sourceOfDevice.split("·");
                    if(splitSource[0]!=null){
                        movieMap.put(macToDevice,splitSource[0]);
                        qurryMovieMap = dataProcessing(movieMap);
                    }else{
                        movieMap.put(null,null);
                    }
                    if(splitSource[1]!=null){
                        musicMap.put(macToDevice,splitSource[1]);
                        qurryMusicMap = dataProcessing(musicMap);
                    }else{
                        musicMap.put(null,null);
                    }
                    if(splitSource[2]!=null){
                        packageMap.put(macToDevice,splitSource[2]);
                        qurryPackageMap = dataProcessing(packageMap);
                    }else{
                        packageMap.put(null,null);
                    }
                    if(splitSource[3]!=null){
                        wordMap.put(macToDevice,splitSource[3]);
                        qurryWordMap = dataProcessing(wordMap);
                    }else{
                        wordMap.put(null,null);
                    }
                    Log.d("电影列表中的信息：",movieMap.toString());
                }
            }else if(message.what == 3){
                isGateWay = message.obj.toString();
            }else if (message.what == 4){
                netID = (int)message.obj;
            }else if(message.what == 5){
                lcGOMAC = message.obj.toString();
            }else if(message.what == 6){
                disconnectResult = message.obj.toString();
            }else if(message.what == 7){
                multiCastInfo = message.obj.toString();
            }
        }

        /**
         *
         * @param resourceMap mac path+name|path+name...,mac path+name|path+name... 样式
         * @return mac+path name， mac+path name ...样式
         */
        public Map<String,String> dataProcessing(Map<String,String> resourceMap){
            //keySet.size 和mac地址的数目相等
            Set<String> keySet =  resourceMap.keySet();
            String [] resource = new String[keySet.size()];
            String [] macSet = new String[keySet.size()];
            int i=0;
            for(String key:keySet){
                macSet[i] = key;
                resource[i] = resourceMap.get(key);
                String[] singleResource = resource[i].split("\\|");
                String [] macPlusPathSource = new String[singleResource.length-1];
                String [] nameSource = new String[singleResource.length-1];
                int j =0;
                //singleResource.length表示一个mac地址所对应的资源数目
                for(;j<singleResource.length-1;j++){
                    String temp[] = singleResource[j].split("=");
                    macPlusPathSource[j] = macSet[i]+"+"+temp[0];
                    nameSource[j] = temp[1];
                    resultResourceMap.put(macPlusPathSource[j],nameSource[j]);
                }
                i++;
            }
            return resultResourceMap;
        }
    }

    private static String getDeviceStatus(int deviceStatus){
        switch(deviceStatus){
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    public class WifiServiceAdapter extends ArrayAdapter<WifiDeviceWithLabel>{
        private List<WifiDeviceWithLabel> options;
        public WifiServiceAdapter(Context context,int resource,List<WifiDeviceWithLabel> items){
            super(context,resource,items);
            options = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_device,null);
            }
            WifiDeviceWithLabel service = options.get(position);
            Log.d(MainActivity.TRG,"000000000"+service);
            if(service!=null){
                TextView leftTop = (TextView) v.findViewById(R.id.device_name);
                TextView leftBottom = (TextView)v.findViewById(R.id.device_status);
                TextView right = (TextView)v.findViewById(R.id.group_label);
               // Log.d(MainActivity.TRG,service.device.deviceName+"-----------------------------"+service.label);
                if(leftTop!=null){
                    Log.d(MainActivity.TRG,service.getDevice().deviceName+"-----------------------------"+service);
                    leftTop.setText(service.getDevice().deviceName);
                }
                if(leftBottom!=null){
                    leftBottom.setText(getDeviceStatus(service.getDevice().status));
                }
                if(right!=null){
                    right.setText(service.getLabel());
                }
            }
            return v;
        }

        @Override
        public void add(WifiDeviceWithLabel WifiDeviceWithLabel){
            Log.d("每次调用add时传入的参数：", WifiDeviceWithLabel.getDevice().deviceName);
            updateTime++;
            if(ePeers.size()==0){
                ePeers.add(WifiDeviceWithLabel);
                deviceList.add(WifiDeviceWithLabel.getDevice());
                string = string+ WifiDeviceWithLabel.getDevice().deviceName+"/";
                string1 = string1+"/"+ WifiDeviceWithLabel.getDevice().deviceName;
            }else{
                string1 = string1+"/"+ WifiDeviceWithLabel.getDevice().deviceName;

                    int count = ePeers.size();
                    for(int i =0 ;i<ePeers.size();i++){
                        if(!WifiDeviceWithLabel.getDevice().deviceName.equals(ePeers.get(i).getDevice().deviceName)){
                            count--;
                        }
                    }
                    Log.d("count的值:",String.valueOf(count));
                    if(count == 0){
                        ePeers.add(WifiDeviceWithLabel);
                        deviceList.add(WifiDeviceWithLabel.getDevice());
                        string = string+ WifiDeviceWithLabel.getDevice().deviceName+"/";
                    }
            }
            Log.d("stirng中情况：",string);
            Log.d("eppers.size::::",String.valueOf(ePeers.size()));
            Log.d("deviceList.size::::",String.valueOf(deviceList.size()));
            //在5秒内，触发一次discovery Service,将结果放到test中，比较ePeers 和 test,如果相同，则，不调用notifyDataSetChanged,如果不同，将test赋给ePeers并清空test,调用notify。
            //问题是，如果不触发discovery 能接收到信息吗？ 好像可以接收额外的信息。将test，清空，看看test会不会有内容。
            notifyDataSetChanged();
        }

        @Override
        public void remove(WifiDeviceWithLabel wifiDeviceWithLabel){
            String name = wifiDeviceWithLabel.getDevice().deviceName;
            for(int i =0;i<ePeers.size();i++){
                if(name.equals(ePeers.get(i).getDevice().deviceName)){
                    ePeers.remove(i);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void update(WifiP2pDevice device,int status){
        this.mWifiP2pDevice = device;
        mWifiP2pDevice.status = device.status;
    }
    public void updateThisDevice(WifiP2pDevice Device){
        Log.d("查看本组信息","本组信息");
        //((MainActivity)getActivity()).requestGroupInfo();
        mWifiP2pDevice = Device;
        Log.d("本设备是不是组主？？？",String.valueOf(isGO));
        update(mWifiP2pDevice,mWifiP2pDevice.status);
        TextView textView = (TextView) mContentView.findViewById(R.id.my_name);
        textView.setText(mWifiP2pDevice.deviceName);
        textView = (TextView) mContentView.findViewById(R.id.my_status);
        textView.setText(getDeviceStatus(mWifiP2pDevice.status));
        //wifiP2pDevice.status修改设备状态，需要设备和wifiP2pDevice 对象关联
        Log.d("更新本设备信息：：：：：",mWifiP2pDevice.toString());

        //当设备状态更改，变为connected，更新ui
        String myStatus = new String();
        myStatus = textView.getText().toString();
        List<WifiP2pDevice> groupClientList = new ArrayList<>();
        if(myStatus.equals("Connected")){
            mContentView.findViewById(R.id.interGroup).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.createGroup).setVisibility(View.GONE);
            mContentView.findViewById(R.id.toBeGateway).setVisibility(View.VISIBLE);

            //标志位countOfFindResource标志该机是否已经运行过寻找本机资源子线程，如果已运行则不再执行
            if(countOfFindResouce<1){
                new Thread (new FindResourceThread()).start();
                countOfFindResouce++;
            }
            Log.d("资源清单字符串",messageHandler.mSource);
            /**
             * 页面跳转，进入第二个页面，对于组主需要向潜在的网关节点发起TCP连接，以获取网关节点所在组组主的MAC地址，更新本组的网关节点表。
             * 1、如果是设备第一次进入第二页面，将设备的资源信息发送给组主，
             * 2、如果该设备是网关节点，还需要将资源信息发送给LC组主。
             */
            mContentView.findViewById(R.id.interGroup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),IntraCommunication.class);
                    intent.putExtra("deviceAddress",mWifiP2pDevice.deviceAddress);
                    intent.putExtra("isGO",isGO);
                    intent.putExtra("isGW",isGW);
                    if(!isGO){
                        intent.putExtra("goMAC",goMAC);
                    }
                    startActivity(intent);
                    Log.d("资源清单字符串",mWifiP2pDevice.deviceAddress+"+"+messageHandler.mSource);
                    Log.d("firstInterGroup的值：",String.valueOf(firstInterGroup));
                    if(firstInterGroup){
                        Log.d("isGroupOwner()的值",String.valueOf(mWifiP2pDevice.isGroupOwner()));
                        if(!mWifiP2pDevice.isGroupOwner()){
                            Log.d("组员","我是组员组员组员");
                            new ClientSocket("192.168.49.1",30000,"write","source"+"+"+mWifiP2pDevice.deviceAddress+"*"+
                                    messageHandler.mSource).start();
                            Log.d("写写写","已开启客户端写");
                            firstInterGroup = false;
                        }
                        if(isGW){
                            Socket socket = icnOfGW.getISI().get("2");
                            SocketReuse socketReuse = new SocketReuse(socket,"write","cs"+"+"+mWifiP2pDevice.deviceAddress+"*"+
                                    messageHandler.mSource);
                            socketReuse.start();
                            Log.d("网关节点发送资源名称到LC组主","资源名称写到LC组主成功");
                        }
                    }
                }
            });
            /**
             * toBeGateway 按钮中的事件包括5个：
             * 1、与组主进行信息交互，将自身周围的组主信息发送给当前组主；2、组主将判断后的信息发送给组员 3、根据收到的信息建立LC连接
             * 所以既需要给组主发送消息，又需要接收组主返回的推荐信息，所以，点击toBeGateway 时要开启组员设备的服务端socket
             * 在增加一个事件，4、成为网关节点时，开启一个组间的TCP服务端，接收来自LC组主的单播，与LC组主建立一个Socket连接
             * 同时，对于相关的组主节点和LC组主节点需要更新网关信息表；5、开启组内组播接收和组间组播接收，用来接收组主节点连接信息更新后，网
             * 关节点所发出的变化性消息，以及组主节点由于网关节点新增所引起变化性消息激发所发出的自身连接信息的接收。
             */
            mContentView.findViewById(R.id.toBeGateway).setOnClickListener(new View.OnClickListener(){
                String temp = "";
                String aimGOInfo;
                @Override
                public void onClick(View v){
                    List<String> nearbyGO = new ArrayList<>();
                    if(ePeers.size()==0||ePeers.size()==1){
                        Log.d("附近没有其他组存在","您暂时无法成为网关节点");
                    }else{
                        for(int i =0;i<ePeers.size();i++){
                            temp = ePeers.get(i).getDevice().deviceAddress+"/"+ePeers.get(i).getLabel();
                            nearbyGO.add(temp);
                        }
                        temp = nearbyGO.toString();
                        Log.d("附近组主信息----",temp);
                        gmSocket = new ClientSocket("192.168.49.1",30000,"write","toBeGateway"+"+"+mWifiP2pDevice.deviceAddress+"+"+temp);
                        gmSocket.start();
                        GMServer = new MyServerSocket(goMAC,wifiManager,ePeers,mWifiP2pDevice.deviceAddress,30002,"read", "GW");
                        GMServer.start();
                        isGW = true;
                    }
                    //网关节点与LC组主的第一次socket通信
                    UnicastSever unicastSever = new UnicastSever(goMAC,mWifiP2pDevice.deviceAddress,50004,"read");
                    unicastSever.start();
                    //网关节点(p2p口)开启组内组播监听--这里使用组播，可能回有不可靠的情况存在，可以考虑组主轮询发送TCP，或者是可靠UDP 组内组播，接收端口号 50000 地址是239.2.1.2
                    MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(goMAC,mWifiP2pDevice.deviceAddress,50000,"recv","239.2.1.2",
                            GetIpAddrInP2pGroup.getLocalIPAddress(),GetIpAddrInP2pGroup.getGWWlanIP(GetIpAddrInP2pGroup.getWlanMac()),0,false);
                    myMulticastSocketThread.start();
                    //网关节点(wlan口)开启组间组播监听--这里使用组播，可能回有不可靠的情况存在，可以考虑组主轮询发送TCP，或者是可靠UDP，这里是监听LC组主传来的信息 组间组播 接收端口号50000 地址是239.2.1.2
                    MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(goMAC,GetIpAddrInP2pGroup.getWlanMac(),50000,"recv","239.2.1.2",
                            GetIpAddrInP2pGroup.getGWWlanIP(GetIpAddrInP2pGroup.getWlanMac()),GetIpAddrInP2pGroup.getLocalIPAddress(),1,false);
                    myMulticastSocketThread1.start();
                }
            });
        }
        else if(myStatus.equals("Available")){
            mContentView.findViewById(R.id.interGroup).setVisibility(View.GONE);
            mContentView.findViewById(R.id.createGroup).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.toBeGateway).setVisibility(View.GONE);
        }
    }
    public class FindResourceThread implements Runnable{
        @Override
        public void run() {
            FindResources findResources = new FindResources();
            String thisDeviceResources =  findResources.getResources();
            Log.d("寻找资源的子线程：",thisDeviceResources);
            //将thisDeviceResources 从子线程传输到主线程中。

            Message message = Message.obtain();
            message.what = IS_RESOURCE;
            message.obj = thisDeviceResources;
            Log.d("寻找资源的子线程中message的信息：",message.obj.toString());
            messageHandler.sendMessage(message);
        }
    }
    @Override
    //点击目标节点，获取目标节点的信息
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiDeviceWithLabel service = (WifiDeviceWithLabel) getListAdapter().getItem(position);
        WifiP2pDevice device = service.getDevice();
        goMAC = device.deviceAddress;
        Log.d("组主的MAC地址",goMAC);
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = device.deviceAddress;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        Log.d("DEVICE---------",device.toString());
        //这个device是组主，可以获取相关的组信息吗？
        ((DeviceActionListener)getActivity()).connect(wifiP2pConfig,device);
        //开启一个服务端线程，针对当前节点（GM或者GW）开始回溯数据或者接收回溯的数据
        MyServerSocket myServerSocket = new MyServerSocket(60006,"read","dataBack",mWifiP2pDevice.deviceAddress);
        myServerSocket.start();
    }


    /**
     * 用peers去记录当前设备附近的组主信息，用于向当前组主发送邻近组主信息
     */
    @Override
   public void onPeersAvailable(WifiP2pDeviceList peerList){
        if(processDialog!=null&&processDialog.isShowing()){
            processDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        Log.d(MainActivity.TRG,"调用次数"+i++);
        for(int i =0;i<peers.size();i++){
            if(!peers.get(i).isGroupOwner()){
                peers.remove(i);
            }
        }
    }
    @Override
    //网关节点通过Wifi连接到LC组主后，也会触发该方法，通过该方法LC组主可以在连接后拿到网关节点的MAC地址
    //当组内设备离开，组主节点可以通过该方法得知，并对CS表进行更新以及组主节点表的更新
    public void onGroupInfoAvailable(WifiP2pGroup group){
        //macOfGWs是局部变量，每次调用onGroupInfoAvailable方法，macOfGWs都是新的值
        String macOfGWs = null;
        if(!isPublished){
            if(isGO){
                wifiP2pGroup = group;
                Log.d("这是第一个组赞赞赞赞赞赞组信息",wifiP2pGroup.toString());
                label = label+"/"+wifiP2pGroup.getNetworkName()+"/"+wifiP2pGroup.getPassphrase();
                ((MainActivity)getActivity()).publishService(label);
                Log.d(MainActivity.TRG,"调用publishService函数成功啦啦啦啦啦啦啦啦啦"+label);
                isPublished = true;
                //组主开启一个组播接收端，接收来自网关节点关于组主连接信息的转发
                MultiCast multiCast = new MultiCast(50005,"recv","239.2.1.3");
                Log.d("组主开启组播监听","组主开始组播监听了ROT维持");
                multiCast.start();
            }
        }
        String aimMac = null;
        Log.d("组内成员数量",String.valueOf(group.getClientList().size()));
        if(groupSize != group.getClientList().size()){
            if(groupSize > group.getClientList().size()){
                Collection<WifiP2pDevice> tempList = group.getClientList();
                for(WifiP2pDevice d : tempList){
                    for(int j = 0;j<clientMacList.size();j++){
                        if(clientMacList.get(j)!=d.deviceAddress){
                            aimMac = clientMacList.get(j);
                            Log.d("离开本组的设备mac地址：",aimMac);
                            //设备离开时，更新RN资源表
                            messageHandler.macOfAll.remove(aimMac);
                            messageHandler.movieMap.remove(aimMac);
                            messageHandler.musicMap.remove(aimMac);
                            messageHandler.packageMap.remove(aimMac);
                            messageHandler.wordMap.remove(aimMac);
                            Log.d("当前时刻的电影资源信息",messageHandler.movieMap.toString());
                            //如果是网关节点，则更新组主节点的网关节点表
                            String lcGOMac = null;
                            if(BasicWifiDirectBehavior.icnOfGO.getGM().containsKey(aimMac)){
                                lcGOMac = BasicWifiDirectBehavior.icnOfGO.getGM().get(aimMac);
                                GateWay gateWay = new GateWay(aimMac,mWifiP2pDevice.deviceAddress);
                                BasicWifiDirectBehavior.icnOfGO.updateGMTable(gateWay);
                                try{
                                    if(icnOfGO.getISI().get(aimMac)!=null){
                                        icnOfGO.getISI().get(aimMac).close();
                                    }
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                            //将变化的信息转发给组主所连接的网关节点
                            String changedGMTContent = mWifiP2pDevice.deviceAddress+"-"+lcGOMac+"-"+"info"+"gwLeave";
                            MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",changedGMTContent);
                            myMulticastSocketThread.start();
                        }
                    }
                }
            }
            groupSize = group.getClientList().size();
            Collection<WifiP2pDevice> deviceList = group.getClientList();
            for(WifiP2pDevice d : deviceList){
                clientMacList.add(d.deviceAddress);
            }
        }
        mWifiP2pGroup = group;
        Log.d("本机位组主",String.valueOf(mWifiP2pGroup.isGroupOwner()));
        Log.d("组情况变化了","这里也可以展现变化的好吧");
        ArrayList<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
        list.addAll(group.getClientList());
        Log.d("组内成员列表信息：：：：",group.getClientList().toString());

        //找出status值为4的设备，即为通过WiFi连接到本组的网关节点。设备刚连接的时候arp表内还没有此项内容。。。
        //并与本设备的网关节点表进行对比，找到新的准网关节点
        Collection<WifiP2pDevice> clientDevices = group.getClientList();
        for(WifiP2pDevice device: clientDevices){
                if(device.status == 4){
                    if(icnOfGO.getGM().size()!=0){
                        for(String s:icnOfGO.getGM().keySet()){
                            if(device.deviceAddress.compareToIgnoreCase(s)!=0){
                                //字符串的处理工作
                                if(macOfGWs!=null){
                                    macOfGWs = macOfGWs+"+"+device.deviceAddress;
                                }else{
                                    macOfGWs = device.deviceAddress;
                                }
                            }
                        }
                    }else{
                        if(macOfGWs!=null){
                            macOfGWs = macOfGWs+"+"+device.deviceAddress;
                        }else{
                            macOfGWs = device.deviceAddress;
                        }
                    }
                }
        }
        //如果是组主，调用unicastToGWS方法，建立组主和网关节点的第一次socket连接
        if(isGO){
            unicastToGWS(macOfGWs);
            if(macOfGWs!=null){
                Log.d("需要发送单播的网关节点信息",macOfGWs);
            }
        }
        TextView textView = mContentView.findViewById(R.id.nameOfResource);
        allSourceOfThisGroup = messageHandler.movieMap.toString()+messageHandler.musicMap.toString()+
                messageHandler.packageMap.toString()+messageHandler.wordMap.toString();
        Log.d("组内资源信息",allSourceOfThisGroup);
        if(allSourceOfThisGroup!=null){
            textView.setText(allSourceOfThisGroup);
        }
        if(group.getClientList().size()==0){
            textView.setText("无连接");
        }
    }
    private void unicastToGWS(String macOfGWs){
        if(macOfGWs!=null){
            Log.d("LC组主的网关信息",macOfGWs);
            String  [] macsOfGWs = macOfGWs.split("\\+");
            //为新的网关节点建立socket，需要过滤掉老的
            for(int i =0;i<macsOfGWs.length;i++){
                String IP = GetIpAddrInP2pGroup.getGWWlanIP(macsOfGWs[i]);
                while(true){
                    if(IP.equals("11")){
                        try{
                            Thread thread = new Thread();
                            thread.sleep(1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        IP = GetIpAddrInP2pGroup.getGWWlanIP(macsOfGWs[i]);
                    }else{
                        break;
                    }
                }
                UnicastClient unicastClient = new UnicastClient(IP,50004,"write",mWifiP2pDevice.deviceAddress);
                unicastClient.start();
                Log.d("LC组主发送组间单播成功",IP);
            }
        }
    }
    public interface DeviceActionListener{
      void connect(WifiP2pConfig wifiP2pConfig, WifiP2pDevice wifiP2pDevice);
      void disconnect();
      void publishService(String string);
      void createGroup(WifiP2pDevice wifiP2pDevice);
      void removeLocalService();
      void requestGroupInfo();
    }
}

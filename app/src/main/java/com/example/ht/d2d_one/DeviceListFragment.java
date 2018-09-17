package com.example.ht.d2d_one;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyServerSocket;
import com.example.ht.d2d_one.util.FindResources;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,WifiP2pManager.GroupInfoListener{
    public int i=1;
    private boolean firstInterGroup = true;
    View mContentView = null;
    ProgressDialog processDialog =null;
    private final static int IS_RESOURCE = 1 ;
    private String allSourceFromClient = null;
    private  WifiP2pDevice mWifiP2pDevice;
    private  WifiP2pInfo mWifiP2pInfo;
    private WifiP2pGroup mWifiP2pGroup;
    private int countOfFindResouce = 0;
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
    Boolean isGroupMemberNull = true;
    Boolean isGO = true;
    private List<WifiDeviceWithLabel> ePeers = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiDeviceWithLabel> test = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();
    private String string = "/";
    private String string1 = "";
    private int updateTime =0;
    public static MessageHandler messageHandler = new MessageHandler();
    WifiServiceAdapter wifiServiceAdapter;

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        wifiServiceAdapter = new WifiServiceAdapter(getActivity(),R.layout.row_device,ePeers);
        this.setListAdapter(wifiServiceAdapter);
        //this.setListAdapter(new WifiServiceAdapter(getActivity(),R.layout.row_device,ePeers));
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        mContentView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceActionListener)getActivity()).disconnect();
                wifiServiceAdapter.clear();
                if(isGO){
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
                        EditText editText = (EditText)DialogView.findViewById(R.id.label);
                        label = editText.getText().toString();
                        ((MainActivity)getActivity()).publishService(label);
                        Log.d(MainActivity.TRG,"调用publishService函数成功啦啦啦啦啦啦啦啦啦"+label);



                        Log.d("组主????????????????",String.valueOf(mWifiP2pDevice.isGroupOwner()));
                        //new MyServerSocket(mWifiP2pDevice.deviceAddress,30000,"read").start();
                        myServerSocket = new MyServerSocket(mWifiP2pDevice.deviceAddress,30000,"read");
                        myServerSocket.start();
                        Log.d("服务socket","组主开启服务端socket");
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

    public static class MessageHandler extends Handler{
        private List<String> sourceList = new ArrayList<>();
        private String sourceFromClient = null;
        private String mSource = "1";
        @Override
        public void handleMessage(Message message){
            if(message.what ==IS_RESOURCE){
                mSource = (String)message.obj;
                Log.d("子线程发来的message",mSource);
            }else if(message.what == 2){
                sourceFromClient = (String)message.obj;
                if(sourceFromClient!=null){
                    sourceList.add(sourceFromClient);
                }
                if(sourceFromClient!=null){
                    Log.d("组员发来的资源信息为：",sourceFromClient);
                }
            }
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
        ((MainActivity)getActivity()).requestGroupInfo(mWifiP2pDevice);
        mWifiP2pDevice = Device;
        Log.d("本设备是不是组主？？？",String.valueOf(mWifiP2pDevice.isGroupOwner()));
        update(mWifiP2pDevice,mWifiP2pDevice.status);
        TextView textView = (TextView) mContentView.findViewById(R.id.my_name);
        textView.setText(mWifiP2pDevice.deviceName);
        textView = (TextView) mContentView.findViewById(R.id.my_status);
        textView.setText(getDeviceStatus(mWifiP2pDevice.status));
        //wifiP2pDevice.status修改设备状态，需要设备和wifiP2pDevice 对象关联
        Log.d("更新本设备信息：：：：：",mWifiP2pDevice.toString());

        //当设备状态更改，变为connected，设备进入组内页面，并将该设备对应的组的信息展示到组页面中。
        String myStatus = new String();
        myStatus = textView.getText().toString();
        List<WifiP2pDevice> groupClientList = new ArrayList<>();
        if(myStatus.equals("Connected")){
            mContentView.findViewById(R.id.interGroup).setVisibility(View.VISIBLE);
            //标志位countOfFindResource标志该机是否已经运行过寻找本机资源子线程，如果已运行则不再执行
            if(countOfFindResouce<1){
                new Thread (new FindResourceThread()).start();
                countOfFindResouce++;
            }
            Log.d("资源清单字符串",messageHandler.mSource);
                mContentView.findViewById(R.id.interGroup).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //页面跳转
                        Intent intent = new Intent(getActivity(),Main2Activity.class);
                        startActivity(intent);
                        Log.d("资源清单字符串",mWifiP2pDevice.deviceAddress+"+"+messageHandler.mSource);
                        if(firstInterGroup){
                            if(!mWifiP2pDevice.isGroupOwner()){
                                Log.d("组员","我是组员组员组员");
                                new ClientSocket("192.168.49.1",30000,"write",mWifiP2pDevice.deviceAddress+"+"+
                                        messageHandler.mSource).start();
                                Log.d("写写写","已开启客户端写");
                            }
                            firstInterGroup = false;
                        }
                    }
                });
        }
        else if(myStatus.equals("Available")){
            mContentView.findViewById(R.id.interGroup).setVisibility(View.GONE);
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
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = device.deviceAddress;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        Log.d("DEVICE---------",device.toString());
        //这个device是组主，可以获取相关的组信息吗？
        ((DeviceActionListener)getActivity()).connect(wifiP2pConfig,device);
    }

//    @Override WifiP2pGroupList类是隐藏的，所以需要用反射机制（方案一：获取附近组的列表）
//    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups){
//
//    }

    //方案二：判断获取的设备是否为组主，过滤出来。
    @Override
   public void onPeersAvailable(WifiP2pDeviceList peerList){
//        if(processDialog!=null&&processDialog.isShowing()){
//            processDialog.dismiss();
//        }
//        peers.clear();
//        peers.addAll(peerList.getDeviceList());
//        Log.d(MainActivity.TRG,"调用次数"+i++);
//        Log.d(MainActivity.TRG,"周围设备信息："+peers);
//        //过滤组主
//        //        for(int i =0;i<peers.size();i++){
//        //            if(!peers.get(i).isGroupOwner()){
//        //                peers.remove(i);
//        //            }
//        //        }
//        //重新加载页面notifyDataSetChanged(),这是arrayAdapter中的方法
//      ((WifiPeerListAdapter)getListAdapter()).notifyDataSetChanged();
//        if(peers.size()==0){
//            Log.d(MainActivity.TRG,"No device found");
//            return;
//        }
    }
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo){
        //每有设备进组，都会触发onConnectionInfoAvailable函数
        mWifiP2pInfo = wifiP2pInfo;
        isGO = mWifiP2pInfo.isGroupOwner;
        Log.d("本设备是不是组主------","mWifiP2pInfo.isGroupOwner"+"="+String.valueOf(isGO));
        Log.d("mWifiP2pInfo的信息好吧",mWifiP2pInfo.toString());
    }

    public void onGroupInfoAvailable(WifiP2pGroup group){
        mWifiP2pGroup = group;
        Log.d("组主设备物理地址",group.getOwner().deviceAddress);
        Log.d("本机设备物理地址",mWifiP2pDevice.deviceAddress);
        //boolean t=mWifiP2pGroup.isGroupOwner();
       // Log.d("本机位组主",String.valueOf(t));
        Log.d("本机位组主",String.valueOf(mWifiP2pGroup.isGroupOwner()));
        Log.d("组情况变化了","这里也可以展现变化的好吧");
        ArrayList<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
        Collection<WifiP2pDevice> deviceList = group.getClientList();
        list.addAll(group.getClientList());
        Log.d("组内成员列表信息：：：：",group.getClientList().toString());

        Log.d("资源名称列表内容：：：：：",messageHandler.sourceList.toString());
        TextView textView = mContentView.findViewById(R.id.nameOfResource);
        if(messageHandler.sourceList!=null){
            textView.setText(messageHandler.sourceList.toString());
        }
        if(group.getClientList().size()==0){
            textView.setText("无连接");
        }
    }
    public interface DeviceActionListener{
      void connect(WifiP2pConfig wifiP2pConfig,WifiP2pDevice wifiP2pDevice);
      void disconnect();
      void publishService(String string);
      void createGroup(WifiP2pDevice wifiP2pDevice);
      void removeLocalService();
      void requestGroupInfo(WifiP2pDevice wifiP2pDevice);
    }
}

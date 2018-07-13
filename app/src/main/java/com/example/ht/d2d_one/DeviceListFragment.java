package com.example.ht.d2d_one;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener{
    public int i=1;
    View mContentView = null;
    ProgressDialog processDialog =null;
    WifiP2pDevice wifiP2pDevice;
    WifiP2pManager manager;
    String label = "";
    ProgressDialog progressDialog = null;
    Boolean isGO = false;
    private List<WifiDeviceWithLabel> ePeers = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiDeviceWithLabel> test = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();
    private String string = "/";
    private String string1 = "";
    private int updateTime =0;

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WifiServiceAdapter(getActivity(),R.layout.row_device,ePeers));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        mContentView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceActionListener)getActivity()).disconnect();
                Log.d("本设备是不是组主？？？",String.valueOf(isGO));
                if(isGO){
                    ((DeviceActionListener)getActivity()).removeLocalService();
                }
            }
        });
        mContentView.findViewById(R.id.createGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用publishservice函数，写到mainactivity 中。
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View DialogView = layoutInflater.inflate(R.layout.creategroup,null);
                AlertDialog dlg = new AlertDialog.Builder(getActivity()).
                        setTitle("建组").setIcon(R.mipmap.ic_launcher).setView(DialogView).
                        setPositiveButton("publish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((DeviceActionListener)getActivity()).createGroup();
                        EditText editText = (EditText)DialogView.findViewById(R.id.label);
                        label = editText.getText().toString();
                        ((MainActivity)getActivity()).publishService(label);
                        Log.d(MainActivity.TRG,"调用publishService函数成功啦啦啦啦啦啦啦啦啦"+label);
                    }
                }).create();
                dlg.show();
            }
        });
        return mContentView;
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
        this.wifiP2pDevice = device;
        wifiP2pDevice.status = device.status;
    }
    public void updateThisDevice(WifiP2pDevice wifiP2pDevice){
        isGO = wifiP2pDevice.isGroupOwner();
        update(wifiP2pDevice,wifiP2pDevice.status);
        TextView textView = (TextView) mContentView.findViewById(R.id.my_name);
        textView.setText(wifiP2pDevice.deviceName);
        textView = (TextView) mContentView.findViewById(R.id.my_status);
        textView.setText(getDeviceStatus(wifiP2pDevice.status));
        //wifiP2pDevice.status修改设备状态，需要设备和wifiP2pDevice 对象关联
        Log.d("更新本设备信息：：：：：",wifiP2pDevice.toString());

        //当设备状态更改，变为connected，设备进入组内页面，并将该设备对应的组的信息展示到组页面中。
        String myStatus = new String();
        myStatus = textView.getText().toString();
        List<WifiP2pDevice> groupClientList = new ArrayList<>();
        if(myStatus.equals("Connected")){
            //如果设备信息更新为connected，则显示进组按钮
            mContentView.findViewById(R.id.interGroup).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.interGroup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).requestGroupInfo();
                }
            });
        }else if(myStatus.equals("Available")){
            mContentView.findViewById(R.id.interGroup).setVisibility(View.GONE);
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
    public interface DeviceActionListener{
      void connect(WifiP2pConfig wifiP2pConfig,WifiP2pDevice wifiP2pDevice);
      void disconnect();
      void publishService(String string);
      void createGroup();
      void removeLocalService();
      void requestGroupInfo();
    }
}

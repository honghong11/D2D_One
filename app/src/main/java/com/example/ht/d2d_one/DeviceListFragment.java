package com.example.ht.d2d_one;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener{
    public int i=1;
    View mContentView = null;
    ProgressDialog processDialog =null;
    WifiP2pDevice wifiP2pDevice;
    WifiP2pManager manager;
    String label = "";
    private List<WifiDeviceWithLabel> ePeers = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiDeviceWithLabel> test = new ArrayList<WifiDeviceWithLabel>();
    private List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();
    private String string = "/";
    private String string1 = "";

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
                ((DeviceActionListener)getActivity()).removeLocalService();
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
            test.add(WifiDeviceWithLabel);
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
            notifyDataSetChanged();
        }
    }

    public void update(WifiP2pDevice device,int status){
        this.wifiP2pDevice = device;
        wifiP2pDevice.status = device.status;
    }
    public void updateThisDevice(WifiP2pDevice wifiP2pDevice){
        update(wifiP2pDevice,wifiP2pDevice.status);
        TextView textView = (TextView) mContentView.findViewById(R.id.my_name);
        textView.setText(wifiP2pDevice.deviceName);
        textView = (TextView) mContentView.findViewById(R.id.my_status);
        textView.setText(getDeviceStatus(wifiP2pDevice.status));
        //wifiP2pDevice.status修改设备状态，需要设备和wifiP2pDevice 对象关联
        Log.d("更新本设备信息：：：：：",wifiP2pDevice.toString());
    }
    @Override
    //点击目标节点，获取目标节点的信息
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice Device = (WifiP2pDevice) getListAdapter().getItem(position);
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = Device.deviceAddress;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;
        Log.d("DEVICE---------",Device.toString());
        ((DeviceActionListener)getActivity()).connect(wifiP2pConfig);

//设备详细信息，用于进组之后的操作，作为Main2Activity第二页面。现在先不管了。。
//        String device = new String();
//        Intent intent = new Intent(getActivity(),Main2Activity.class);
//        intent.putExtra(device,Device.toString());
//        startActivity(intent);
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
      void connect(WifiP2pConfig wifiP2pConfig);
      void disconnect();
      void publishService(String string);
      void createGroup();
      void removeLocalService();
    }
}

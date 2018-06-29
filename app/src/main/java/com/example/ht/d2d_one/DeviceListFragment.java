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
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private List<wifiDeviceWithLabel> epeers = new ArrayList<wifiDeviceWithLabel>();
    private List<wifiDeviceWithLabel> data = new ArrayList<wifiDeviceWithLabel>();
    List<wifiDeviceWithLabel> peers = new ArrayList<wifiDeviceWithLabel>();
    WifiP2pDevice device = new WifiP2pDevice();

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//手动给数据,可以
        device.deviceName = "11";
        device.status=1;
        label = "youxi";
        wifiDeviceWithLabel wifiDeviceWithLabel = new wifiDeviceWithLabel(device,label);
        data.add(0,wifiDeviceWithLabel);
//       this.setListAdapter(new WifiServiceAdapter(getActivity(),R.layout.row_device,data));


//        this.setListAdapter(new WifiServiceAdapter(this.getActivity(),
//                android.R.layout.simple_list_item_2, android.R.id.text1,
//                new ArrayList<wifiDeviceWithLabel>()));
        this.setListAdapter(new WifiServiceAdapter(getActivity(),R.layout.row_device,epeers));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        mContentView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceActionListener)getActivity()).disconnect();
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
                        Log.d(MainActivity.TRG,"调用createGroup函数成功啦啦啦啦啦啦啦啦啦");
                        EditText editText = (EditText)DialogView.findViewById(R.id.label);
                        label = editText.getText().toString();
                        ((MainActivity)getActivity()).publishService(label);
                        Log.d(MainActivity.TRG,"调用publishService函数成功啦啦啦啦啦啦啦啦啦");
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

// //   为ListFragment保存WiFiDevice list 的array adapter
//    public class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice>{
//        private List<WifiP2pDevice> items;
//        //适用仅有设备信息
//        public WifiPeerListAdapter(Context context,int textViewResource,List<WifiP2pDevice> object){
//            super(context,textViewResource,object);
//            items = object;
//        }
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent){
//            View v = convertView;
//            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
//            if(v==null){
//                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v = vi.inflate(R.layout.row_device,null);
//            }
//            WifiP2pDevice device = items.get(position);
//            if(device!=null){
//                TextView leftTop = v.findViewById(R.id.device_name);
//                TextView leftBottom = v.findViewById(R.id.device_status);
//                if(leftTop!=null){
//                    leftTop.setText(device.deviceName);
//                }
//                if(leftBottom!=null){
//                    leftBottom.setText(getDeviceStatus(device.status));
//                }
//            }
//            return v;
//        }
//    }

    public class WifiServiceAdapter extends ArrayAdapter<wifiDeviceWithLabel>{
        private List<wifiDeviceWithLabel> options;
        public WifiServiceAdapter(Context context,int resource,List<wifiDeviceWithLabel> items){
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
            wifiDeviceWithLabel service = options.get(position);
            Log.d(MainActivity.TRG,"000000000"+service);
            if(service!=null){
                TextView leftTop = (TextView) v.findViewById(R.id.device_name);
                TextView leftBottom = (TextView)v.findViewById(R.id.device_status);
                TextView right = (TextView)v.findViewById(R.id.group_label);
               // Log.d(MainActivity.TRG,service.device.deviceName+"-----------------------------"+service.label);
                if(leftTop!=null){
                    Log.d(MainActivity.TRG,service.device.deviceName+"-----------------------------"+service);
                    leftTop.setText(service.device.deviceName);
                }
                if(leftBottom!=null){
                    leftBottom.setText(getDeviceStatus(service.device.status));
                }
                if(right!=null){
                    right.setText(service.label);
                }
            }
            return v;
        }
        @Override
        public void add(wifiDeviceWithLabel service){
            if(epeers.size()==0){
                epeers.add(service);
            }else{
                if (service.device.deviceName != epeers.get(epeers.size()-1).device.deviceName){
                    epeers.add(service);
                }
            }
            Log.d(MainActivity.TRG,"iiiiiiiiiiiiiiiiii"+epeers.get(0).label);
            setNotifyOnChange(true);
            Log.d(MainActivity.TRG,"iiiiiiiiiiiiiiiiii"+epeers.toString());
        }
        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            Log.d(MainActivity.TRG,"tiaoyonglllllllllllllllllllllll");
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
    }
}

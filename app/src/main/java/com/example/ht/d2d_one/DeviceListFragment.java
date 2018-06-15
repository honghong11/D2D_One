package com.example.ht.d2d_one;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener{
    View mContentView = null;
    ProgressDialog processDialog =null;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //需要将adapter和资源文件关联
        this.setListAdapter(new WifiPeerListAdapter(getActivity(),R.layout.row_device,peers));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        mContentView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceActionListener)getActivity()).disconnect();
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
    //为ListFragment保存WiFiDevice list 的array adapter
    public class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice>{
        private List<WifiP2pDevice> items;
        public WifiPeerListAdapter(Context context,int textViewResource,List<WifiP2pDevice> object){
            super(context,textViewResource,object);
            items = object;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_device,null);
            }
            WifiP2pDevice device = items.get(position);
            if(device!=null){
                TextView top = v.findViewById(R.id.device_name);
                TextView bottom = v.findViewById(R.id.device_status);
                if(top!=null){
                    top.setText(device.deviceName);
                }
                if(bottom!=null){
                    bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;
        }
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
    }
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList){
        if(processDialog!=null&&processDialog.isShowing()){
            processDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        Log.d(MainActivity.TRG,"22222222222"+peers);
        //重新加载页面notifyDataSetChanged(),这是arrayAdapter中的方法、
        ((WifiPeerListAdapter)getListAdapter()).notifyDataSetChanged();
        if(peers.size()==0){
            Log.d(MainActivity.TRG,"No device found");
            return;
        }

    }
    public interface DeviceActionListener{
      void connect(WifiP2pConfig wifiP2pConfig);
      void disconnect();
    }
}

package com.example.ht.d2d_one;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetailFragment extends ListFragment{
    private View mContentView = null;
    private String label = "";
    public void onActivityCreated(Bundle saveInstanceState){
        super.onActivityCreated(saveInstanceState);
//        this.setListAdapter(new DeviceDetailFragment.WifiServiceAdapter(this.getActivity(),
//                R.layout.service_list, android.R.id.text1,
//                new ArrayList<WifiDeviceWithLabel>()));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle saveInstanceState){
        mContentView = inflater.inflate(R.layout.device_detail,null);
        return mContentView;
    }
    //将service_list.xml和WifiServiceListAdapter相关联
//    public class WifiServiceAdapter extends ArrayAdapter<WifiDeviceWithLabel>{
//        private List<WifiDeviceWithLabel> items;
//        public WifiServiceAdapter(Context context, int resource,
//                                   int textViewResourceId, List<WifiDeviceWithLabel> items) {
//            super(context, resource, textViewResourceId, items);
//            this.items = items;
//        }
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent){
//            View v = convertView;
//            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
//            if(v==null){
//                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v = vi.inflate(R.layout.service_list,null);
//            }
//            WifiDeviceWithLabel service = items.get(position);
//            String string = label;
////            WifiDeviceWithLabel extendDevice = extendItems.get(position);
//            if(service!=null){
//                TextView leftTop = v.findViewById(R.id.service_name);
//                TextView leftBottom = v.findViewById(R.id.service_status);
//                TextView right = v.findViewById(R.id.service_label);
//                Log.d(MainActivity.TRG,service.device.deviceName+"-----------------------------"+service.label);
//                if(leftTop!=null){
//                    leftTop.setText(service.device.deviceName);
//                }
//                if(leftBottom!=null){
//                    leftBottom.setText(getDeviceStatus(service.device.status));
//                }
//                if(right!=null){
//                    right.setText(service.label);
//                }
//            }
//            return v;
//        }
//    }
//
//    private static String getDeviceStatus(int deviceStatus){
//        switch(deviceStatus){
//            case WifiP2pDevice.AVAILABLE:
//                return "Available";
//            case WifiP2pDevice.CONNECTED:
//                return "Connected";
//            case WifiP2pDevice.FAILED:
//                return "Failed";
//            case WifiP2pDevice.INVITED:
//                return "Invited";
//            case WifiP2pDevice.UNAVAILABLE:
//                return "Unavailable";
//            default:
//                return "Unknown";
//        }
//    }
}

package com.example.ht.d2d_one;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import java.util.ArrayList;
import java.util.List;

public class wifiDeviceWithLabel{
    WifiP2pDevice device = null;
    String label = null;
    public wifiDeviceWithLabel(String deviceName,int status,String label){
        device.deviceName = deviceName;
        device.status = status;
        this.label = label;
    }
    public wifiDeviceWithLabel(WifiP2pDevice device,String label){
        this.device = device;
        this.label = label;
    }
    public wifiDeviceWithLabel(){

    }
}

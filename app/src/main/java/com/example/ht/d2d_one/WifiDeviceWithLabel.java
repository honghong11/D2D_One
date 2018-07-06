package com.example.ht.d2d_one;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import java.util.ArrayList;
import java.util.List;


public class WifiDeviceWithLabel {
    private WifiP2pDevice device;
    private String label;
    public WifiDeviceWithLabel(String deviceName, int status, String label){
        device.deviceName = deviceName;
        device.status = status;
        this.label = label;
    }
    public WifiDeviceWithLabel(WifiP2pDevice device, String label){
        this.device = device;
        this.label = label;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

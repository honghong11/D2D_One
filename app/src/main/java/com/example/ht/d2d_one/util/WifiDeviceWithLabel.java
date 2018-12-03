package com.example.ht.d2d_one.util;

import android.net.wifi.p2p.WifiP2pDevice;

public class WifiDeviceWithLabel {
    private WifiP2pDevice device;
    private String label;
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public WifiDeviceWithLabel(WifiP2pDevice device, String label){
        this.device = device;
        this.label = label;
    }

    public WifiDeviceWithLabel(WifiP2pDevice device){
        this.device = device;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
}

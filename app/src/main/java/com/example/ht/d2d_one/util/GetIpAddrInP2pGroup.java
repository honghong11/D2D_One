package com.example.ht.d2d_one.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

public class GetIpAddrInP2pGroup {

    private final static String p2pInt = "p2p0";
    //对于华为的手机，p2p-wlan0-0,p2p-wlan0-1,p2p-wlan0-2...需要对前缀进行匹配
    private final static String p2pIntHuawei = "p2p-wlan0";

    public static String getGWWlanIP(String MAC){
        String IP = "11";
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while((line = br.readLine())!=null){
                Log.d("arp 文件内容",line);
                String [] arpInfo = line.split(" +");
                if(arpInfo.length>=4){
                    if(arpInfo[3].equals(MAC)){
                        IP = arpInfo[0];
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                br.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return IP;
    }

    /**
     * 通过网络接口取
     * @return
     */
    public static String getWlanMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    public static String getIPFromMac(String MAC) {
        /*
         * method modified from:
         *
         * http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
         *
         * */
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
                    if (device.matches(".*" +p2pInt+ ".*")){
                        String mac = splitted[3];
                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static String getLocalIPAddress() {
        /*
         * modified from:
         *
         * http://thinkandroid.wordpress.com/2010/03/27/incorporating-socket-programming-into-your-applications/
         *
         * */
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String iface = intf.getName();
                    if(iface.matches(".*" +p2pInt+ ".*")||iface.matches(".*"+p2pIntHuawei+".*")){
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return getDottedDecimalIP(inetAddress.getAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("AndroidNetworkAddress", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            Log.e("AndroidNetworkAddress", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private static String getDottedDecimalIP(byte[] ipAddr) {
        /*
         * ripped from:
         *
         * http://stackoverflow.com/questions/10053385/how-to-get-each-devices-ip-address-in-wifi-direct-scenario
         *
         * */
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }
}

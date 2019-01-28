package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网关节点最重要的性质是什么？桥梁，连接到不同的组。
 * 现在主要是两个作用
 * 1、确定准网关节点和哪个组主节点建立LC连接
 *  ToBeGatewayNode(Map,String)
 *  ToBeGatewayNode(String)
 * 2、向外传递数据时根据最小覆盖原则确定从哪个网关节点出
 */
public class GateWay {
    private String macOfDevice;
    private String macOfLcGO;
    private String macOfP2pGO;
    private String tagOfLcGO;
    private String tagOfP2pGO;
    private boolean isGateWay = false;

    public String getMacOfDevice() {
        return macOfDevice;
    }

    public void setGateWay(boolean gateWay) {
        isGateWay = gateWay;
    }

    public GateWay(boolean isGateWay, String mac, String macOfP2pGO, String macOfLcGO){
        this.isGateWay = isGateWay;
        this.macOfDevice = mac;
        this.macOfLcGO = macOfLcGO;
        this.macOfP2pGO = macOfP2pGO;
    }
    public GateWay(String mac, String macOfP2pGO){
        this.macOfDevice = mac;
        this.macOfP2pGO = macOfP2pGO;
    }
    public GateWay(boolean isGateWay){
        this.isGateWay = isGateWay;
    }
    public GateWay(boolean isGateWay,String mac){
        this.isGateWay = isGateWay;
        this.macOfDevice = mac;
    }

    /**
     *当GM中没有记录，则随机从准网关节点周围的GO中随机抽取一个作为连接对象。若GM中有记录，则通过一个快排，一个二分得到推荐的连接GO
     *
     * @param macOfGatways 提取出网关节点信息表中已经LC连接的GOmac地址，记作集合B并通过字符串的值进行排序
     *                     第一个String是网关节点的MAC地址，第二个String是LC连接的组主的MAC地址
     * @param nearbyGOInfo 准网关节点周围发现的组主信息，提取出mac地址，记作集合A 通过二分法于上述序列进行比对，得到A中存在B中没有的Mac地址。
     *                     nearbyGOInfo需要除去当前组主
     * @return 给出一个推荐的附近组主节点，当aimGODevice的值不唯一，可以通过距离，信号值等做判断，现在就是随机取一个值。
     */
    public String chooseWifiGO (Map<String,String> macOfGatways,String nearbyGOInfo,String currentGOMAC){
        String aimGO;
        String [] nearbyAllGOInfo = nearbyGOInfo.split(",");
        List<String> nearLcGoMAC = new ArrayList<>();
        int k =0;
        for(int i =0;i<nearbyAllGOInfo.length;i++){
            String [] temp = nearbyAllGOInfo[i].split("/");
            if(temp[0].compareTo(currentGOMAC)!=0){
                nearLcGoMAC.add(temp[0]);
            }
        }

        if(macOfGatways.size()==0){
            int index = (int)(Math.random()*k);
            aimGO = nearLcGoMAC.get(index);
        }else{
            String [] macs = new String[macOfGatways.size()];
            int j =0;
            for(String gwMAC: macOfGatways.keySet()){
                macs[j] = macOfGatways.get(gwMAC);
                j++;
            }
            Log.d("网关节点表信息",String.valueOf(macOfGatways.size()));
            String [] resultMacs = quickSortByStringValue(macs,0, macs.length-1);
            List<String> aimGODevice = binaryQuery(resultMacs,nearLcGoMAC);
            if(aimGODevice.size()==1){
                aimGO = aimGODevice.get(0);
            }else{
                String [] collectionOfGO = new String [aimGODevice.size()];
                for(int i=0;i<aimGODevice.size();i++){
                    collectionOfGO [i] = aimGODevice.get(i);
                }
                int index = (int)(Math.random()*collectionOfGO.length);
                aimGO = collectionOfGO[index];
            }
        }
        return aimGO;
    }

    /**
     * 利用快排对mac地址进行字符串大小排序
     * @param m
     * @return
     */
    public String[] quickSortByStringValue(String[] m,int low,int heigh){
        int i =low,j= heigh,aim =0;
        while(i<j){
            while(i<j&&m[j].compareTo(m[aim])>=0){
                j--;
            }
            if(i<j){
                String temp = "";
                temp = m[j];
                m[j] = m[i];
                m[i] = temp;
                i++;
            }
            while(i<j&&m[i].compareTo(m[aim])<=0){
                i++;
            }
            if(i<j){
                String temp ="";
                temp = m[i];
                m[i] = m[j];
                m[j] = temp;
                j--;
            }
        }
        if(i>0){
            quickSortByStringValue(m,0,i-1);
        }
        if(j<m.length-1){
            quickSortByStringValue(m,i+i,m.length);
        }
        return m;
    }

    /**
     * 当附近只有一个组，那么该组就是推荐的LC组主，如果附近多余一个组主，通过二分，得到当前组主未连接的组主的mac地址。
     * 利用二分法找到当前节点所触及的组而当前组主未触及的组的mac地址
     * @param resultMacs 当前组主所触及组按照mac地址的大小的排列(网关节点相连接的组主设备)
     * @param nearbyLCGOMAC 当前节点所触及的组的mac地址
     * @return
     */
    public List<String> binaryQuery(String[] resultMacs, List<String> nearbyLCGOMAC){
        List<String> aimedWifiGO = new ArrayList<>();
        int low = 0;
        int high = resultMacs.length-1;
        int middle = (low+high)/2;
        if(nearbyLCGOMAC.size()==1){
            nearbyLCGOMAC.add(nearbyLCGOMAC.get(0));
            return aimedWifiGO;
        }
        for(int i =0;i<nearbyLCGOMAC.size();i++){
            while(low<high){
                if(nearbyLCGOMAC.get(i).compareTo(resultMacs[middle])==0){
                    nearbyLCGOMAC.remove(i);
                    break;
                }else if(nearbyLCGOMAC.get(i).compareTo(resultMacs[middle])>0){
                    low = middle;
                    middle = (low+high)/2+1;
//                    aimedWifiGO.add(nearbyLCGOMAC.get(i));
                }else{
                    high = middle;
                    middle = (low+high)/2;
//                    aimedWifiGO.add(nearbyLCGOMAC.get(i));
                }
            }
            if(low == high){
                if(nearbyLCGOMAC.get(i).compareToIgnoreCase(resultMacs[middle])==0){
                    nearbyLCGOMAC.remove(i);
                }
            }
        }
        aimedWifiGO = nearbyLCGOMAC;
        return aimedWifiGO;
    }
}

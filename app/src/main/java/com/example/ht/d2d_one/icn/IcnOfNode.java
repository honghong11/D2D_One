package com.example.ht.d2d_one.icn;

import android.util.Log;

import com.example.ht.d2d_one.interGroupCommunication.GateWay;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IcnOfNode {
    public boolean isGO = false;
    private String goMAC;
    private String gmMAC;

    public void setGoMAC(String goMAC) {
        this.goMAC = goMAC;
    }

    public void setGmMAC(String gmMAC) {
        this.gmMAC = gmMAC;
    }

    public void setGwMAC(String gwMAC) {
        this.gwMAC = gwMAC;
    }

    public void setGW(boolean GW) {
        isGW = GW;
    }

    private String gwMAC;
    private boolean isGW;
    private CacheInformation cacheInformation;
    private Map<String,String > GM = new LinkedHashMap<>();
    List<Map<String,List<String>>> QR = new ArrayList<Map<String,List<String>>>();
    private Map<String,String> RN = new HashMap<>();
    public IcnOfNode(Map<String,Socket> ISI,Map<String,String>RN, List<Map<String,List<String>>> QR, Map<String,String> GM, CacheInformation cacheInformation, String mac){
        this.RN = RN;
        this.QR = QR;
        this.GM = GM;
        this.cacheInformation = cacheInformation;
        this.goMAC = mac;
    }
    public IcnOfNode(CacheInformation cacheInformation,String mac){
        this.cacheInformation = cacheInformation;
        this.gmMAC = mac;
    }
    public IcnOfNode(Map<String,Socket>ISI,CacheInformation cacheInformation, String mac,boolean isGW){
        this.ISI = ISI;
        this.cacheInformation =cacheInformation;
        this.gwMAC = mac;
        this.isGW = isGW;
    }
    /**
     * Map类型，key值是RON节点的MAC地址和资源名称，value值是存储路径
     * RN
     */
    //private Map<String,String> RN = new HashMap<>();
    /**
     * 该方法在组刚组建的时候或者组员更新以及缓存更新的时候调用，增添RN表
     * @param MACOfRON
     * @param storagePath
     * @param Name
     * 结构：MACOfRON,Name ,storagePath
     */
    public void addRNTable(String MACOfRON,String storagePath,String Name){
        if(traverseRNTabl(MACOfRON,Name)){
            return;
        }else{
            String key = MACOfRON+","+Name;
            RN.put(key,storagePath);
        }
    }
    public void subRNTable(String MACOfRON,String Name){
        if(traverseRNTabl(MACOfRON,Name)){
            String key = MACOfRON+","+Name;
            RN.remove(key);
        }else{
            Log.d("无法删除RN表中的一些表项","查无此项资源信息");
        }
    }
    /**
     * 查看当前录入的资源是否已经是重复项，通过mac地址和名称确定
     * @param MACOfRON
     * @param Name
     * @return
     */
    public boolean traverseRNTabl(String MACOfRON,String Name){
        boolean isSameOne = false;
        String key = MACOfRON+","+Name;
        if(RN.get(key)!=null){
            isSameOne = true;
        }
        return isSameOne;
    }
    /**
     * @param resourceRequestPacket
     * @return 返回的是RON节点的mac地址和存储路径，可能存在多个结果
     */
    public List<String> queryRNTable(ResourceRequestPacket resourceRequestPacket){
        List<String> result = new ArrayList<>();
       for(String key : RN.keySet()){
            String bothKey[] = key.split(",");
            //这里在搜索的时候仅仅是字符串的匹配,以后应该改成一个更加快速和适配的算法，可以定义一个匹配率，
           // 当达到阈值就认为是匹配的。
           String [] name = bothKey[1].split(".");
            if((name[0].compareTo(resourceRequestPacket.ResourceName)==0)&&
                    (name[1].compareTo(resourceRequestPacket.TypeOfResourceName)==0)){
                String returnString = bothKey[0]+"\\+"+RN.get(key);
                result.add(returnString);
            }else{
                addQRTable(resourceRequestPacket);
            }
       }
       return result;
    }

    /**
     * 每一项查询记录为一个Map类型数据，第一个String存放RRN节点的MAC地址和请求的资源名称，第二个List是路径信息，
     * 因为在ResourceRequestPacket中就将请求的路径信息设置为List类型的。 使用顺序表，每隔一个生存时间就删除表项
     * 的最后一个信息
     * QR
     */
    //List<Map<String,List<String>>> QR = new ArrayList<Map<String,List<String>>>();
    /**
     * 新建QR表 结构为：RRNMAC＋name+type,路径信息
     * @param resourceRequestPacket
     */
    public void addQRTable(ResourceRequestPacket resourceRequestPacket){
        String key = resourceRequestPacket.MACOfRRN+"\\+"+resourceRequestPacket.ResourceName+
                "\\+"+resourceRequestPacket.TypeOfResourceName;
        List<String> value = resourceRequestPacket.PathInfo;
        Map<String,List<String>> temp = new HashMap<>();
        temp.put(key,value);
        QR.add(temp);
    }

    /**
     * 当GO收到查询信息resourceRequestPacket后，先进行QR表的查询,当查询到有结果则不再查询RN表，并且在QR中添加此记录
     * 如果查询没有匹配结果，返回false去查cs
     * @param resourceRequestPacket
     */
    public boolean queryQRTable(ResourceRequestPacket resourceRequestPacket){
        String macOfRRN = resourceRequestPacket.MACOfRRN;
        String qurryName = resourceRequestPacket.ResourceName;
        String qurryNameType = resourceRequestPacket.TypeOfResourceName;
        for(int i =0;i<QR.size();i++){
            Map<String,List<String>> contentOfQR = QR.get(i);
            String key = macOfRRN+"\\+"+qurryName+"\\+"+qurryNameType;
            if(contentOfQR.get(key)!=null) {
                addQRTable(resourceRequestPacket);
                return true;
            }
//            }else{
//                queryRNTable(resourceRequestPacket);
//            }
        }
        return false;
    }
    /**
     * 每隔10分钟自动查看QR表中是否还有记录，如果有则删除最后一条记录。如果没有则不删除。当组主节点建立时就直接执行这个方法，
     * 当然这个方法要开一个新的线程
     * 当有资源经过本节点时，查询该资源本节点中的QR表中是否有相关的记录，如果有，给予回复并更新QR表
     * 这个部分在后面数据包路由返回的时候去处理，数据包返回时也需要固定的数据结构，所以这个方法应该还需要一个数据包的参数
     */
    public void updateQRTable(List<Map<String,List<String>>> qr){
        if(qr!=null&&qr.size()>=1){
            int index = qr.size();
            qr.remove(index);
        }
    }

    /**
     * 这个线程在组主创建的时候开始，在本组主离开时将QR表转交或者本组解散之后该线程停止
     * 本线程每隔10分钟执行一次QR表的更新
     */
    public class UpdateThread implements Runnable{
        @Override
        public void run(){
            try {
                while (true) {
                    updateQRTable(QR);
                    Thread.sleep(60000);
                }
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    /**
     * 多副本情况下的  GM 网关节点表
     * 在GateWay.java 中利用chooseWifiGO得到LC 组主的mac地址
     * 第一个String是网关节点的MAC地址，第二个String是LC相连的GO的MAC地址
     */

    public Map<String, String> getGM() {
        return GM;
    }

    public void addGMTable(GateWay gateWay, String MACOfLcGO){
        gateWay.setGateWay(true);
        GM.put(gateWay.getMacOfDevice(),MACOfLcGO);
        Log.d(gateWay.getMacOfDevice(),"恭喜成为网关节点啦");
    }
    public void updateGMTable(GateWay gateWay){
        gateWay.setGateWay(false);
        GM.remove(gateWay.getMacOfDevice());
        Log.d(gateWay.getMacOfDevice(),"他不再是网管节点啦");
    }
    public void clearGMTable(){
        GM.clear();
    }
    /**
     * 选取网关节点方法
     * 过程很简单，对于value值相同的key值只选取一个
     */
    public List<String> chooseGW(Map<String,String> gm){
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for(String key: gm.keySet()){
            keys.add(key);
        }
        for(String value:gm.values()){
            values.add(value);
        }
        for(int i =0;i<values.size()-1;i++){
            //如果前一个value和后一个value相等，对应的keys中选择靠后的一个，因为使用linkedhashmap是顺序的，靠后说明此网关节点比较新
            if(values.get(i).equals(values.get(i+1))){
                keys.remove(i);
            }
        }
        return keys;
    }
    /**
     * 单副本情况下的GM网关节点
     */

    /**
     * 添加一个记录组间TCP socket的Map，仅网关节点和组主节点持有 interGroupSocketInfo
     * 第一个String是网关节点的mac地址
     */
    private Map<String,Socket> ISI = new HashMap<>();

    public Map<String, Socket> getISI() {
        return ISI;
    }

    //添加一个socket，并设置该socket保持活性
    public Map<String,Socket> addInterGroupSocketInfo(String mac, Socket socket){
        ISI.put(mac,socket);
        return ISI;
    }
    public void destroyInterGroupSocketInfo(){
        ISI.clear();
    }
}

package com.example.ht.d2d_one.icn;

import android.util.Log;

import com.example.ht.d2d_one.interGroupCommunication.GateWay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
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
//    private CacheInformation cacheInformation;
//    public CacheInformation getCacheInformation() {
//        return cacheInformation;
//    }

    private Map<String,String> cache = new HashMap<>();
    private Map<String,String > GM = new LinkedHashMap<>();
    List<Map<String,String>> QR = new ArrayList<Map<String,String>>();
    private Map<String,String> RN = new HashMap<>();
    public IcnOfNode(Map<String,Socket> ISI,Map<String,String>RN, List<Map<String,String>> QR, Map<String,String> GM, Map<String,String> cache, String mac){
        this.RN = RN;
        this.QR = QR;
        this.GM = GM;
        this.cache = cache;
        this.goMAC = mac;
    }
    public IcnOfNode(Map<String,String> cache,String mac){
        this.cache = cache;
        this.gmMAC = mac;
    }
    public IcnOfNode(Map<String,ObjectOutputStream> IOOS,Map<String,List<String>> got,Map<String,Socket>ISI,Map<String,String> cache, String mac,boolean isGW){
        this.IOOS = IOOS;
        this.ISI = ISI;
        this.cache =cache;
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
     * 每一项查询记录为一个Map类型数据，第一个String存放RRN节点的MAC地址和请求的资源名称，第二个List是路径信息(路径信息改为String类型的)，
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
        String value = resourceRequestPacket.PathInfo;
        Map<String,String> temp = new HashMap<>();
        temp.put(key,value);
        QR.add(temp);
    }

    /**
     * 当GO收到查询信息resourceRequestPacket后，先进行QR表的查询,当查询到有结果则不再查询RN表，并且在QR中添加此记录
     * 如果查询没有匹配结果，返回false去查cs，这一部分查询工作在myServerSocektThread中编写
     * @param resourceRequestPacket
     */
    public boolean queryQRTable(ResourceRequestPacket resourceRequestPacket){
        String macOfRRN = resourceRequestPacket.MACOfRRN;
        String qurryName = resourceRequestPacket.ResourceName;
        String qurryNameType = resourceRequestPacket.TypeOfResourceName;
        for(int i =0;i<QR.size();i++){
            Map<String,String> contentOfQR = QR.get(i);
            String key = macOfRRN+"\\+"+qurryName+"\\+"+qurryNameType;
            if(contentOfQR.get(key)!=null) {
                //将add和query分开
//                addQRTable(resourceRequestPacket);
                return true;
            }
        }
        return false;
    }
    /**
     * 每隔10分钟自动查看QR表中是否还有记录，如果有则删除最后一条记录。如果没有则不删除。当组主节点建立时就直接执行这个方法，
     * 当然这个方法要开一个新的线程
     * 当有资源经过本节点时，查询该资源本节点中的QR表中是否有相关的记录，如果有，给予回复并更新QR表
     * 这个部分在后面数据包路由返回的时候去处理，数据包返回时也需要固定的数据结构，所以这个方法应该还需要一个数据包的参数
     */
    public void updateQRTable(List<Map<String,String>> qr){
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
     * 第一个String是网关节点的MAC地址，第二个String是逻辑上LC相连的GO的MAC地址，即非本组主的另一组主MAC地址
     */

    public Map<String, String> getGM() {
        return GM;
    }

    public boolean isChangedGM(boolean changed){
        boolean result = false;
        if(changed){
            result = true;
        }
        return result;
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
    //当网关节点离开时，删除网关节点表中的该网关信息
    public void updateGMTable2(String mac){
        for(String string:GM.keySet()){
            if(mac.equals(string)){
                GM.remove(mac);
            }
        }
    }
    public void clearGMTable(){
        GM.clear();
    }
    /**
     * 通过网关节点表得到组主节点与其他组主的连接情况，这里的连接不是直接连接，而是通过一跳网关节点得以连接,得到的结果结构为：thisGOMAC-GOMAC1-GOMAC2...
     * 这里的TTL初始设置为8，从组主到组主算作是一跳，网关节点收到此信息后只负责转发
     */
    public String getAllGOConInfo(Map<String,String>gm){
        String allGOConInfo = goMAC;
        Collection<String> LCGOs = gm.values();
        for(String string:LCGOs){
            allGOConInfo = allGOConInfo+"-"+string;
        }
        return allGOConInfo;
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
//        for(String value:gm.values()){
//            values.add(value);
//        }
//        for(int i =0;i<values.size()-1;i++){
//            //如果前一个value和后一个value相等，对应的keys中选择靠后的一个，因为使用linkedhashmap是顺序的，靠后说明此网关节点比较新
//            if(values.get(i).equals(values.get(i+1))){
//                keys.remove(i);
//            }
//        }
        return keys;
    }
    /**
     *得到此组主除正在通信的网关节点之外的所有网关节点的MAC地址
     */
    public String getGWs(Map<String,String>gm,String inputGW){
        Collection<String> gws = gm.keySet();
        String result = " ";
        for(String str:gws){
            if(!str.equals(inputGW)){
                result = result +"-"+str;
            }
        }
        result = result.replace(" ","");
        return result;
    }
    /**
     * 单副本情况下的GM网关节点
     */

    /**
     * 添加一个记录组间TCP socket的Map，仅网关节点和组主节点持有 interGroupSocketInfo
     * 第一个String是网关节点的mac地址
     */
    private Map<String,Socket> ISI = new HashMap<>();
    /**
     * IOOS用来存放该网关节点的ObjectOutputStream，因为，对于一个网关节点应该使用同一个ObjectOutputStreami写入到ObjectInputStream中。
     */
    private Map<String,ObjectOutputStream> IOOS = new HashMap<>();

    public Map<String, Socket> getISI() {
        return ISI;
    }
    public Map<String,ObjectOutputStream>getIOOS(){
        return IOOS;
    }
    //添加一个socket，并设置该socket保持活性
    public Map<String,Socket> addInterGroupSocketInfo(String mac, Socket socket){
        ISI.put(mac,socket);
        return ISI;
    }
    public Map<String,ObjectOutputStream> addIOOS(String mac, ObjectOutputStream objectOutputStream){
        IOOS.put(mac,objectOutputStream);
        return IOOS;
    }
    //当网关节点离开或者组主离开时，关闭所有复用socket连接，并清除ISI表
    public void destroyInterGroupSocketInfo(){
        try{
            for(String s:ISI.keySet()){
                Socket socket = ISI.get(s);
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        ISI.clear();
    }

    /**
     * GOT 组主节点表，仅网关节点保存该表，邻接链表存储组主的拓扑结构（无向有环图）
     * 这里假定changeInfo的格式为：macGOOne-macOfGOTwo-change-gwAdd
     * GOT，第一个为组主节点的MAC地址，第二个为该组主节点所连接的其他组主节点(不包括本组主节点)
     */
    private Map<String,List<String>> GOT = new HashMap<>();

    public Map<String, List<String>> getGOT() {
        return GOT;
    }

    /**
     * @param got 组主节点表
     * @param conInfo 格式：TTL-应该转发次数-剩余转发次数-thisMAC-mac1-mac2...
     * @return 连接信息是否会造成组主节点表的改变
     */
    public boolean isChangedGOTByConInfo(Map<String,List<String>>got,String[] conInfo){
        boolean result = false;
        if(got.keySet().contains(conInfo[3])){
            List<String> gos = got.get(conInfo[3]);
            for(int i = 4;i<conInfo.length;i++){
                if(!gos.contains(conInfo[i])){
                    result = true;
                }
            }
        }else{
            result = true;
        }
        return result;
    }

    /**
     * 网关节点根据组主的连接情况更新组主节点表
     * @param got 组主节点表
     * @param conInfo 收到的组主节点表信息，格式：TTL-应该转发次数-剩余转发次数-thisMAC-mac1-mac2...
     */
    public void updateGOTByConInfo(Map<String,List<String>>got,String[] conInfo){
        if(got.size()==0){
            List<String> tmp = new ArrayList<>();
            for(int i =4;i<conInfo.length;i++){
                tmp.add(conInfo[i]);
            }
            got.put(conInfo[3],tmp);
            for(int j =4;j<conInfo.length;j++){
                List<String>temp = new ArrayList<>();
                temp.add(conInfo[3]);
                got.put(conInfo[j],temp);
            }
        }else{
            if(got.keySet().contains(conInfo[3])){
                List<String> gomacs = got.get(conInfo[3]);
                for(int i=4;i<conInfo.length;i++){
                    if(!gomacs.contains(conInfo[i])){
                        got.get(conInfo[3]).add(conInfo[i]);
                        if(!got.keySet().contains(conInfo[i])){
                            List<String> tmp = new ArrayList<>();
                            tmp.add(conInfo[3]);
                            got.put(conInfo[i],tmp);
                        }else{
                            got.get(conInfo[i]).add(conInfo[3]);
                        }
                    }
                }
            }else{
                List<String> tmp = new ArrayList<>();
                for(int i =4;i<conInfo.length;i++){
                    tmp.add(conInfo[i]);
                    got.get(conInfo[i]).add(conInfo[3]);
                }
                got.put(conInfo[3],tmp);
            }
        }
    }

    public boolean isChangGOT(Map<String,List<String>> got, String changeInfo){
        boolean result = false;
        String []macs = changeInfo.split("-");
        if(got.size()==0){
            result = true;
        }else{
            Collection<String> macsOfGOTNumer = got.keySet();
            if(macsOfGOTNumer.contains(macs[0])){
                if(got.get(macs[0]).contains(macs[1])){
                    return false;
                }else{
                    return true;
                }
            }
        }
        return result;
    }

    /**
     * 更新组主节点表
     * @param got 组主节点表
     * @param changeInfo 变化性信息 格式：goMAc-LcGOmac-info-gwAdd-gws
     */
    public void updateGOT(Map<String,List<String>>got,String changeInfo){
        String [] macs = changeInfo.split("-");
        String key1 = null;
        if(!got.containsKey(macs[0])){
            key1 = macs[0];
        }
        if(key1!=null){
            List<String> value = new ArrayList<>();
            value.add(macs[1]);
            got.put(key1,value);
        }else{
            got.get(macs[0]).add(macs[1]);
            List<String> value = new ArrayList<>();
            value.add(macs[0]);
            got.put(macs[1],value);
        }
    }
    public void destoryROT(Map<String,List<String>> rot){
        rot.clear();
    }
    public String toString(Map<String,List<String>>rot){
        if(rot.size()==0){
            return "组主节点表为空";
        }else{
            String result = " ";
            Collection<String> keys = rot.keySet();
            for(String str:keys){
                result = result+":"+str+"-"+rot.get(str).toString();
            }
            result.replace(" ","");
            return result;
        }
    }
    /**
     * @ TODO: 2019/2/28  这个深度优先查找无向图中的指定端点路径的算法
     * 利用节点表得到到目的地址的路由 递归，深度优先
     * 根据节点表，RRN所在的组主MAC地址，
     * @param thisGOMAC 当前调用此方法的组主的MAC地址
     * @param got 节点表
     * @param path 得到的目标路径之一 "node1-node2-node3..."
     * @param RRNGOMAC RRN所在组主的MAC地址
     * @param result 目标路径的集合
     */
    public List<String> getPath(Map<String,List<String>> got,String RRNGOMAC,String thisGOMAC,String path,List<String> result){
        for(String str:got.get(thisGOMAC)){
            if(path ==null){
                path = thisGOMAC;
            }
            boolean noredundant = true;
            String []paths = path.split("-");
            if(!str.equals(RRNGOMAC)){
                for(int i =0;i<paths.length;i++){
                    if(str.equals(paths[i])){
                        noredundant = false;
                        break;
                    }
                }
                if(noredundant){
                    path = path +"-"+str;
                    List<String> test = getPath(got,RRNGOMAC,str,path,result);
                    String[] ss = path.split("-");
                    if(ss.length>1){
                        path = ss[0];
                        for(int i=1;i<ss.length-2;i++){
                            path = path +"-"+ss[i];
                        }
                    }else{
                        path = null;
                    }
                }
            }else{
                path = path+"-"+str;
                result.add(path);
            }
        }
        return result;
    }
    public boolean isAddCache(String resourceInfo){
        boolean result = false;
        if(!cache.containsValue(resourceInfo)){
            result = true;
        }
        return result;
    }
    /**
     *简化cache机制，使用一个Map来存储请求信息
     */
    public void addCache(long time,String resourceInfo){
        cache.put(String.valueOf(time),resourceInfo);
    }
    //根据当前回溯的数据名称判断是否cache该资源
    public boolean isCache(String resourceInfo){
        boolean result = false;
        long currentTime = System.currentTimeMillis();
        Collection<String> time = cache.keySet();
        int count = 0;
        for(String key:time){
            if(resourceInfo.equals(cache.get(key).split("-")[0])){
                count++;
                if(count==2){
                    if(Long.parseLong(key)+3600000>currentTime){
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
    public String cacheToString(){
        String result = "/";
        Collection<String> resourceName = cache.values();
        for(String value:resourceName){
            result = result+"+"+value;
        }
        return result;
    }
}


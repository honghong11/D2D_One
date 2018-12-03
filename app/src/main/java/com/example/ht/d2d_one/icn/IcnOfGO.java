package com.example.ht.d2d_one.icn;

import android.util.Log;

import com.example.ht.d2d_one.interGroupCommunication.GateWay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IcnOfGO {
    public boolean isGO = false;
    /**
     * Map类型，key值是RON节点的MAC地址和资源名称，value值是存储路径
     * RN
     */
    public Map<String,String> RN = new HashMap<>();
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
            if((name[0].compareTo(resourceRequestPacket.ResourceName[0])==0)&&
                    (name[1].compareTo(resourceRequestPacket.ResourceName[1])==0)){
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
    List<Map<String,List<String>>> QR = new ArrayList<Map<String,List<String>>>();

    /**
     * 新建QR表 结构为：RRNMAC＋name+type,路径信息
     * @param resourceRequestPacket
     */
    public void addQRTable(ResourceRequestPacket resourceRequestPacket){
        String key = resourceRequestPacket.MACOfRRN+"\\+"+resourceRequestPacket.ResourceName[0]+
                "\\+"+resourceRequestPacket.ResourceName[1];
        List<String> value = resourceRequestPacket.PathInfo;
        Map<String,List<String>> temp = new HashMap<>();
        temp.put(key,value);
        QR.add(temp);
    }

    /**
     * 当GO收到查询信息resourceRequestPacket后，先进行QR表的查询,当查询到有结果则不再查询RN表，并且在QR中添加此记录
     * 如果查询没有匹配结果则，前往QN中进行查询。
     * @param resourceRequestPacket
     */
    public void qurryQRTable(ResourceRequestPacket resourceRequestPacket){
        String macOfRRN = resourceRequestPacket.MACOfRRN;
        String qurryName = resourceRequestPacket.ResourceName[0];
        String qurryNameType = resourceRequestPacket.ResourceName[1];
        for(int i =0;i<QR.size();i++){
            Map<String,List<String>> contentOfQR = QR.get(i);
            String key = macOfRRN+"\\+"+qurryName+"\\+"+qurryNameType;
            if(contentOfQR.get(key)!=null){
                addQRTable(resourceRequestPacket);
            }else{
                queryRNTable(resourceRequestPacket);
            }
        }
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
    private Map<String,String > GM = new HashMap<>();

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
    /**
     * 单副本情况下的GM网关节点
     */
}

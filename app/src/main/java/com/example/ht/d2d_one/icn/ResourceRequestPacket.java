package com.example.ht.d2d_one.icn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceRequestPacket implements Serializable{
    /**
     * 资源请求包，可由所有节点生成
     */
    public int TTL;
    public String MACOfRRN;
    public String PathInfo;
    public String TAG;
    public String ResourceName;
    public String TypeOfResourceName;

    public ResourceRequestPacket(int TTL,String MACOfRRN,String Path,String TAG,String Name,String typeOfResourceName){
        this.TTL = TTL;
        this.MACOfRRN = MACOfRRN;
        this.PathInfo = Path;
        this.TAG = TAG;
        this.ResourceName = Name;
        this.TypeOfResourceName = typeOfResourceName;
    }
    public ResourceRequestPacket(int TTL,String MACOfRRN,String Path,String Name,String typeOfResourceName){
        this.TTL = TTL;
        this.MACOfRRN = MACOfRRN;
        this.PathInfo = Path;
        this.ResourceName = Name;
        this.TypeOfResourceName = typeOfResourceName;
    }
    /**
     * 销毁已使用过的资源请求包,需要注意的是，一点销毁，则相关的所有引用全部消失。
     * 并且要注意顺序，QR和CR表都需要RR中的一些信息
     * @param resourceRequestPacket
     */
    public void destory(ResourceRequestPacket resourceRequestPacket){
        resourceRequestPacket = null;
    }

    /**
     * 转发时需要添加新的路径信息以及TTL
     * @param resourceRequestPacket
     */
    public ResourceRequestPacket update(ResourceRequestPacket resourceRequestPacket,String MACOfThisGO){
        resourceRequestPacket.TTL = resourceRequestPacket.TTL-1;
        resourceRequestPacket.PathInfo = resourceRequestPacket.PathInfo+","+MACOfThisGO;
        return resourceRequestPacket;
    }
    /**
     * 重写toString方法
     */
    public String toString(){
        String string;
//        String path;
//        if(PathInfo.size()==0){
//            path = null;
//        }else{
//            path = PathInfo.get(0);
//            int i =0;
//            while (i<PathInfo.size()&&PathInfo.get(i)!=null){
//                path = path +","+ PathInfo.get(i);
//                i++;
//            }
//        }
        string = Integer.toString(TTL)+"+"+MACOfRRN+"+"+PathInfo+"+"+TAG+"+"+ResourceName+"+"+TypeOfResourceName;
        return string;
    }
}

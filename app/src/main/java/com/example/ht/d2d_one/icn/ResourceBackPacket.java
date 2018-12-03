package com.example.ht.d2d_one.icn;

import java.util.ArrayList;
import java.util.List;

public class ResourceBackPacket {
    public int TTL;
    public String MACOfRRN;
    public List<String> PathInfo = new ArrayList<>();
    public String [] ResourceName = new String[2];
    /**
     * 2018-12-01，传输的资源可能是音视频，安装包和pdf，现在都是用文件流传输。关于具体的传输过程在通信实现后处理
     */
    public String contentOfResource;
    public ResourceBackPacket(int TTL, String MACOfRRN,List<String> path,String [] resourceName,String content){
        this.TTL = TTL;
        this.MACOfRRN = MACOfRRN;
        this.PathInfo = path;
        this.ResourceName = resourceName;
        this.contentOfResource = content;
    }
    /**
     * 销毁已使用过的资源请求包,需要注意的是，一点销毁，则相关的所有引用全部消失。
     * @param resourceBackPacket
     */
    public void destory(ResourceBackPacket resourceBackPacket){
        resourceBackPacket = null;
    }

    /**
     * 转发时需要删去上一个路径信息项以及更新TTL值
     * @param resourceBackPacket
     */
    public ResourceBackPacket update(ResourceBackPacket resourceBackPacket,String MACOfThisGO){
        resourceBackPacket.TTL = resourceBackPacket.TTL-1;
        resourceBackPacket.PathInfo.remove(MACOfThisGO);
        return resourceBackPacket;
    }
}

package com.example.ht.d2d_one.icn;


import android.util.Log;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheInformation {
    private final double CACHETHRESHOLD = 0.25;
    private final int CAPACITY = 15;
    /**
     * 存在两个表结构，CR（缓存推荐表）SC（缓存存储表） CR 表使用DoubleLinkedList数据结构
     * CR key放入请求时间，value放入请求的资源名称，在推荐时，如果RN表中已经存在该资源，则不缓存该资源
     * SC key放入资源名称，value放入存储路路径，SC更新后需要跟新RN表，定义capacity =15;SC 表使用LRUcache数据结构
     * CR 以后还可以添加一项RequestMac,用于以后将缓存推荐到对应的设备上
     * 目前仅根据类别进行缓存，不根据名称进行缓存
     */
    private DoubleLinkedList cacheRecommend = new DoubleLinkedList();
    private LRUCache storageCache = new LRUCache(CAPACITY);
    public CacheInformation(DoubleLinkedList cacheRecommend,LRUCache storageCache){
        this.cacheRecommend = cacheRecommend;
        this.storageCache = storageCache;
    }
    /**
     * @param time 当前时刻
     * @param name name的格式为: 资源名称+资源类型
     */
    public DoubleLinkedList addCacheRecommend(Long time,String name){
        String currentTime = time.toString();
        cacheRecommend.put(currentTime,name);
        return cacheRecommend;
    }
    /**
     * 我们认为只有一个小时内的记录才有意义。如果最近一个小时内没有记录，不缓存，清除CR表。只保留CR表当前时刻的一小时之内的记录，
     */
    private void updateCacheRecommend(DoubleLinkedList doubleLinkedList){
        Long time = System.currentTimeMillis();
        time = time - 3600000;
        String limitationTime = time.toString();
        String latestKey = "";
        try {
            Field tail = doubleLinkedList.getClass().getDeclaredField("tail");
            tail.setAccessible(true);
            Map.Entry<String,String> entry=(Map.Entry<String, String>) tail.get(doubleLinkedList);
            latestKey = entry.getKey();
        }catch(Exception e) {
            e.printStackTrace();
        }
        if(latestKey.compareTo(limitationTime)<0){
            doubleLinkedList.clear();
            Log.d("最近一个小时内没有查询记录","清除CR表");
        }
        doubleLinkedList.update(limitationTime);
    }
    /**
     * 根据CR判断是否要cache该项资源，如果返回true，调用storageCache方法，记录cache的存储信息
     * 遍历CR表，每隔10分钟调整一次权值，得到当前时刻CR表的一个向量（０.２，０.４，０.１，０.３）比如
     * @param resourceBackPacket 返回的数据包
     * @return 是否缓存该数据
     */
    private boolean cacheThisResource(ResourceBackPacket resourceBackPacket){
        updateCacheRecommend(cacheRecommend);
        Long time = System.currentTimeMillis();
        double rateOfCurrentResource =0;
        boolean cacheResult = false;
        String resourceName = resourceBackPacket.ResourceName[1];
        String resourceDetailName[] = resourceName.split("/");
        String backResourceType = resourceDetailName[1];
        double numOfMovie=0;
        double numOfMusic = 0;
        double numOfPacket = 0;
        double numOfWord = 0;
        int num = 1;
        DoubleLinkedList.Node tempNode = cacheRecommend.head.next;
        while (tempNode.next!=cacheRecommend.head){
            String sourceType[] = tempNode.value.split(".");
            switch(sourceType[1]){
                case "mkv":
                case "mp4":
                    if((time-Integer.valueOf(tempNode.key))/600==1){
                        numOfMovie = numOfMovie + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==2){
                        numOfMovie = numOfMovie + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==3){
                        numOfMovie = numOfMovie + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==4){
                        numOfMovie = numOfMovie + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==5){
                        numOfMovie = numOfMovie + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==0){
                        numOfMovie = numOfMovie + 3*num;
                    }
                    break;
                case "mp3":
                    if((time-Integer.valueOf(tempNode.key))/600==1){
                        numOfMusic = numOfMusic + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==2){
                        numOfMusic = numOfMusic + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==3){
                        numOfMusic = numOfMusic + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==4){
                        numOfMusic = numOfMusic + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==5){
                        numOfMusic = numOfMusic + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==0){
                        numOfMusic = numOfMusic + 3*num;
                    }
                    break;
                case "apk":
                    if((time-Integer.valueOf(tempNode.key))/600==1){
                        numOfPacket = numOfPacket + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==2){
                        numOfPacket = numOfPacket + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==3){
                        numOfPacket = numOfPacket + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==4){
                        numOfPacket = numOfPacket + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==5){
                        numOfPacket = numOfPacket + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==0){
                        numOfPacket = numOfPacket + 3*num;
                    }
                    break;
                case "pdf":
                    if((time-Integer.valueOf(tempNode.key))/600==1){
                        numOfWord = numOfWord + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==2){
                        numOfWord = numOfWord + 2*num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==3){
                        numOfWord = numOfWord + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==4){
                        numOfWord = numOfWord + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==5){
                        numOfWord = numOfWord + num;
                    }else if((time-Integer.valueOf(tempNode.key))/600==0){
                        numOfWord = numOfWord + 3*num;
                    }
                    break;
                default:
                    Log.d("该资源不符合类型要求","请注意");
                    break;
            }
            tempNode = tempNode.next;
        }
        if(backResourceType.equals("mkv")||backResourceType.equals("mp4")){
            rateOfCurrentResource = numOfMovie/(numOfMovie+numOfMusic+numOfPacket+numOfWord);
        }else if(backResourceType.equals("mp3")){
            rateOfCurrentResource = numOfMusic/(numOfMovie+numOfMusic+numOfPacket+numOfWord);
        }else if(backResourceType.equals("apk")){
            rateOfCurrentResource = numOfPacket/(numOfMovie+numOfMusic+numOfPacket+numOfWord);
        }else if(backResourceType.equals("pdf")){
            rateOfCurrentResource = numOfWord/(numOfMovie+numOfMusic+numOfPacket+numOfWord);
        }
        if(rateOfCurrentResource>CACHETHRESHOLD){
            cacheResult = true;
        }
        return cacheResult;
    }
    /**
     * 这个方法是在socket接收数据时调用
     * 如果推荐缓存此项目，首先要选择存储路径（目前是存放在组主节点），然后放入SC表中。
     * @param resourceBackPacket 返回的数据包
     * @param storagePath 存储路径
     */
    public void addStorageCache(ResourceBackPacket resourceBackPacket,String storagePath){
         if(cacheThisResource(resourceBackPacket)){
             /**
              * 路径来自于socket接收数据时存放到SD卡的位置
              * 这个等连接完成，字符串可以组间通信的时候在写
              * 2018-12-03
              */
             storageCache.put(resourceBackPacket.ResourceName[0]+"."+resourceBackPacket.ResourceName[1],
                     resourceBackPacket.PathInfo.toString());
         }
    }
//    //LRU 缓存的数据，顺序不是插入的顺序而是访问的数据
//    class LRUCache extends LinkedHashMap<String,String>{
//        private int maxElements;
//        private LRUCache(int maxSize){
//            super(maxSize,0.75f,true);
//            maxElements = maxSize;
//        }
//        @Override
//        public boolean removeEldestEntry(java.util.Map.Entry eldest){
//            return size()>maxElements;
//        }
//    }
}

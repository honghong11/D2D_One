package com.example.ht.d2d_one.icn;

import java.util.LinkedHashMap;

//LRU 缓存的数据，顺序不是插入的顺序而是访问的数据
public class LRUCache extends LinkedHashMap<String,String> {
    private int maxElements;
    public LRUCache(int maxSize){
        super(maxSize,0.75f,true);
        maxElements = maxSize;
    }
    @Override
    public boolean removeEldestEntry(java.util.Map.Entry eldest){
        return size()>maxElements;
        //remove 时需要将对应位置的资源删除
    }
}
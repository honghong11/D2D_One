package com.example.ht.d2d_one.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MatchingAlgorithm {
    private String qurryName = "qurryName";
    private Map<String,String> resourceMap = new HashMap<>();
    public Map<String,String> resultResourceMap = new HashMap<>();
    public MatchingAlgorithm(String qurryName,Map<String,String>resourceMap){
        this.qurryName = qurryName;
        this.resourceMap = resourceMap;
    }
    /**
     *基于字符串匹配的匹配算法
     * @param qurryName 目标字符串
     * @param qurrySetMap 查找集合 map类型 比如在DeviceListFragment中定义的 qurryMovieMap
     * @return 返回查询列表
     */
    public Map<String,String> matchingCharacterAlgorithm(String qurryName,Map<String,String> qurrySetMap){
        Map<String,String> resultMap = new HashMap<>();
        Set<String> indexs = qurrySetMap.keySet();
        String [] resourceName = new String[indexs.size()];
        String [] resourceIndex = new String[indexs.size()];
        int i =0;
        for(String index: indexs){
            resourceName[i] = qurrySetMap.get(index);
            resourceIndex[i] = index;
            i++;
        }
        for(i=0;i<=indexs.size()-1;i++){
            //目标字符串完全命中，也有不完全命中的时候，所以我要写一个新的基于字符串匹配的算法
            if(qurryName.compareTo(resourceName[i])==0){
                Log.d("命中啦啦啦啦啦",qurryName);
                resultMap.put(resourceIndex[i],resourceName[i]);
            }
        }
        return resultMap;
    }
}

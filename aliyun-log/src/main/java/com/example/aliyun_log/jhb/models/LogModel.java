package com.example.aliyun_log.jhb.models;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.model.Log;

import java.util.HashMap;
import java.util.Map;

public class LogModel {
    private String timeKey = "__time__";
    public Map<String,Object> mContent = new HashMap<>();

    private int time = 0;

    public LogModel(){
        this.time = (new Long(System.currentTimeMillis() / 1000L)).intValue();
    }

    public void putTime(int time){
       this.time = time;
    }

    public void putContent(String key,String value){
        mContent.put(key,value);
    }

    /**
     * 利用json字符串初始化
     * @param jsonString
     */
    public LogModel(String jsonString){
        try {
            JSONObject object = JSONObject.parseObject(jsonString);
            time = object.getIntValue(timeKey);
            object.remove(time);
            for(Map.Entry<String,Object> entry: object.entrySet()){
                mContent.put(entry.getKey(),entry.getValue());
            }

        }catch (Exception e){

        }

    }


    /**
     * 转换成阿里云日志模型
     * @return
     */
    public Log convertAliyunLog(){
        Log logModel = new Log();

        for(Map.Entry obj : this.mContent.entrySet()) {
            String key = (String) obj.getKey();
            if(key != timeKey){
                logModel.PutContent(key,(String) obj.getValue());
            }

        }
        logModel.PutTime(time);
        return  logModel;
    }

    /**
     * 转成json字符串
     * @return
     */
    public String convertToJsonString(){
        Map logGroupMap = new HashMap();

        for(Map.Entry<String,Object> entry:mContent.entrySet()){
            logGroupMap.put(entry.getKey(),entry.getValue());
        }
        logGroupMap.put(timeKey,time);
        String jsonString = JSON.toJSONString(logGroupMap);
        return  jsonString;
    }

}
package com.example.aliyun_log.jhb.models;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogGroupModel {
    /**
     * 多个日志list
     */
    private List<LogModel> logContents = new ArrayList<>();

    /**
     * 日志topic
     */
    private String logTopic;

    /**
     * 日志来源
     */
    private String logSource;

    /**
     * 日志tag内容map
     */
    private Map<String,String> logTagContentMap = new HashMap();

    /**
     * 是否是缓存日志
     */
    public boolean isCacheLog = false;

    /**
     * 上传次数(针对上传失败的处理)
     */
    public int postCount = 0;
    /**
     * 多个logentity(上传缓存日志有用)
     */
    public List<LogEntity> logEntityList = new ArrayList<>();

    /**
     * 初始化
     * @param logTopic
     * @param logSource
     * @param logContents
     */
    public LogGroupModel(String logTopic,String logSource,List<LogModel> logContents){
        this.logTopic = logTopic;
        this.logSource = logSource;
        this.logContents = logContents;
//        this.logTagContentMap = logTagContentMap;
    }

    /**
     * 转成成阿里云日志logGroup
     * @return
     */
    public LogGroup convertAliyunLogGroup(){
        LogGroup logGroup = new LogGroup(this.logTopic,this.logSource);
        for(LogModel model : this.logContents){
            Log logModel = model.convertAliyunLog();
            logGroup.PutLog(logModel);
        }
        return  logGroup;
    }

    /**
     * 转成json字符串
     * @return
     */
    public String convertToJsonString(){
        Map logGroupMap = new HashMap();
        logGroupMap.put("logTopic",logTopic);
        logGroupMap.put("logSource",logSource);
        List<String> contentList = new ArrayList<>();
        for(LogModel model : logContents){
            contentList.add(model.convertToJsonString());
        }
        logGroupMap.put("logContents",contentList);
        logGroupMap.put("postCount",postCount);

        String jsonString = JSON.toJSONString(logGroupMap);

        return  jsonString;
    }

    /**
     * 利用json字符串初始化
     * @param jsonString
     */

    public LogGroupModel(String jsonString){
        try {
            JSONObject object = JSONObject.parseObject(jsonString);
            String logTopic = object.getString("logTopic");
            String logSource = object.getString("logSource");
            int postCount = object.getInteger("postCount");
            JSONArray list = (JSONArray)object.get("logContents");
            for(int i = 0;i < list.toArray().length;i++){
                String logJsonString = list.getString(i);
                LogModel logModel = new  LogModel(logJsonString);
                this.logContents.add(logModel);
            }
            this.logTopic = logTopic;
            this.logSource = logSource;
            this.postCount = postCount;
        }catch (Exception e){

        }
    }

    /**
     * 添加日志内容
     * @param model
     */
    public void addLogContents(LogGroupModel model){
        this.logContents.addAll(model.logContents);
    }

    /**
     * 日志是否为空
     * @return
     */
    public boolean logIsEmpty(){
        return logContents.isEmpty() || logTopic.isEmpty() || logSource.isEmpty();
    }

    /**
     * 获取日志topic
     * @return
     */
    public String getLogTopic(){
        return  logTopic;
    }

    /**
     * 获取日志来源
     * @return
     */
    public String getLogSource(){
        return  logSource;
    }


}

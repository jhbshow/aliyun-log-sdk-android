package com.example.aliyun_log.jhb;

import android.content.Context;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;

import com.aliyun.sls.android.sdk.core.auth.CredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.example.aliyun_log.jhb.models.LogGroupModel;
import com.example.aliyun_log.jhb.models.LogModel;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;


public class LogManager {
      //单列
    private static LogManager instance;
    private LogManager(){ }
    public static class SingletonInstance {
        private static final LogManager INSTANCE = new LogManager();
    }
    public static LogManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    /**
     * 日志配置
     */
    LogConfig logConfig;

    /**
     * 上传客户端
     */
    LOGClient logClient;

    /**
     * 上传日志客户端
     */
    public void setupLogConfig(Context context, LogConfig config){

        this.logConfig = config;
        CredentialProvider credentialProvider;


        if(config.isSlsAuth()){

            credentialProvider =
                    new StsTokenCredentialProvider(config.getAccessKeyID(), config.getAccessKeySecret(), config.getToken());
        }else {
             credentialProvider =
                    new PlainTextAKSKCredentialProvider(config.getAccessKeyID(), config.getAccessKeySecret());
        }
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setCachable(false);
        conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
        this.logClient = new LOGClient(context,config.getEndPoint(),credentialProvider,conf);

    }

    /**
     * 上传日志
     */
    public void postLog(final LogGroupModel logGroupModel, final CompletedCallback<PostLogRequest, PostLogResult> callback){
        if(logGroupModel.logIsEmpty()){
            Log.e("aliyunLog","日志数据为空");
            return;
        }
   
        try {
            final LogEntity entity = this.getLogEntity(logGroupModel);
            if(logGroupModel.isNeedInsertDB){
                this.saveLog(entity);
            }

            LogGroup logGroup = logGroupModel.convertAliyunLogGroup();
            PostLogRequest request = new PostLogRequest(logConfig.getProjectName(), logConfig.getLogStoreName(), logGroup);

            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest postLogRequest, PostLogResult postLogResult) {
                    if(logGroupModel.isNeedInsertDB){
                        LogManager.getInstance().deleteLogFromDB(entity);
                    }
                    callback.onSuccess(postLogRequest,postLogResult);
                }

                @Override
                public void onFailure(PostLogRequest postLogRequest, LogException e) {
                    callback.onFailure(postLogRequest,e);
                }
            });
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取日志存储实列
     * @param logGroupModel
     * @return
     */
    public LogEntity getLogEntity(LogGroupModel logGroupModel){
        LogEntity entity = new LogEntity();
        entity.setEndPoint(logConfig.getEndPoint());
        entity.setStore(logConfig.getLogStoreName());
        entity.setProject(logConfig.getProjectName());
        String jsonString = logGroupModel.convertToJsonString();
        entity.setJsonString(jsonString);
        entity.setTimestamp(new Long((new Date()).getTime()));
        return  entity;
    }

    /**
     * 保存日志
     */
    protected void saveLog(LogEntity entity){
        SLSDatabaseManager.getInstance().insertRecordIntoDB(entity);
    }

    /**
     * 保存日志
     * @param logGroupModel
     */
    public void saveLog(LogGroupModel logGroupModel){
        LogEntity entity = getLogEntity(logGroupModel);
        saveLog(entity);
    }

    /**
     * 删除日志
     * @param entity
     */
    public void deleteLogFromDB(LogEntity entity){
        SLSDatabaseManager.getInstance().deleteRecordFromDB(entity);
    }

    /**
     * 上传数据库日志数据
     */
    public void uploadDbLogData(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    /* 发送log 会调用网络操作，需要在一个异步线程中完成*/
                    List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
                    for (final LogEntity logEntity: list) {
                        String jsonString = logEntity.getJsonString();
                        try{
                            final LogGroupModel logGroupModel = new LogGroupModel(jsonString);
                            logGroupModel.postCount = logGroupModel.postCount + 1;
                            logGroupModel.isNeedInsertDB = false;

                            if(logGroupModel.logIsEmpty()){
                                //日志为空的时候上传是不成功的,避免数据格式问题
                                //既然有上传，格式有问题，也要上传到阿里云记录下来

                                LogManager.getInstance().deleteLogFromDB(logEntity);
                                LogManager.getInstance().handerLogFormatFail(logGroupModel);
                                continue;
                            }

                            LogManager.getInstance().postLog(logGroupModel, new CompletedCallback<PostLogRequest, PostLogResult>() {
                                @Override
                                public void onSuccess(PostLogRequest postLogRequest, PostLogResult logRequestResult) {
                                    LogManager.getInstance().deleteLogFromDB(logEntity);
                                }
                                @Override
                                public void onFailure(PostLogRequest postLogRequest, LogException e) {

                                    Log.e("LogManager","上传持久化日志失败");
                                }
                            });
                        }catch (Exception e){
                             //日志持久化数据解析出错
                            JSONObject object = JSONObject.parseObject(jsonString);
                            String logTopic = object.getString("logTopic");
                            String logSource = object.getString("logSource");

                            //删除持久化
                            LogManager.getInstance().deleteLogFromDB(logEntity);

                            //重新写入日志持久化
                            LogModel logModel = new LogModel();
                            logModel.putContent("content",jsonString);
                            List<LogModel> logContents = new ArrayList<>();
                            logContents.add(logModel);
                            LogGroupModel logGroupModel = new LogGroupModel(logTopic,logSource,logContents);
                            LogManager.getInstance().handerLogFormatFail(logGroupModel);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }).start();
    }

    /**
     * 处理日志格式错误
     * @param logGroupModel
     */
    private void handerLogFormatFail(LogGroupModel logGroupModel){

        String jsonString = logGroupModel.convertToJsonString();
        LogModel logModel = new LogModel();
        logModel.putContent("content",jsonString);
        logModel.putContent("log_format_error","安卓日志格式数据错误");
        List<LogModel> logContents = new ArrayList<>();
        logContents.add(logModel);
        LogGroupModel newLogGroupModel = new LogGroupModel(logGroupModel.getLogTopic(),logGroupModel.getLogSource(),logContents);
        LogEntity failEntity = LogManager.getInstance().getLogEntity(newLogGroupModel);

        LogManager.getInstance().saveLog(failEntity);

    }

}

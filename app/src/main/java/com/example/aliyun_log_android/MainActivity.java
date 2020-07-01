package com.example.aliyun_log_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.example.aliyun_log.jhb.DatabaseManager;
import com.example.aliyun_log.jhb.LogConfig;
import com.example.aliyun_log.jhb.LogManager;
import com.example.aliyun_log.jhb.models.LogGroupModel;
import com.example.aliyun_log.jhb.models.LogModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化日志
    }

    void configLog(){
        String endPoint = "cn-shenzhen.log.aliyuncs.com";

        String accessKeyID = "LTAI*******oipRi";

        String accessKeySecret = "CYFJz*********i0P";

        String projectName = "jhb-log-test";

        String logStoreName = "jhb-logstore-test";

        String token = "";

        LogConfig config = new  LogConfig(endPoint,accessKeyID,accessKeySecret,projectName,logStoreName,token);

//        初始化日志上传

        LogManager.getInstance().setupLogConfig(getApplicationContext(),config);

        DatabaseManager.getInstance().setupDBContext(getApplicationContext());

    }

    /**
     * 保存日志
     *
     * @param v
     */
    public void saveLog(View v) {
        LogModel logModel = new LogModel();
        logModel.putContent("androidLogError","android3232323");
        logModel.putContent("androidUrl","/android/url/error");
        List<LogModel> logList = new ArrayList<>();
        logList.add(logModel);

        LogGroupModel logGroupModel = new LogGroupModel("android-log-topic-20200618","android-log-source-20200618",logList);
        LogManager.getInstance().saveLog(logGroupModel);

        Log.i("ddd","保存日志");
    }


    /**
     * 上传持久化的日志
     * @param v
     */
    public void uploadArchiveLog(View v) {
        Log.i("ddd","上传持久化的日志");
        //一般在wifi情况下进行上传持久化日志
        LogManager.getInstance().uploadDbLogData();
    }

    /**
     * 上传日志
     * @param v
     */
    public void postLog(View v){
        LogModel logModel = new LogModel();
        logModel.putContent("androidLogError","android3232323");
        logModel.putContent("androidUrl","/android/url/error");
        List<LogModel> logList = new ArrayList<>();
        logList.add(logModel);

        LogGroupModel logGroupModel = new LogGroupModel("android-log-topic-20200618","android-log-source-20200618",logList);
        LogManager.getInstance().postLog(logGroupModel, new CompletedCallback<PostLogRequest, PostLogResult>() {
            @Override
            public void onSuccess(PostLogRequest postLogRequest, PostLogResult postLogResult) {
                Log.i("LogManager",postLogResult.getResponseHeader().toString());
                if(postLogResult.getStatusCode() == 200){
                    //上传成功
                }
            }

            @Override
            public void onFailure(PostLogRequest postLogRequest, LogException exception) {
                String error = exception.getMessage();
                Log.i("LogManager",error);
            }
        });
    }
}
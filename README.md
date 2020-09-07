# 阿里云日志上传


# Android 接入
Step 1. Add it in your root build.gradle at the end of repositories:
```android
    allprojects {
       repositories {
           ...
           maven { url 'https://jitpack.io' }
       }
    }
```  
Step 2. Add the dependency(Tag是对应的版本)
```android
    dependencies {
          implementation 'com.alibaba:fastjson:1.2.35'
          implementation 'com.github.jhbshow:aliyun-log-sdk-android:0.0.1'
    }
```
 # Usage:
 ### 初始化参数
 ```android
String endPoint = "cn-shenzhen.log.aliyuncs.com";
String accessKeyID = "LTAI*******oipRi";
String accessKeySecret = "CYFJz*********i0P";
String projectName = "jhb-log-test";
String logStoreName = "jhb-logstore-test";
LogConfig config = new LogConfig(endPoint,accessKeyID,accessKeySecret,projectName,logStoreName,"");
LogManager.getInstance().setupLogConfig(getApplicationContext(),config);
```    
 
### 上传日志
```android
LogModel logModel = new LogModel();
logModel.putContent("androidLogError","android3232323");
logModel.putContent("androidUrl","/android/url/error");
List<LogModel> logList = new ArrayList<>();
logList.add(logModel);
LogGroupModel logGroupModel = new LogGroupModel("android-log-topic-20200618","android-log-source-20200618",logList);
LogManager.getInstance().postLog(logGroupModel, new CompletedCallback<PostLogRequest, PostLogResult>() {
     @Override
     public void onSuccess(PostLogRequest postLogRequest, PostLogResult postLogResult) {          
     }

     @Override
     public void onFailure(PostLogRequest postLogRequest, LogException exception) {
        String error = exception.getMessage();
     }
});
```     
### 暂停日志服务(app退到后台)
```android
LogManager.getInstance().stopLogService();
```
### 开始日志服务(app进到前台)
```android
LogManager.getInstance().startLogService();
```

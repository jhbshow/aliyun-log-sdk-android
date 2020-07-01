package com.example.aliyun_log.jhb;

public class LogConfig {

    private String endPoint = "";

    private String accessKeyID = "";

    private String accessKeySecret = "";

    private String projectName = "";

    private String logStoreName = "";

    private String token;

    /**
     * 初始化
     * @param endPoint
     * @param accessKeyID
     * @param accessKeySecret
     * @param projectName
     * @param logStoreName
     * @param token
     */
    public LogConfig(String endPoint, String accessKeyID, String accessKeySecret, String projectName, String logStoreName, String token){

        this.endPoint = endPoint;
        this.accessKeyID = accessKeyID;
        this.accessKeySecret = accessKeySecret;
        this.projectName = projectName;
        this.logStoreName = logStoreName;
        if(token.length()>0){
            this.token = token;
        }
    }

    public String getEndPoint(){
        return  this.endPoint;
    }

    public String getAccessKeyID(){
        return  this.accessKeyID;
    }

    public String getAccessKeySecret(){
        return this.accessKeySecret;
    }

    public String getProjectName(){
        return  this.projectName;
    }

    public String getLogStoreName(){
        return  this.logStoreName;
    }

    public String getToken(){
        return  this.token;
    }

    /**
     * 是否是SLS验证
     * @return
     */
    public boolean isSlsAuth(){
        return this.token != null && this.token.length() > 0;
    }
}

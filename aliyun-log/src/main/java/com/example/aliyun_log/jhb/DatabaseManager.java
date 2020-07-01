package com.example.aliyun_log.jhb;

import android.content.Context;

import com.aliyun.sls.android.sdk.SLSDatabaseManager;

public class DatabaseManager {
    //单列
    private static DatabaseManager instance;
    private DatabaseManager(){ }
    public static class SingletonInstance {
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }
    public static DatabaseManager getInstance() {
        return DatabaseManager.SingletonInstance.INSTANCE;
    }


    /**
     * 设置DB的context
     * @param context
     */
    public void setupDBContext(Context context){
        SLSDatabaseManager.getInstance().setupDB(context);
    }
}

package com.example.aliyun_log.jhb;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.request.PostCachedLogRequest;
import com.aliyun.sls.android.sdk.result.PostCachedLogResult;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LogCacheManager {
    //单列
    private static LogCacheManager instance;
    private LogCacheManager(LOGClient mClient) {
        this.mClient = mClient;
    }
    private static class SingletonInstance {
        private static final LogCacheManager INSTANCE = new LogCacheManager(LogManager.getInstance().logClient);
    }
    public static LogCacheManager getInstance() {
        return LogCacheManager.SingletonInstance.INSTANCE;
    }


    private Timer mTimer;
    private LOGClient mClient;



    public void setupTimer() {
        if(this.mTimer == null) {
            //为空才初始化
            this.mTimer = new Timer();
            TimerTask timerTask = new com.example.aliyun_log.jhb.LogCacheManager.CacheTimerTask(this);
            this.mTimer.schedule(timerTask, 30000L, 30000L);
        }
    }

    public void stopTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        this.stopTimer();
        Log.d("CacheManager", "CacheManager finalize");
    }

    private static class CacheTimerTask extends TimerTask {
        private WeakReference<com.example.aliyun_log.jhb.LogCacheManager> mWeakCacheManager;

        public CacheTimerTask(LogCacheManager manager) {
            this.mWeakCacheManager = new WeakReference(manager);
        }

        public void run() {
            if (this.mWeakCacheManager.get() != null) {
                @SuppressLint("WrongConstant") ConnectivityManager cm = (ConnectivityManager)((com.example.aliyun_log.jhb.LogCacheManager)this.mWeakCacheManager.get()).mClient.getContext().getSystemService("connectivity");
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Boolean shouldPost = false;
                    if (((com.example.aliyun_log.jhb.LogCacheManager)this.mWeakCacheManager.get()).mClient.getPolicy() == ClientConfiguration.NetworkPolicy.WIFI_ONLY && activeNetwork.getType() == 1) {
                        shouldPost = true;
                    } else if (((com.example.aliyun_log.jhb.LogCacheManager)this.mWeakCacheManager.get()).mClient.getPolicy() == ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI) {
                        shouldPost = true;
                    }

                    if (shouldPost) {
                       LogManager.getInstance().uploadDbLogData();
                    }

                }
            }
        }
    }
}

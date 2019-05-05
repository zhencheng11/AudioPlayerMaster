package com.dingxingxiang.audioplaymaster.audio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 音乐播放服务
 */
public class MusicPlayerService extends Service {
    private String TAG = "debug:MusicPlayerService--";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicController getMusicController() {
            return MusicControllerImp.getInstance(MusicPlayerService.this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        MusicNotification.getMusicNotification(this, MusicControllerImp.getInstance(this)).onCancelMusicNotifi();
    }
}

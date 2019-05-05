package com.dingxingxiang.audioplaymaster.audio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.dingxingxiang.audioplaymaster.MainActivity;
import com.dingxingxiang.audioplaymaster.R;

/**
 * Created by ding on 2016/12/3.
 * 状态栏的音频播放控制器
 */
public class MusicNotification {

    /**
     * 音频播放控制 的  Notification
     * 动态的显示后台的AudioplayService的前台展示
     */
    private static MusicNotification notifyInstance = null;

    // 通知id
    private final int FLAG = PendingIntent.FLAG_UPDATE_CURRENT;
    private final int NOTIFICATION_ID = 0x1213;
    // 通知
    private Notification musicNotifi = null;
    // 管理通知
    private NotificationManager manager = null;
    // 界面实现
    private Notification.Builder builder = null;
    // 上下文
    private Context context;
    MusicController musicController;
    // 布局
    private RemoteViews remoteViews;
    private Intent playsIntent = null;
    /**
     * 上一首 按钮点击 ID
     */
    private final static int BUTTON_PREV_ID = 1;
    /**
     * 播放/暂停 按钮点击 ID
     */
    private final static int BUTTON_PALY_ID = 2;
    /**
     * 下一首 按钮点击 ID
     */
    private final static int BUTTON_NEXT_ID = 3;
    /**
     * 关闭 按钮点击 ID
     */
    private final static int BUTTON_COSE_ID = 4;

    /**
     * 跳转播放界面 按钮点击 ID
     */
    private final static int BUTTON_JUMP_ID = 5;

    private final static String ACTION_BUTTON = "xinkunic.aifatushu.customviews.MusicNotification.ButtonClick";
    private final static String INTENT_BUTTONID_TAG = "ButtonId";
    private ImageView imageView;

    private MusicNotification() {

    }

    private MusicNotification(Context context, MusicController musicController) {
        this.context = context;
        this.musicController = musicController;
        // 初始化操作
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_custom_button);

        builder = new Notification.Builder(context);
        // 初始化控制的Intent
        playsIntent = new Intent();
        playsIntent.setAction(ACTION_BUTTON);
        imageView = new ImageView(context);
        onCreateMusicNotifi(context);
    }

    /**
     * 恶汉式实现 通知
     *
     * @return
     */
    public static MusicNotification getMusicNotification(Context context, MusicController musicController) {
        if (notifyInstance == null) {
            synchronized (MusicNotification.class) {
                if (notifyInstance == null) {
                    notifyInstance = new MusicNotification(context, musicController);
                }
            }
        }
        return notifyInstance;
    }

    /**
     * 创建通知
     * 初始化通知
     */
    @SuppressLint("NewApi")
    public void onCreateMusicNotifi(Context context) {
        // 设置点击事件
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager = NotificationManagerCompat.from(context);
        registerClick();
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.putExtra("isHead", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
        builder.setContent(remoteViews).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher);
        //Android 8.0以后
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(context.getPackageName(), "有章音频", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
            builder.setChannelId(context.getPackageName());
        } else {
            builder.setVibrate(new long[]{0});
            builder.setSound(null);
        }
        musicNotifi = builder.build();
        initButtonReceiver(context);

    }

    private void registerClick() {
        // 1.注册播放或暂停点击事件
        playsIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent pplay = PendingIntent.getBroadcast(context, 1, playsIntent, FLAG);
        remoteViews.setOnClickPendingIntent(R.id.btn_custom_play, pplay);

        // 2.注册上一首点击事件
        playsIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        PendingIntent playPre = PendingIntent.getBroadcast(context, 3, playsIntent, FLAG);
        remoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, playPre);

        // 3.注册下一首点击事件
        playsIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent pnext = PendingIntent.getBroadcast(context, 2, playsIntent, FLAG);
        remoteViews.setOnClickPendingIntent(R.id.btn_custom_next, pnext);

        // 4.注册关闭点击事件
        playsIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_COSE_ID);
        PendingIntent pclose = PendingIntent.getBroadcast(context, 4, playsIntent, FLAG);
        remoteViews.setOnClickPendingIntent(R.id.btn_custom_close, pclose);

    }

    private void initButtonReceiver(Context context) {
        ButtonBroadcastReceiver bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        context.registerReceiver(bReceiver, intentFilter);
    }


    /**
     * 取消通知栏
     */
    public void onCancelMusicNotifi() {
        if (manager == null) return;
        manager.cancel(NOTIFICATION_ID);
        isShowing = false;
    }

    private String TAG = "---MusicNotification--";

    private boolean isPlaying = false;

    /**
     * 更新通知
     */
    public void upDataNotifacation(boolean needRefrush, final String name, String faceUrl, boolean isPlaying) {
        this.isPlaying = isPlaying;
        if (remoteViews == null) {
            return;
        }
        if (needRefrush) {
            remoteViews.setTextViewText(R.id.tv_custom_song_singer, name);
        }
        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.btn_custom_play, android.R.drawable.ic_media_pause);
        } else {
            remoteViews.setImageViewResource(R.id.btn_custom_play, android.R.drawable.ic_media_play);
        }
        show();
    }

    boolean isShowing = false;

    private void show() {
//        if (isShowing) {
//            return;
//        }
        manager.notify(NOTIFICATION_ID, musicNotifi);
        isShowing = true;
    }


    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_PREV_ID://上一首
                        if (musicController != null) {
                            musicController.pre();
                        }
                        break;
                    case BUTTON_PALY_ID://播放或暂停
                        if (musicController != null) {
                            if (musicController.isPlaying()) {
                                musicController.pause();
                            } else {
                                musicController.play();
                            }
                        }
                        break;

                    case BUTTON_NEXT_ID://下一首
                        if (musicController != null) {
                            musicController.next();
                        }
                        break;

                    case BUTTON_COSE_ID://关闭
                        onCancelMusicNotifi();
                        if (musicController != null) {
                            musicController.release();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

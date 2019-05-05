package com.dingxingxiang.audioplaymaster;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dingxingxiang.audioplaymaster.audio.Audio;
import com.dingxingxiang.audioplaymaster.audio.MusicController;
import com.dingxingxiang.audioplaymaster.audio.MusicPlayerService;
import com.dingxingxiang.audioplaymaster.audio.RatateImage;
import com.dingxingxiang.audioplaymaster.dialog.AudioTimerDialog;
import com.dingxingxiang.audioplaymaster.dialog.PitchDialog;
import com.dingxingxiang.audioplaymaster.dialog.RateDialog;
import com.dingxingxiang.audioplaymaster.dialog.SpeedDialog;
import com.dingxingxiang.audioplaymaster.util.AudioFlag;
import com.dingxingxiang.audioplaymaster.util.AudioPlayerConstant;
import com.dingxingxiang.audioplaymaster.util.KJLoger;
import com.dingxingxiang.audioplaymaster.util.MyUtil;
import com.dingxingxiang.audioplaymaster.util.SPManager;
import com.dingxingxiang.audioplaymaster.util.TimerFlag;
import com.smp.soundtouchandroid.AudioSpeed;
import com.smp.soundtouchandroid.MediaCallBack;
import com.smp.soundtouchandroid.OnProgressChangedListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "--ActAudioPlay--";
    //上一首
    private ImageView audio_preImg;
    //播放
    private ImageView audio_playImg;
    //下一首
    private ImageView audio_nextImg;
    //播放速度
    private TextView audio_speed;

    private TextView audioTitle;
    //当前播放的进度
    private TextView audio_curren_playTv;
    //播放总时间
    private TextView audio_totalTimeTv;
    //进度条
    private SeekBar skbProgress;
    //定时
    private View timingView;
    //当前的播放状态
    private int playerState = 0;
    //音频播放类
    private Audio audio;
    private Activity context;
    private ImageView albumView;
    private RatateImage ratateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initWidget();
    }

    private void initView() {
        albumView = findViewById(R.id.play_album_is);
        //上一首
        audio_preImg = findViewById(R.id.act_audio_player_button_prebuttonId);
        audio_preImg.setOnClickListener(this);
        //播放
        audio_playImg = findViewById(R.id.act_audio_player_button_playId);
        audio_playImg.setOnClickListener(this);

        audio_nextImg = findViewById(R.id.act_audio_player_button_nextId);
        audio_nextImg.setOnClickListener(this);

        audio_speed = findViewById(R.id.audio_speed);
        audio_speed.setOnClickListener(this);

        audioTitle = findViewById(R.id.act_book_detail_titleId);

        audio_curren_playTv = findViewById(R.id.act_audio_player_current_play_timeId);

        audio_totalTimeTv = findViewById(R.id.act_audio_player_total_timeId);

        skbProgress = findViewById(R.id.act_audio_player_audio_progressId);
        //滑动监听
        skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());

        timingView = findViewById(R.id.audio_timing);
        timingView.setOnClickListener(this);
        findViewById(R.id.audio_download).setOnClickListener(this);
        findViewById(R.id.audio_collect).setOnClickListener(this);
    }

    public void initData() {
        context = this;
        setTest();
    }

    /**
     * 再次进入的时候回调
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        KJLoger.log(TAG, "onNewIntent");
        if (intent != null) {
            Audio audio = (Audio) intent.getSerializableExtra("audio");
            boolean play = intent.getBooleanExtra("play", false);
            boolean isHead = intent.getBooleanExtra("isHead", false);
            if (isHead && audio == null) {
                if (musicController != null) {
                    audio = musicController.getAudio();
                }
            }
            if (audio != null) {
                KJLoger.log(TAG, "audio=" + audio.toString());
            }
            if (audio != null) {
                if (!isSame(audio)) {
                    setAudio(audio);
                    changeToCurrent(play);
                } else {
                    judgeState();
                    changePlayUI();
                    if (playerState == AudioPlayerConstant.ACITION_AUDIO_PLAYER_PAUSE) {
                        if (musicController != null && musicController.isPause()) {
                            setCurrentProgress(musicController.getPlayedDuration());
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        KJLoger.log(TAG, "onPause");
//        if (musicController != null) {
//            musicController.removeCallBack();
//        }
    }

    private void changeToCurrent(boolean needPlay) {
        //显示音频标题
        addAudioTitle();
        if (needPlay) {
            play(audio);
        } else if (musicController != null) {
            judgeState();
            autoPlay(false);
            changePlayUI();
        }
    }

    private boolean isSame(Audio audio) {
        if (audio == null) return false;
        return this.audio != null && audio.getId() == this.audio.getId();
    }

    public void initWidget() {
        setTitle("音乐播放器");
        handler.sendEmptyMessage(MSG_SHOW_UI);
        ratateImage = new RatateImage(context, albumView);
    }

    private float speed = AudioSpeed.SPEED_NORMAL;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_collect:
                showRatePop();
                break;
            case R.id.audio_download:
                showPitchPop();
                break;
            case R.id.audio_speed:
                showSpeedPop();
                break;
            case R.id.audio_timing:
                showTimingPop();
                break;
            //进行播放/暂停
            case R.id.act_audio_player_button_playId:
                play(audio);
                break;
            //上一首
            case R.id.act_audio_player_button_prebuttonId:
                if (musicController != null) {
                    musicController.pre();
                }
                break;
            //下一首
            case R.id.act_audio_player_button_nextId:
                if (musicController != null) {
                    musicController.next();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (popShowing()) {
            popHide();
        } else {
            //添加到后台，下次进入不用重新生成该页面
            moveTaskToBack(true);
        }
    }

    private void popHide() {
        if (timerDialog != null && timerDialog.isShowing()) {
            timerDialog.dismiss();
        }
    }

    private boolean popShowing() {
        if (timerDialog != null && timerDialog.isShowing()) {
            return true;
        }
        return false;
    }

    /**
     * 显示速度弹窗
     */
    private SpeedDialog speedDialog;

    private void showSpeedPop() {
        if (speedDialog == null) {
            speedDialog = new SpeedDialog(this, R.style.my_dialog);
            speedDialog.setOnChangeListener(new SpeedDialog.OnTimerListener() {
                @Override
                public void OnChange(float speed) {
                    if (musicController != null) {
                        musicController.setTempo(speed);
                    }
//                    MyUtil.setText(audio_speed, speed + "X");
                }
            });
        }
        speedDialog.show();
    }

    /**
     * 显示音调调节弹窗
     */
    private PitchDialog pitchDialog;

    private void showPitchPop() {
        if (pitchDialog == null) {
            pitchDialog = new PitchDialog(this, R.style.my_dialog);
            pitchDialog.setOnChangeListener(new PitchDialog.OnListener() {
                @Override
                public void onChange(float pitchSemi) {
                    if (musicController != null) {
                        musicController.setPitchSemi(pitchSemi);
                    }
                }
            });
        }
        pitchDialog.show();
    }

    /**
     * 变速变音率音调调节弹窗
     */
    private RateDialog rateDialog;

    private void showRatePop() {
        if (rateDialog == null) {
            rateDialog = new RateDialog(this, R.style.my_dialog);
            rateDialog.setOnChangeListener(new RateDialog.OnListener() {
                @Override
                public void onChange(float rate) {
                    if (musicController != null) {
                        musicController.setRateChange(rate);
                    }
                }
            });
        }
        rateDialog.show();
    }

    /**
     * 显示定时弹窗
     */
    private AudioTimerDialog timerDialog;

    private void showTimingPop() {
        if (timerDialog == null) {
            timerDialog = new AudioTimerDialog(this, R.style.my_dialog);
            timerDialog.setOnChangeListener(new AudioTimerDialog.OnTimerListener() {
                @Override
                public void OnChange() {
                    int timerState = SPManager.getTimerState(context);
                    switch (timerState) {
                        case TimerFlag.CLOSE:
                        case TimerFlag.CURRENT:
                            //取消定时功能
                            if (connect && musicController != null) {
                                musicController.cancelDelay();
                            }
                            break;
                        default:
                            //定时关闭功能
                            if (connect && musicController != null) {
                                musicController.delayClose(timerState);
                            }
                            break;
                    }
                }
            });
        }
        timerDialog.show();
    }

    //显示UI
    private final int MSG_SHOW_UI = 1;
    //准备播放
    private final int MSG_AUDIO_PREPARE = MSG_SHOW_UI + 1;
    //自动播放
    private final int MSG_AUTO_PLAY = MSG_AUDIO_PREPARE + 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_UI:
                    update();
                    break;
                case MSG_AUTO_PLAY:
                    autoPlay(needPlay);
                    break;
//                case MSG_AUDIO_PREPARE:
////                    onPrepare();
//                    initMusicService(true);
//                    changePlayUI();
//                    break;
            }
        }
    };

    boolean needPlay;

    private void initMusicService(boolean needPlay) {
        this.needPlay = needPlay;
        Intent intent = new Intent(this, MusicPlayerService.class);
        if (!MyUtil.isServiceRunning(this, MusicPlayerService.class.getName())) {
            KJLoger.log(TAG, "服务未开启");
            //开启服务
            startService(intent);
        }
        //绑定服务
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private boolean connect;
    private MusicController musicController;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connect = true;
            if (service == null) return;
            musicController = ((MusicPlayerService.MyBinder) service).getMusicController();
            KJLoger.log(TAG, "musicController=" + musicController);
            if (musicController.getPlayList() == null || musicController.getPlayList().isEmpty()) {
                if (audioList != null) {
                    musicController.setPlayList(audioList);
                }
            }
            musicController.setOnProgressChangedListener(progressChangedListener);
            musicController.setMediaCallBack(mediaCallBack);
            onPrepare();
            KJLoger.log(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connect = false;
            KJLoger.log(TAG, "onServiceDisconnected");
        }
    };

    private void onPrepare() {
        if (musicController != null) {
            speed = musicController.getTemp();
//            MyUtil.setText(audio_speed, speed + "X");
        }
        judgeState();
        changePlayUI();
        handler.sendEmptyMessage(MSG_AUTO_PLAY);
    }

    private void setCurrentProgress(long position) {
        if (skbProgress != null) {
            skbProgress.setProgress((int) position);
        }
        if (audio_curren_playTv != null) {
            audio_curren_playTv.setText(getTimeStr((int) position));
        }
    }

    private void setInitDate(long duration) {
        KJLoger.log(TAG, "duration=" + duration);
        if (musicController == null) return;
        if (skbProgress != null) {
            skbProgress.setMax((int) duration);
        }
        if (audio_totalTimeTv != null) {
            audio_totalTimeTv.setText(getTimeStr((int) duration));
        }
    }

    private void play() {
        if (!connect) {
            initMusicService(true);
            return;
        }
        if (musicController != null) {
            musicController.play(audio);
        }
    }

    MediaCallBack mediaCallBack = new MediaCallBack() {
        @Override
        public void onError() {

        }

        @Override
        public void onChange(int state) {
            if (state == AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY_PRE_AUDIO || state == AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY_NEXT_AUDIO) {
                playerState = 0;
                if (musicController != null) {
                    setAudio(musicController.getAudio());
                    setCurrentProgress(0);
                    addAudioTitle();
                }
            } else {
                playerState = state;
            }
            KJLoger.log(TAG, "onChange=" + state);
            changePlayUI();
        }

        @Override
        public void onPrepare() {
            KJLoger.log(TAG, "onPrepare()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialog();
                }
            });
        }

        @Override
        public void onPrepared(long duration) {
            KJLoger.log(TAG, "onPrepared()");
            setInitDate(duration);
        }

        @Override
        public void onPlay() {
            KJLoger.log(TAG, "onPlay()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                }
            });
        }

        @Override
        public void onStop() {
            KJLoger.log(TAG, "onStop()");
        }

        @Override
        public void onPause() {
            KJLoger.log(TAG, "onPause()");
            if (timerDialog != null) {//更新定时方式
                timerDialog.updateSelect();
            }
        }

        @Override
        public void onComplete() {
            KJLoger.log(TAG, "onComplete()");
        }
    };

    private OnProgressChangedListener progressChangedListener = new OnProgressChangedListener() {
        @Override
        public void onProgressChanged(int track, double currentPercentage, long position) {
            setCurrentProgress(position);
        }

        @Override
        public void onTrackEnd(int track) {

        }

        @Override
        public void onExceptionThrown(String string) {

        }
    };

    /**
     * 自动播放
     */
    private void autoPlay(boolean needPlay) {
        KJLoger.log(TAG, "autoPlay:playerState=" + playerState);
        switch (playerState) {
            //正在播放，更新播放ui
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY:
                if (musicController != null && musicController.isPlaying()) {
                    setInitDate(musicController.getDuration());
                }
                break;
            //暂停状态，更新ui
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PAUSE:
                if (musicController != null && musicController.isPause()) {
                    setInitDate(musicController.getDuration());
                    setCurrentProgress(musicController.getPlayedDuration());
                }
                break;
            //未播放，执行播放
            default:
                if (needPlay) {
                    play();
                }
                break;
        }
    }

    /**
     * 执行播放点击事件
     */
    private void play(Audio audio) {
        if (audio == null) return;
        this.audio = audio;
        if (audio.getFileUrl() == null) return;
        KJLoger.log(TAG, "点击的id = " + audio.getId() + " url=" + audio.getFileUrl());
        judgeState();
        switch (playerState) {
            /**
             * 直接暂停
             * */
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY:
                KJLoger.log(TAG, "---执行暂停--");
                if (musicController != null) {
                    musicController.pause();
                }
                break;
            /**
             * 直接开始
             * */
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PREPARE:
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY_COMPLETE:
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_STOP:
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PAUSE:
                KJLoger.log(TAG, "---执行播放--");
                play();
                break;
            /**
             * 不是播放的一个
             *1.判断是否是当前页的播放列表
             *2.是： 执行播放当前的某一个；否：将列表传给playerService 并播放当前点击的那个音频
             */
            case AudioFlag.NOT_PLAY_ITEM:
                KJLoger.log(TAG, "---播放特定的某一首--");
                play();
                break;
        }
    }

    /**
     * 判断状态
     */
    private void judgeState() {
        if (audio == null) return;
        if (musicController != null && musicController.getCurrentId() == audio.getId()) {
            playerState = musicController.getState();
        } else {//不是用以个音频
            playerState = AudioFlag.NOT_PLAY_ITEM;
        }
        KJLoger.log(TAG, "judgeState：playerState = " + playerState);
    }

    /**
     * 改变按钮状态
     */
    private void changePlayUI() {
        KJLoger.log(TAG, "changePlayUI:playerState=" + playerState);
        boolean isMainThread = Thread.currentThread() == Looper.getMainLooper().getThread();
        if (!isMainThread) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changePlayUIInternal();
                }
            });
        } else {
            changePlayUIInternal();
        }
    }

    private void changePlayUIInternal() {
        switch (playerState) {
            //正在播放
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY:
                setImg(audio_playImg, R.mipmap.audio_state_play);
                if (ratateImage != null) {
                    ratateImage.startSpin();
                }
                break;
            //暂停/未播放
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PAUSE:
                setImg(audio_playImg, R.mipmap.audio_state_pause);
                if (ratateImage != null) {
                    ratateImage.stopSpin();
                }
                break;
            //播放完成
            case AudioPlayerConstant.ACITION_AUDIO_PLAYER_PLAY_COMPLETE:
                setImg(audio_playImg, R.mipmap.audio_state_pause);
                if (ratateImage != null) {
                    ratateImage.stopSpin();
                }
                //更新进度
                if (skbProgress != null && skbProgress.getProgress() != skbProgress.getMax()) {
                    skbProgress.setProgress(skbProgress.getMax());
                }
                break;
            //未播放/播放错误
            default:
                if (ratateImage != null) {
                    ratateImage.stopSpin();
                }
                setImg(audio_playImg, R.mipmap.audio_state_pause);
                //更新进度
                if (skbProgress != null) {
                    skbProgress.setProgress(0);
                }
                break;
        }
    }

    private void setImg(ImageView imageView, int imgRes) {
        if (imageView == null) return;
        imageView.setImageResource(imgRes);
    }

    /**
     * 更新页面数据
     */
    private void update() {
        addAudioTitle();
        changePlayUI();
    }

    /**
     * 显示音频标题
     */
    private void addAudioTitle() {
        if (this.audio == null) return;
        MyUtil.setText(audioTitle, audio.getName());
    }

    private void setAudio(Audio audio) {
        this.audio = audio;
    }

    /**
     * 滑动条监听
     */
    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        //单位:s
        long progress;
        long total_time;
        boolean fromUser;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (musicController != null) {
                total_time = musicController.getDuration();
                if (seekBar.getMax() != 0) {
                    this.progress = progress * total_time / seekBar.getMax();
                }
            }
            this.fromUser = fromUser;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (fromUser) {
                KJLoger.log("debug", "  fromUser:" + fromUser + "  progress=" + progress);
                //s—>ms
                if (musicController != null) {
                    musicController.seekTo(progress);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KJLoger.log(TAG, "onDestroy");
        //如果音频正在播放，停止获取播放进度
        if (connect && serviceConnection != null) {
            if (musicController != null) {
                musicController.removeCallBack();
            }
            unbindService(serviceConnection);
        }
    }

    /**
     * 获取总时长
     */
    private String getTimeStr(int time) {
        time = time / 1000000;
        int min = time / 60;//分
        int second = time % 60;
        String min1 = "";
        if (min < 10) {
            min1 = "0" + min;
        } else {
            min1 = min + "";
        }
        String second1 = "";
        if (second < 10) {
            second1 = "0" + second;
        } else {
            second1 = second + "";
        }
        return min1 + ":" + second1;
    }

    private String[] fileArr = {
            "http://sc1.111ttt.cn/2017/1/11/11/304112002493.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205102140160.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205101753237.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205100947591.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100155247.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100109191.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100107217.mp3",
            "http://sc1.111ttt.cn/2017/1/11/11/304112002493.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205102140160.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205101753237.mp3",
            "http://sc1.111ttt.cn/2016/1/12/10/205100947591.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100155247.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100109191.mp3",
            "http://sc1.111ttt.cn/2016/5/12/10/205100107217.mp3",
            "http://119.254.198.163:8090/lpdpres/audio/20180808173341220.mp3"
    };
    //音频列表
    private List<Audio> audioList = new ArrayList<>();

    private void setTest() {
        for (int i = 0; i < fileArr.length; i++) {
            Audio audio = new Audio();
            audio.setFileUrl(fileArr[i]);
            audio.setId(i + 1);
            audio.setType(1);
            audio.setName("音频" + (i + 1));
            if (i == fileArr.length - 1) {
                audio.setLock(true);
            }
            audioList.add(audio);
        }
        this.audio = audioList.get(0);
    }
}

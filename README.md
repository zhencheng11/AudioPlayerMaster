#Android音乐播放器【支持：速率调节，音调调节，采样率调节】

这是一个音乐播放器，支持：速率调节，音调调节，采样率，这三个功能基于soundTouch开源项目，解决了Android 6.0之前不能调节播放速率的问题。
由于公司项目需要做一个倍速播放的音乐播放器，我们知道，使用Android 自带的MediaPlayer的在Android 6.0之前的是不支持倍速播放的，我看过很多关于音频播放的开源项目，找到[soundTouch](http://www.surina.net/soundtouch/)能够改变音频播放速度，但是没有一个完整的能封装成播放器的。于是，我结合AudioTrack+MediaExtractor+MediaCodec+[SoundTouch](http://www.surina.net/soundtouch/)封装成一个音乐播放器。因为之前都是用Android 自带的MediaPlayer进行播放，使用这些新技术我花了很多的时间去了解他们的使用。
先看项目截图：UI有点粗糙，见谅。
<div>
<img src="https://img-blog.csdnimg.cn/20190430172010891.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
<img src="https://img-blog.csdnimg.cn/20190430173432392.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
<img src="https://img-blog.csdnimg.cn/20190430173539804.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
</div>


<div>
<img src="https://img-blog.csdnimg.cn/20190430173657540.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
<img src="https://img-blog.csdnimg.cn/20190430173729789.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
<img src="https://img-blog.csdnimg.cn/20190430173906427.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=31%>
</img>
</div>

<div>
<img src="https://img-blog.csdnimg.cn/20190430173948937.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=35%>
</img>
<img src="https://img-blog.csdnimg.cn/2019043017402022.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMzcyMzcw,size_16,color_FFFFFF,t_70" alt="" width=35%>
</img>
</img>
</div>

以下介绍项目重要功能和技术：
## 一、播放服务的使用
之前我写过一篇过于Service的使用，这里正好用到里面的一些知识，就是startService和bindService的混合使用，我们知道，音频播放都是在后台进行播放，我们推出页面的时候需要音乐也继续在后台播放，所以我们需要开启一个服务来播放，但是同时，我们需要在显示播放界面的时候也能看到播放状态，于是我们需要绑定到这个服务上面，监听回调后台音乐播放的状态展示给用户，这里我就不细说了，不明白的可以看我的博客【[Android Service的使用](https://blog.csdn.net/qq_33372370/article/details/89675318)】。
## 二、播放器控制
对于只要求正常播放的话，使用AudioTrack+MediaExtractor+MediaCodec就可以了，MediaExtractor用于加载资源，可以加载网络资源，本地资源，通过setDataSource（）方法，设置要加载的资源；MediaCodec将MediaExtractor的流进行编码然后传给AudioTrack进行播放;MeAudioTrack用于播放，可以通过传入音频流就可以进行播放，内部方法为`write（）`方法，将音频流写入，再调用`play()`方法就可以进行播放。对于需要设置播放倍速，设置音调来说，以上三个类的组合是不行的，需要借助于[soundTouch](http://www.surina.net/soundtouch/)它是一个开源项目，专门针对调节播放速率，调节音调，原理是将MediaCodec解码后的流传给soundTouch，soundTouch进行进一步的变化之后，再传给AudioTrack进行播放。
### 1、设置播放倍速
`private static synchronized native final void setTempo(int track, float tempo);`

通过soundTouch的本地方法`setTempo（）` 设置播放速率，这种情况下，只是改变了播放的速度，它的音调并没有改变，传入自己需要的倍数就可以进行倍速播放，1倍速代表正常的倍数。
### 2、设置音调

    private static synchronized native final void setPitchSemi(int track, float pitchSemi);
  
 通过传入音调数值，取值范围[-12-12]可以调节音调。
### 3、设置采样率

     private static synchronized native final void setRate(int track, float rate);
通过`setRate（）`方法，rate的取值范围[-50-100]，可以设置采样频率也就是音律，改变播放速度的同时也改变了音调，所以它是`变速又变调`的。
## 三、状态栏显示播放器
通过Notification将播放状态显示在状态栏，需要注意的是系统适配问题
 //Android 8.0以后
   

     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(context.getPackageName(), "测试音频", NotificationManager.IMPORTANCE_HIGH);
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
在Android 8.0以后，通知需要设置Channelld才能显示，需要用`NotificationChannel` 类来进行配置，还有设置声音和震动也需要用此类来进行配置。

## 四、音频焦点的使用
在我们播放音频的时候，我们希望其他正在播放音频的软件暂停播放，这里就需要使用到获取焦点的这个类`AudioManager`,通过AudioManager的`requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint)`来获取音频的焦点，这样，其他的音频也就会暂停播放了，当需要释放焦点时通过`abandonAudioFocus(OnAudioFocusChangeListener l)`方法来释放焦点。
## 五、联动系统媒体中心MediaSession的使用
如上面的最后一个图，通过MediaSessionCompat设置联动媒体，跟我们自己的播放器同步，可以显示在锁屏页面。
创建MediaSessionCompat

    /**
         * 初始化并激活MediaSession
         */
        private void setupMediaSession() {
            mMediaSession = new MediaSessionCompat(context, TAG);
            mMediaSession.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            );
            //设置播放监听
            mMediaSession.setCallback(callback);
            mMediaSession.setActive(true);
        }
更新媒体状态

      /**
         * 更新播放状态，播放/暂停/拖动进度条时调用
         */
        public void updatePlaybackState() {
            int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mMediaSession.setPlaybackState(
                    new PlaybackStateCompat.Builder()
                            .setActions(MEDIA_SESSION_ACTIONS)
                            .setState(state, getCurrentPosition(), 1)
                            .build());
        }
个更新媒体信息

    /**
     * 更新正在播放的音乐信息，切换歌曲时调用
     */
    public void updateMetaData(Audio audio) {
        if (audio == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        Audio info = control.getAudio();
        MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, info.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, control.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getCoverBitmap(info));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getCount());
        }

        mMediaSession.setMetadata(metaData.build());
    }

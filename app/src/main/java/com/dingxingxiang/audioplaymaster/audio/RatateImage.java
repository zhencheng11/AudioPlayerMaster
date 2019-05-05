package com.dingxingxiang.audioplaymaster.audio;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

/**
 * 播放动画
 */

public final class RatateImage {
    private final ValueAnimator rotateAnim;
    private boolean isSpin = false;

    public RatateImage(Context context, final ImageView view) {
        rotateAnim = ObjectAnimator.ofFloat(0, 360);
        rotateAnim.setDuration(45 * 1000);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        view.setRotation(0.0f);
        rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setRotation(value);
            }
        });
    }

    public void startSpin() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused()) {
                rotateAnim.resume();
            } else {
                rotateAnim.start();
            }
        } else {
            rotateAnim.start();
        }

        isSpin = true;
    }

    public void stopSpin() {

        if (rotateAnim.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                rotateAnim.pause();
            } else {
                rotateAnim.cancel();
            }
            isSpin = false;
        }
    }

    public boolean isSpin() {
        return isSpin;
    }

}

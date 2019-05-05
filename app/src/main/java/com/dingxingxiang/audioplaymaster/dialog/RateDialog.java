package com.dingxingxiang.audioplaymaster.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dingxingxiang.audioplaymaster.util.KJLoger;
import com.dingxingxiang.audioplaymaster.R;

/**
 * Created by dingxingxing on 2019.4.4
 * 音速音律调节弹窗
 */
public class RateDialog extends Dialog implements View.OnClickListener {
    private Activity mContext;
    private String TAG = "--SpeedDialog--";
    private SeekBar pitch_seek;
    private TextView rate_show;

    public RateDialog(Context context) {
        super(context);
        init(context);
    }

    public RateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = (Activity) context;
        View view = mContext.getLayoutInflater().inflate(R.layout.dialog_rate_item, null);
        setContentView(view);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setWindowAnimations(R.style.popupAnimation);
        dialogWindow.setAttributes(lp);
        //dissmiss
        view.findViewById(R.id.cancel_action).setOnClickListener(this);
        pitch_seek = (SeekBar) findViewById(R.id.rate_seek);
        pitch_seek.setOnSeekBarChangeListener(onRateSeekBarListener);
        rate_show = (TextView) findViewById(R.id.pitch_show);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //取消
            case R.id.cancel_action:
                KJLoger.log(TAG, " 取消");
                dismiss();
                break;
        }
    }

    private SeekBar.OnSeekBarChangeListener onRateSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            float r = seekBar.getProgress() - 50;
            Log.e("AA", "onStopTrackingTouch  pitch" + r + "%");
            if (onTimerListener != null) {
                onTimerListener.onChange(r);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float r = progress - 50;
            rate_show.setText("变速变音率: " + r);
        }
    };
    private OnListener onTimerListener;

    public void setOnChangeListener(OnListener onTimerListener) {
        this.onTimerListener = onTimerListener;
    }

    public interface OnListener {
        void onChange(float speed);
    }
}

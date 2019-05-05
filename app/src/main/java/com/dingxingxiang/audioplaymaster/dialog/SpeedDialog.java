package com.dingxingxiang.audioplaymaster.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dingxingxiang.audioplaymaster.util.KJLoger;
import com.dingxingxiang.audioplaymaster.util.MyUtil;
import com.dingxingxiang.audioplaymaster.R;
import com.dingxingxiang.audioplaymaster.util.TimerEntity;
import com.smp.soundtouchandroid.AudioSpeed;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingxingxing on 2019.4.4
 * 音频播放速度选择弹窗
 */
public class SpeedDialog extends Dialog implements View.OnClickListener {
    private Activity mContext;
    private String TAG = "--SpeedDialog--";
    private MyAdapter adapter;

    public SpeedDialog(Context context) {
        super(context);
        init(context);
    }

    public SpeedDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mContext = (Activity) context;
        View view = mContext.getLayoutInflater().inflate(R.layout.audio_timer_dialog, null);
        setContentView(view);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setWindowAnimations(R.style.popupAnimation);
        dialogWindow.setAttributes(lp);
        //dissmiss
        view.findViewById(R.id.cancel_action).setOnClickListener(this);
        ListView listView = findViewById(R.id.listviewId);
        adapter = new MyAdapter(getContext());
        adapter.setData(getData());
        listView.setAdapter(adapter);
    }

    private List<TimerEntity> getData() {
        List<TimerEntity> list = new ArrayList<>();
        list.add(new TimerEntity("0.5倍语速", AudioSpeed.SPEED_05));
        list.add(new TimerEntity("正常语速", AudioSpeed.SPEED_NORMAL, true));
        list.add(new TimerEntity("1.5倍语速", AudioSpeed.SPEED_15));
        list.add(new TimerEntity("2倍语速", AudioSpeed.SPEED_2));
        return list;
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

    private OnTimerListener onTimerListener;

    public void setOnChangeListener(OnTimerListener onTimerListener) {
        this.onTimerListener = onTimerListener;
    }

    public interface OnTimerListener {
        void OnChange(float speed);
    }

    private class MyAdapter extends BaseAdapter {
        List<TimerEntity> data;
        private Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.audio_timer_item, null);
                holder.selectView = convertView.findViewById(R.id.select_img);
                holder.titleTv = convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (data == null || position > data.size() - 1) return convertView;
            final TimerEntity entity = data.get(position);
            //标题
            MyUtil.setText(holder.titleTv, entity.getTitle());
            //背景
            MyUtil.setBgColor(context, convertView, entity.isSelect() ? R.color.f5f5f5 : R.color.white);
            //选中按钮
            MyUtil.setVisible(holder.selectView, entity.isSelect() ? View.VISIBLE : View.GONE);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entity.isSelect()) return;
                    clear();
                    entity.setSelect(true);
//                    SPManager.write(context, SP.TIMER_STATE, entity.getTimeState());
                    updateTimer(entity.getSpeed());
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private void updateTimer(float speed) {
            if (onTimerListener != null) {
                onTimerListener.OnChange(speed);
            }
        }

        private void clear() {
            if (data == null) return;
            for (TimerEntity entity : data) {
                if (entity.isSelect()) {
                    entity.setSelect(false);
                }
            }
        }

        public void setData(List<TimerEntity> data) {
            this.data = data;
        }

        private class ViewHolder {
            private TextView titleTv;
            private View selectView;
        }
    }
}

package com.dingxingxiang.audioplaymaster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by dingxingxiang
 * 基本Activity 继承类
 * 所有的activity都应继承它，方便管理
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private KProgressHUD dialog;
    //页面是否处于前台
    boolean isFront = false;

    @Override
    protected void onResume() {
        super.onResume();
        isFront = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    protected void showDialog() {
        if (!isFront) return;
        try {
            if (dialog == null) {
                dialog = KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
            } else {
                dialog.setLabel(null);
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

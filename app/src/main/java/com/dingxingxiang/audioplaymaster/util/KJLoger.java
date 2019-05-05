package com.dingxingxiang.audioplaymaster.util;

import android.util.Log;

public final class KJLoger {
    public static boolean IS_DEBUG = true;
    public static boolean DEBUG_LOG = true;
    public static boolean SHOW_ACTIVITY_STATE = true;

    public KJLoger() {
    }

    public static final void openDebutLog(boolean enable) {
        IS_DEBUG = enable;
        DEBUG_LOG = enable;
    }

    public static final void openActivityState(boolean enable) {
        SHOW_ACTIVITY_STATE = enable;
    }

    public static final void debug(String msg) {
        if (IS_DEBUG) {
            Log.i("debug", msg);
        }

    }

    public static final void log(String packName, String state) {
        debugLog(packName, state);
    }

    public static final void debug(String msg, Throwable tr) {
        if (IS_DEBUG) {
            Log.i("debug", msg, tr);
        }

    }

    public static final void state(String packName, String state) {
        if (SHOW_ACTIVITY_STATE) {
            Log.d("activity_state", packName + state);
        }

    }

    public static final void debugLog(String packName, String state) {
        if (DEBUG_LOG) {
            Log.d("debug", packName + state);
        }

    }

    public static final void exception(Exception e) {
        if (DEBUG_LOG) {
            e.printStackTrace();
        }

    }

    public static final void debug(String msg, Object... format) {
        debug(String.format(msg, format));
    }
}
package com.coocpu.orgmap;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenTools {
    private DisplayMetrics mMetrics = new DisplayMetrics();

    public ScreenTools(Context context) {
        try {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getDensity() {
        return mMetrics.density;
    }

    public int dp2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

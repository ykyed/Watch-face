package com.wooastudio.task1.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtils {

    public static float pxToDp(Context context, float px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
}

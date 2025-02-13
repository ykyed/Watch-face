package com.wooastudio.task1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    public static int getImageWidth(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        return options.outWidth;
    }

    public static int getImageHeight(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        return options.outHeight;
    }

    public static Bitmap loadBitmap(Context context, int resId, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        Bitmap originBitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        return Bitmap.createScaledBitmap(originBitmap, width, height, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        int originWidth = options.outWidth;
        int originHeight = options.outHeight;
        int inSampleSize = 1;

        if (originWidth > width || originHeight > height) {
            int halfWidth = originWidth / 2;
            int halfHeight = originHeight / 2;

            while ((halfWidth / inSampleSize) >= width && (halfHeight / inSampleSize) >= height) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

package com.wooastudio.task1.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wooastudio.task1.R;
import com.wooastudio.task1.utils.BitmapUtils;
import com.wooastudio.task1.utils.DisplayUtils;

import java.time.ZonedDateTime;
import java.util.Calendar;

public class WatchFaceView extends View {

    private static final String TAG = "WatchFaceView";

    private Bitmap scaledBgImage, scaledMinuteHand, scaledHourHand;
    private int currentHour, currentMinute, currentSecond;
    private float left, top, centerX, centerY;

    private ValueAnimator animator;
    private Paint progressPaint, drawablePaint;
    private RectF progressRectF;
    private float animatedProgress;

    private final Handler handler = new Handler();

    public WatchFaceView(Context context) {
        super(context);
        init();
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(getResources().getColor(R.color.progress_indicator, getContext().getTheme()));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        drawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int bgSize = Math.min(w, h);

        int scaledMinuteHandWidth = BitmapUtils.getImageWidth(getContext(), R.drawable.minute_hand) * bgSize / BitmapUtils.getImageWidth(getContext(), R.drawable.background);
        int scaledMinuteHandHeight = BitmapUtils.getImageHeight(getContext(), R.drawable.minute_hand) * bgSize / BitmapUtils.getImageHeight(getContext(), R.drawable.background);
        int scaledHourHandWidth = BitmapUtils.getImageWidth(getContext(), R.drawable.hour_hand) * bgSize / BitmapUtils.getImageWidth(getContext(), R.drawable.background);
        int scaledHourHandHeight = BitmapUtils.getImageHeight(getContext(), R.drawable.hour_hand) * bgSize / BitmapUtils.getImageHeight(getContext(), R.drawable.background);

        scaledBgImage = BitmapUtils.loadBitmap(getContext(), R.drawable.background, bgSize, bgSize);
        scaledMinuteHand = BitmapUtils.loadBitmap(getContext(), R.drawable.minute_hand, scaledMinuteHandWidth, scaledMinuteHandHeight);
        scaledHourHand = BitmapUtils.loadBitmap(getContext(), R.drawable.hour_hand, scaledHourHandWidth, scaledHourHandHeight);

        left = (float) (getWidth() - scaledBgImage.getWidth()) / 2;
        top = (float) (getHeight() - scaledBgImage.getHeight()) / 2;
        centerX = left + ((float) scaledBgImage.getWidth() / 2);
        centerY = top + ((float) scaledBgImage.getHeight() / 2);

        float progressIndicatorRadius = (float) scaledBgImage.getWidth() / 2.6f;
        float progressIndicatorStrokeWidth = DisplayUtils.pxToDp(getContext(), (int) (scaledMinuteHand.getWidth() * 2.5));
        progressPaint.setStrokeWidth(progressIndicatorStrokeWidth);
        progressRectF = new RectF(centerX - progressIndicatorRadius, centerY - progressIndicatorRadius, centerX + progressIndicatorRadius, centerY + progressIndicatorRadius);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(scaledBgImage, left, top, drawablePaint);
        canvas.save();

        canvas.rotate((currentMinute * 6) + ((float) currentSecond / 10), centerX, centerY);
        canvas.drawBitmap(scaledMinuteHand, centerX - ((float) scaledMinuteHand.getWidth() / 2), centerY - scaledMinuteHand.getHeight() + (float) scaledMinuteHand.getWidth() / 2, drawablePaint);
        canvas.restore();
        canvas.save();

        canvas.rotate((currentHour * 30) + ((float) currentMinute / 2) + ((float) currentSecond / 120), centerX, centerY);
        canvas.drawBitmap(scaledHourHand, centerX- ((float) scaledHourHand.getWidth() / 2), centerY - scaledHourHand.getHeight() + (float) scaledHourHand.getWidth() / 2, drawablePaint);
        canvas.restore();

        canvas.drawArc(progressRectF, -90f, animatedProgress, false, progressPaint);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            getCurrentTime();
            handler.post(updateClock);
            startProgressIndicatorAnimation();
        }
        else {
            handler.removeCallbacks(updateClock);
            cancelProgressIndicatorAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelProgressIndicatorAnimation();
    }

    private void getCurrentTime() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZonedDateTime now = ZonedDateTime.now();
            currentHour = now.getHour() % 12;
            currentMinute = now.getMinute();
            currentSecond = now.getSecond();
            Log.d(TAG, "getCurrentTime(), ZonedDateTime: " + now);
        }
        else {
            Calendar calendar = Calendar.getInstance();
            currentHour = calendar.get(Calendar.HOUR);
            currentMinute = calendar.get(Calendar.MINUTE);
            currentSecond = calendar.get(Calendar.SECOND);
            Log.d(TAG, "getCurrentTime(), Calendar: " + String.format("%02d:%02d:%02d", currentHour, currentMinute, currentSecond));
        }
    }

    private void startProgressIndicatorAnimation() {

        animator = ValueAnimator.ofFloat(-360f, 360f);
        animator.setCurrentFraction((0.5f * currentSecond / 60) + 0.5f);
        animator.setDuration(120000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(valueAnimator -> {
            animatedProgress = (float)valueAnimator.getAnimatedValue();
            invalidate();
        });

        if (animator.isRunning()) {
            animator.cancel();
        }
        animator.start();
    }

    private void cancelProgressIndicatorAnimation() {
        if (animator.isRunning()) {
            animator.cancel();
        }
    }

    private final Runnable updateClock = new Runnable() {
        @Override
        public void run() {

            long currentTimeMillis = SystemClock.elapsedRealtime();
            Log.d(TAG, "updateClock, Current Time: " + String.format("%02d:%02d:%02d", currentHour, currentMinute, currentSecond));

            currentSecond++;
            if (currentSecond == 60) {
                currentSecond = 0;
                currentMinute++;
            }

            if (currentMinute == 60) {
                currentMinute = 0;
                currentHour++;
            }

            currentHour %= 12;

            long nextUpdateTime = currentTimeMillis + (1000 - (currentTimeMillis % 1000));
            handler.postAtTime(this, nextUpdateTime);
        }
    };
}

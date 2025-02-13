package com.wooastudio.task1.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
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

    private ValueAnimator progressIndicatorAnimator;
    private Paint progressIndicatorPaint, drawablePaint;
    private RectF progressIndicatorRectF;
    private float ProgressIndicatorAngle;

    private final Choreographer choreographer = Choreographer.getInstance();
    private long lastUpdateTime = 0;

    public WatchFaceView(Context context) {
        super(context);
        init();
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressIndicatorPaint.setColor(getResources().getColor(R.color.progress_indicator, getContext().getTheme()));
        progressIndicatorPaint.setStyle(Paint.Style.STROKE);
        progressIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);

        drawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        // for anti-aliasing
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int bgSize = Math.min(w, h);

        int bgWidth = BitmapUtils.getImageWidth(getContext(), R.drawable.background);
        int bgHeight = BitmapUtils.getImageHeight(getContext(), R.drawable.background);

        int scaledMinuteHandWidth = BitmapUtils.getImageWidth(getContext(), R.drawable.minute_hand) * bgSize / bgWidth;
        int scaledMinuteHandHeight = BitmapUtils.getImageHeight(getContext(), R.drawable.minute_hand) * bgSize / bgHeight;
        int scaledHourHandWidth = BitmapUtils.getImageWidth(getContext(), R.drawable.hour_hand) * bgSize / bgWidth;
        int scaledHourHandHeight = BitmapUtils.getImageHeight(getContext(), R.drawable.hour_hand) * bgSize / bgHeight;

        scaledBgImage = BitmapUtils.loadBitmap(getContext(), R.drawable.background, bgSize, bgSize);
        scaledMinuteHand = BitmapUtils.loadBitmap(getContext(), R.drawable.minute_hand, scaledMinuteHandWidth, scaledMinuteHandHeight);
        scaledHourHand = BitmapUtils.loadBitmap(getContext(), R.drawable.hour_hand, scaledHourHandWidth, scaledHourHandHeight);

        left = (float) (getWidth() - scaledBgImage.getWidth()) / 2;
        top = (float) (getHeight() - scaledBgImage.getHeight()) / 2;
        centerX = left + ((float) scaledBgImage.getWidth() / 2);
        centerY = top + ((float) scaledBgImage.getHeight() / 2);

        float progressIndicatorRadius = (float) scaledBgImage.getWidth() / 2.6f;
        float progressIndicatorStrokeWidth = DisplayUtils.pxToDp(getContext(), (int) (scaledMinuteHand.getWidth() * 2.5));
        progressIndicatorPaint.setStrokeWidth(progressIndicatorStrokeWidth);
        progressIndicatorRectF = new RectF(centerX - progressIndicatorRadius, centerY - progressIndicatorRadius, centerX + progressIndicatorRadius, centerY + progressIndicatorRadius);
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

        canvas.drawArc(progressIndicatorRectF, -90f, ProgressIndicatorAngle, false, progressIndicatorPaint);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            updateCurrentTime();
            choreographer.postFrameCallback(frameCallback);
            startProgressAnimation();
        }
        else {
            choreographer.removeFrameCallback(frameCallback);
            cancelProgressAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelProgressAnimation();
    }

    private void updateCurrentTime() {
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

    private void startProgressAnimation() {

        progressIndicatorAnimator = ValueAnimator.ofFloat(-360f, 360f);
        progressIndicatorAnimator.setCurrentFraction((0.5f * currentSecond / 60) + 0.5f);
        progressIndicatorAnimator.setDuration(120000);
        progressIndicatorAnimator.setInterpolator(new LinearInterpolator());
        progressIndicatorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        progressIndicatorAnimator.addUpdateListener(valueAnimator -> {
            ProgressIndicatorAngle = (float)valueAnimator.getAnimatedValue();
            invalidate();
        });

        if (progressIndicatorAnimator.isRunning()) {
            progressIndicatorAnimator.cancel();
        }
        progressIndicatorAnimator.start();
    }

    private void cancelProgressAnimation() {
        if (progressIndicatorAnimator.isRunning()) {
            progressIndicatorAnimator.cancel();
        }
    }

    private void incrementTime() {
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
        Log.d(TAG, "updateClock, Current Time: " + String.format("%02d:%02d:%02d", currentHour, currentMinute, currentSecond));
    }

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis / 1000 > lastUpdateTime / 1000) {
                incrementTime();
                lastUpdateTime = currentTimeMillis;
            }
            choreographer.postFrameCallback(this);
        }
    };
}

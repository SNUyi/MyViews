package com.sunny.myviews.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * QQ图片发送样式
 * 图片发送光晕进度条View
 * Created by slp on 2018/4/18.
 */
public class SendImgProgressView extends View {

    private Paint mPaint;
    private Context mContext;
    //圆环画布
    private Canvas circleCanvas;
    //圆环图
    private Bitmap circleBitmap;
    //圆环光晕画布
    private Canvas circleHaloCanvas;
    //圆环光晕图
    private Bitmap circleHaloBitmap;
    //密度
    private Float density;

    //图片遮盖模式
    private PorterDuffXfermode clearXF;
    private PorterDuffXfermode dstOutXF;
    private PorterDuffXfermode srcOutXF;

    //圆环的内圈半径
    private float circleInnerRadius;
    //    圆环的外圈半径
    private float circleOuterRadius;
    //    圆环光晕的内半径
    private float circleHaloInnerRadius;
    //    圆环光晕的外半径
    private float circleHaloOuterRadius;
    //    背景颜色
    private int circleBgColor;
    //    圆环颜色
    private int circleColor;
    //    圆环光晕颜色
    private int circleHaloColor;
    //  动画时, 外圈允许的最大半径
    private float circleOuterAnimMaxRadius;
    private float circleHaloInnerAnimMiniRadius;
    private float circleHaloOuterAnimMaxRadius;
    //    结束动画圆圈的半径
    private float circleFinishDrawRadius;
    //    进度
    private int progress = 0;
    //  动画进度
    private float animatorValue = 0f;

    private ValueAnimator animator;
    private ValueAnimator animatorFinish;

    public SendImgProgressView(Context context) {
        this(context, null);
    }

    public SendImgProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SendImgProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        density = context.getResources().getDisplayMetrics().density;

        clearXF = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        dstOutXF = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        srcOutXF = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);

        circleInnerRadius = 16 * density;
        circleOuterRadius = 17 * density;

        circleBgColor = Color.parseColor("#70000000");
        circleColor = Color.WHITE;
//        circleHaloColor = getResources().getColor(R.color.black);
        circleHaloColor = Color.parseColor("#4DFFFFFF");

        circleHaloInnerRadius = circleInnerRadius - 1 * density;
        circleHaloOuterRadius = circleOuterRadius + 1 * density;

        circleOuterAnimMaxRadius = circleOuterRadius + 2 * density;
        circleHaloInnerAnimMiniRadius = circleHaloInnerRadius - 2 * density;
        circleHaloOuterAnimMaxRadius = circleHaloOuterRadius + 2 * density;

        circleFinishDrawRadius = circleInnerRadius;

        animator = ObjectAnimator.ofFloat(0f, 1f);
        animator.setDuration(700);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatorValue = (float) animator.getAnimatedValue();
//                progress++;
                if (progress > 100) {
                    progress = 0;
                }
                postInvalidateOnAnimation();
            }
        });

        animatorFinish = ObjectAnimator.ofFloat(circleInnerRadius, (float) 500);
        animatorFinish.setDuration(300);
        animatorFinish.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleFinishDrawRadius = (float) animatorFinish.getAnimatedValue();
                postInvalidateOnAnimation();
            }
        });


    }

    /**
     * 设置发送进度
     * @param progress
     */
    public void setProgress(int progress) {
        this.progress = progress;
        if (progress >= 100) {
            startHaloFinishAnimator();
        }
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (circleBitmap != null && circleBitmap.isRecycled()) {
            circleBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            circleCanvas = new Canvas(circleBitmap);
        }
        if (circleHaloBitmap != null && circleHaloBitmap.isRecycled()) {
            circleHaloBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            circleHaloCanvas = new Canvas(circleHaloBitmap);
        }
        startHaloAnimator();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        if (progress < 100) {
            //背景色
            canvas.drawColor(circleBgColor);
            //绘制光晕
            if(circleHaloCanvas != null){
                circleHaloCanvas.save();
                circleHaloCanvas.translate((float) (getMeasuredWidth() / 2), (float)(getMeasuredHeight() / 2));
                mPaint.setXfermode(clearXF);
                circleHaloCanvas.drawPaint(mPaint);
                mPaint.setXfermode(null);
                //绘制光晕
                mPaint.setColor(circleHaloColor);
                circleHaloCanvas.drawCircle(0f, 0f, circleHaloOuterRadius + (circleHaloOuterAnimMaxRadius - circleHaloOuterRadius) * animatorValue, mPaint);
                mPaint.setXfermode(srcOutXF);
                circleHaloCanvas.drawCircle(0f, 0f, circleHaloInnerRadius - (circleHaloInnerRadius - circleHaloInnerAnimMiniRadius) * animatorValue, mPaint);
                mPaint.setXfermode(null);
                circleHaloCanvas.restore();
                canvas.drawBitmap(circleHaloBitmap, 0f, 0f, null);
            }
            //绘制圆
            if(circleCanvas != null){
                circleCanvas.save();
                circleCanvas.translate((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2));

                mPaint.setXfermode(clearXF);
                circleCanvas.drawPaint(mPaint);
                mPaint.setXfermode(null);
                //绘制圆圈
                mPaint.setColor(circleColor);
                circleCanvas.drawCircle(0f, 0f, circleOuterRadius + (circleOuterAnimMaxRadius - circleOuterRadius) * animatorValue, mPaint);

                mPaint.setXfermode(srcOutXF);
                circleCanvas.drawCircle(0f, 0f, circleInnerRadius, mPaint);
                mPaint.setXfermode(null);
                circleCanvas.restore();

                canvas.drawBitmap(circleBitmap, 0f, 0f, null);
            }
            if(progress > 0){
                //绘制显示进度的文字
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mPaint.setTextSize(12 * density);
                mPaint.setColor(circleColor);
                mPaint.setStrokeWidth(1f);
//                Rect rect = new Rect();
//                mPaint.getTextBounds("100%", 0, "100%".length(), rect);//确定文字的宽度
//                canvas.drawText(progress + "%", getMeasuredWidth() / 2 - rect.width() / 2, getMeasuredHeight() / 2 - rect.height() / 2, mPaint);
                String text = progress + "%";
                float textWidth = mPaint.measureText(text);
                float x = getWidth() / 2 - textWidth / 2;

                Paint.FontMetrics metrics = mPaint.getFontMetrics();
                //metrics.ascent为负数
                float dy = -(metrics.descent + metrics.ascent) / 2;
                float y = getHeight() / 2 + dy;
                canvas.drawText(text, x, y, mPaint);
            }

        }else if(animatorFinish.isRunning()){
            if(circleCanvas != null){
                mPaint.setXfermode(clearXF);
                circleCanvas.drawPaint(mPaint);
                mPaint.setXfermode(null);

                //绘制圆圈
                mPaint.setColor(circleBgColor);
                circleCanvas.drawRect(0f, 0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), mPaint);

                circleCanvas.save();
                circleCanvas.translate((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2));

                mPaint.setXfermode(srcOutXF);
                mPaint.setColor(Color.TRANSPARENT);
                circleCanvas.drawCircle(0f, 0f, circleFinishDrawRadius, mPaint);
                mPaint.setXfermode(null);

                circleCanvas.restore();

                canvas.drawBitmap(circleBitmap, 0f, 0f, null);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (circleBitmap != null) {
            circleBitmap.recycle();
        }
        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
            circleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            circleCanvas = new Canvas(circleBitmap);
        }

        if (circleHaloBitmap != null) {
            circleHaloBitmap.recycle();
        }
        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
            circleHaloBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            circleHaloCanvas = new Canvas(circleHaloBitmap);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopHaloAnimator();
        stopHaloFinishAnimator();
        if (circleHaloBitmap != null) {
            circleHaloBitmap.recycle();
        }
        if (circleBitmap != null) {
            circleBitmap.recycle();
        }
        progress = 0;
    }

    /**
     * 启动光晕动画
     */
    private void startHaloAnimator() {
        if (progress >= 100) {
            return;
        }
        if (animator.isStarted() || animator.isRunning()) {
        } else {
            animator.start();
        }
    }

    /**
     * 停止光晕动画
     */
    private void stopHaloAnimator() {
        animator.cancel();
    }

    /**
     * 进度100%后的动画
     */
    private void startHaloFinishAnimator() {
        stopHaloAnimator();
        if (animatorFinish.isStarted() || animatorFinish.isRunning()) {
        } else {
            animatorFinish.start();
        }
    }

    private void stopHaloFinishAnimator() {
        animatorFinish.cancel();
    }
}

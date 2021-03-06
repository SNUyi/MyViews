package com.sunny.myviews.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Kotlin 仿QQ图片发送进度view
 * Created by slp on 2018/4/26.
 */
class HaloProgressBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    init {
        //context.obtainStyledAttributes()
    }

    private var circleCanvas: Canvas? = null
    private var circleBitmap: Bitmap? = null
    private var circleHaloCanvas: Canvas? = null
    private var circleHaloBitmap: Bitmap? = null

    private var density = context.getResources().getDisplayMetrics().density

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private val clearXF by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val dstOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
    private val srcOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.SRC_OUT) }

    /**内圈半径*/
    var circleInnerRadius = 16 * density
    /**外圈半径*/
    var circleOuterRadius = 18 * density

    var circleBgColor = Color.parseColor("#70000000")

    private val circleHaloInnerRadius: Float
        get() {
            return circleInnerRadius - 1 * density
        }
    private val circleHaloOuterRadius: Float
        get() {
            return circleOuterRadius + 1 * density
        }

    /**圆圈颜色*/
    var circleColor = Color.WHITE
    /**光晕圆圈颜色*/
    var circleHaloColor = Color.GRAY

    /**动画时, 外圈允许的最大半径*/
    private val circleOuterAnimMaxRadius: Float
        get() {
            return circleOuterRadius + 2 * density
        }

    private val circleHaloInnerAnimMiniRadius: Float
        get() {
            return circleHaloInnerRadius - 4 * density
        }

    private val circleHaloOuterAnimMaxRadius: Float
        get() {
            return circleHaloOuterRadius + 4 * density
        }

    /**进度 0 - 100*/
    var progress = 0
        set(value) {
            field = value
            if (field >= 100) {
                startHaloFinishAnimator()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (progress < 100) {
            //背景色
            canvas.drawColor(circleBgColor)

            //绘制光晕
            circleHaloCanvas?.let {
                it.save()
                it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                paint.xfermode = clearXF
                it.drawPaint(paint)
                paint.xfermode = null

                //绘制光晕
                paint.color = circleHaloColor
                it.drawCircle(0f, 0f, circleHaloOuterRadius + (circleHaloOuterAnimMaxRadius - circleHaloOuterRadius) * animatorValue, paint)

                paint.xfermode = srcOutXF
                it.drawCircle(0f, 0f, circleHaloInnerRadius - (circleHaloInnerRadius - circleHaloInnerAnimMiniRadius) * animatorValue, paint)
                paint.xfermode = null

                it.restore()

                canvas.drawBitmap(circleHaloBitmap, 0f, 0f, null)
            }
            //绘制圆
            circleCanvas?.let {
                it.save()
                it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                paint.xfermode = clearXF
                it.drawPaint(paint)
                paint.xfermode = null

                //绘制圆圈
                paint.color = circleColor
                it.drawCircle(0f, 0f, circleOuterRadius + (circleOuterAnimMaxRadius - circleOuterRadius) * animatorValue, paint)

                paint.xfermode = srcOutXF
                it.drawCircle(0f, 0f, circleInnerRadius, paint)
                paint.xfermode = null

                it.restore()

                canvas.drawBitmap(circleBitmap, 0f, 0f, null)

            }

//            if (progress > 0) {
            //绘制进度文本
            paint.apply {
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.textSize = 12 * density
                paint.color = circleColor
                paint.strokeWidth = 1f
            }
            val text = "${progress}%"
            canvas.drawText("test",
                    (height / 2).toFloat(), (width / 2).toFloat(),
                    paint)
//            }
        } else if (animatorFinish.isRunning) {
            circleCanvas?.let {
                paint.xfermode = clearXF
                it.drawPaint(paint)
                paint.xfermode = null

                //绘制圆圈
                paint.color = circleBgColor
                it.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)

                it.save()
                it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                paint.xfermode = srcOutXF
                paint.color = Color.TRANSPARENT
                it.drawCircle(0f, 0f, circleFinishDrawRadius, paint)
                paint.xfermode = null

                it.restore()

                canvas.drawBitmap(circleBitmap, 0f, 0f, null)
            }
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap)
        }

        circleHaloBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleHaloBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (circleBitmap != null && circleBitmap!!.isRecycled) {
            circleBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap)
        }
        if (circleHaloBitmap != null && circleHaloBitmap!!.isRecycled) {
            circleHaloBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap)
        }
        startHaloAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopHaloAnimator()
        stopHaloFinishAnimator()
        circleHaloBitmap?.recycle()
        circleBitmap?.recycle()
        progress = 0
    }

    /*动画进度*/
    private var animatorValue: Float = 0f

    private val animator by lazy {
        ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                animatorValue = it.animatedValue as Float
                progress++
                if (progress > 100) {
                    progress = 0
                }
                postInvalidateOnAnimation()
            }
        }
    }

    /**启动光晕动画*/
    fun startHaloAnimator() {
        if (progress >= 100) {
            return
        }
        if (animator.isStarted || animator.isRunning) {
        } else {
            animator.start()
        }
    }

    fun stopHaloAnimator() {
        animator.cancel()
    }

    /**结束动画圆圈的半径*/
    private var circleFinishDrawRadius = circleInnerRadius

    private val animatorFinish by lazy {
        ObjectAnimator.ofFloat(circleInnerRadius, 500.toFloat()).apply {
            duration = 300
            addUpdateListener {
                circleFinishDrawRadius = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
        }
    }

    /**进度100%后的动画*/
    fun startHaloFinishAnimator() {
        stopHaloAnimator()
        if (animatorFinish.isStarted || animatorFinish.isRunning) {
        } else {
            animatorFinish.start()
        }
    }

    fun stopHaloFinishAnimator() {
        animatorFinish.cancel()
    }
}
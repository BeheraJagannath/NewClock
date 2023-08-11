package com.example.Stopwatch

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

import com.afollestad.aesthetic.Aesthetic
import io.reactivex.disposables.Disposable
import me.jfenn.androidutils.DimenUtils

/**
 * Display a progress circle with text in
 * the center.
 */
class ProgressTextView : View, Subscribblable {

    private var progressTextColor =resources.getColor(com.example.newclock.R.color.text_color)
    private var progressColor = resources.getColor(com.example.newclock.R.color.tab_indicator_color)
    private var circlePaintColor = resources.getColor(com.example.newclock.R.color.tab_background)


    private var progress: Long = 0
    private var maxProgress: Long = 0
    private var referenceProgress: Long = 0
    private var text: String? = null
    private var padding: Int = DimenUtils.dpToPx(10f)

    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = padding.toFloat()
        color = circlePaintColor
    }

    private var circlePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = progressColor
    }

    private var referenceCirclePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = progressColor
    }

    private var backgroundPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = progressColor

        strokeWidth = padding.toFloat()
    }

    private var textPaint: Paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color =progressTextColor
        textSize = DimenUtils.spToPx(40f).toFloat()
        strokeWidth = padding.toFloat()

        isFakeBoldText = true
    }

    private var colorAccentSubscription: Disposable? = null
    private var textColorPrimarySubscription: Disposable? = null
    private var textColorSecondarySubscription: Disposable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun subscribe() {
        colorAccentSubscription = Aesthetic.get()
            .colorAccent()
            .subscribe { integer ->
                linePaint.color = integer
                circlePaint.color = integer
                invalidate()
            }

        textColorPrimarySubscription = Aesthetic.get()
            .textColorPrimary()
            .subscribe { integer ->
                textPaint.color = integer
                referenceCirclePaint.color = integer
                invalidate()
            }

        textColorSecondarySubscription = Aesthetic.get()
            .textColorSecondary()
            .subscribe { integer ->
                backgroundPaint.color = integer
                backgroundPaint.alpha = 100
                invalidate()
            }
    }

    override fun unsubscribe() {
        colorAccentSubscription?.dispose()
        textColorPrimarySubscription?.dispose()
        textColorSecondarySubscription?.dispose()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            subscribe()

        }catch (e:Exception){

        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unsubscribe()
    }

    /**
     * Set the text (time) to display in the center
     * of the view.
     */
    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    /**
     * Set the current progress value.
     */
    @JvmOverloads
    fun setProgress(progress: Long, animate: Boolean = false) {
        if (animate) {
            ValueAnimator.ofFloat(this.progress.toFloat(), progress.toFloat()).apply {
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    (valueAnimator.animatedValue as? Float)?.toLong()?.let { value ->
                        setProgress(value, false)
                    }
                }
                start()
            }
        } else {
            this.progress = progress
            postInvalidate()
        }
    }

    /**
     * Set the largest progress that has been acquired so far.
     */
    @JvmOverloads
    fun setMaxProgress(maxProgress: Long, animate: Boolean = false) {
        if (animate) {
            ValueAnimator.ofFloat(this.maxProgress.toFloat(), maxProgress.toFloat()).apply {
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    (valueAnimator.animatedValue as? Float)?.toLong()?.let { value ->
                        setMaxProgress(value, false)
                    }
                }
                start()
            }
        } else {
            this.maxProgress = maxProgress
            postInvalidate()
        }
    }

    /**
     * Set the progress value of the reference dot (?) on
     * the circle. Mostly used in the stopwatch, to indicate
     * the previous/best lap time.
     */
    fun setReferenceProgress(referenceProgress: Long) {
        this.referenceProgress = referenceProgress
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val size = measuredWidth
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val size = Math.min(canvas.width, canvas.height)
        val sidePadding = padding * 3
        canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2 - sidePadding).toFloat(), if (maxProgress in 1..(progress - 1)) linePaint else backgroundPaint)

        if (maxProgress > 0) {
            val angle = 360f * progress / maxProgress
            val referenceAngle = 360f * referenceProgress / maxProgress

            val path = Path()
            path.arcTo(RectF(sidePadding.toFloat(), sidePadding.toFloat(), (size - sidePadding).toFloat(), (size - sidePadding).toFloat()), -90f, angle, true)
            canvas.drawPath(path, linePaint)

            canvas.drawCircle(size / 2 + Math.cos((angle - 90) * Math.PI / 180).toFloat() * (size / 2 - sidePadding), size / 2 + Math.sin((angle - 90) * Math.PI / 180).toFloat() * (size / 2 - sidePadding), (1 * padding).toFloat(), circlePaint)
            if (referenceProgress != 0L)
                canvas.drawCircle(size / 2 + Math.cos((referenceAngle - 90) * Math.PI / 180).toFloat() * (size / 2 - sidePadding), size / 2 + Math.sin((referenceAngle - 90) * Math.PI / 180).toFloat() * (size / 2 - sidePadding), (1 * padding).toFloat(), referenceCirclePaint)
        }

        text?.let { str ->
            canvas.drawText(str, (size / 2).toFloat(), size / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        }
    }

}
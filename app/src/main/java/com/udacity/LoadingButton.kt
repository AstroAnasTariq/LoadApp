package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var valueAnimator = ValueAnimator()
    private var buttonProgress: Float = 0f
    private var backgroundButtonColor = 0
    private var circleProgressColor = 0
    private var textButton = ""

    private var oval = RectF(80f, 30f, 200f, 150f)

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        buttonProgress = animatedValue as Float
                        invalidate()
                    }
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.RESTART
                    duration = Constants.DURATION_ANIMATION_BUTTON
                    start()
                }
                // Disable button
                isEnabled = false
            }

            ButtonState.Completed -> {
                // Cancel animator
                valueAnimator.cancel()
                // Update view
                invalidate()
                // Enable button
                isEnabled = true
                // Reset button state
                buttonState = ButtonState.Clicked
            }

            else -> {
                // Enable button
                isEnabled = true
                return@observable
            }
        }
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundButtonColor =
                getColor(R.styleable.LoadingButton_backgroundButtonColor, Color.GRAY)
            circleProgressColor =
                getColor(R.styleable.LoadingButton_circleProgressColor, Color.YELLOW)
            textButton = getString(R.styleable.LoadingButton_text).toString()
        }
        buttonState = ButtonState.Clicked
    }

    private val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.create("", Typeface.BOLD)
        textSize = 20.0f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundButtonColor
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = circleProgressColor
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawBackgroundButton(canvas)
            when (buttonState) {
                ButtonState.Loading -> {
                    drawBackgroundButton(canvas, buttonProgress)
                    drawArc(
                        oval,
                        -180f,
                        buttonProgress * 360,
                        true,
                        paintProgress
                    )

                    drawTextButton(canvas, resources.getString(R.string.button_loading))
                }

                ButtonState.Completed -> {
                    drawTextButton(canvas, resources.getString(R.string.button_download_completed))
                }

                else -> {
                    drawTextButton(canvas, textButton)
                }
            }
            invalidate()
        }
    }

    private fun drawBackgroundButton(canvas: Canvas?, progress: Float? = null) {
        canvas?.apply {
            if (progress != null) {
                paintBackground.alpha = 220
            }
            drawRect(0f, 0f, width.toFloat() * (progress ?: 1f), height.toFloat(), paintBackground)
        }
    }

    private fun drawTextButton(canvas: Canvas?, text: String) {
        canvas?.apply {
            val posTextX = (width / 2).toFloat()
            val posTextY = ((height - (paintText.descent() + paintText.ascent())) / 2)
            drawText(text, posTextX, posTextY, paintText)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}
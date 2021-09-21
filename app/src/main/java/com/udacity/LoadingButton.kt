package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.view.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var buttonText: String
    private var progress: Float = 0f
    private var buttonBgColor = R.attr.buttonBgColor
    private var widthSize = 0
    private var heightSize = 0
    private var valueAnimator = ValueAnimator()
    private val textRect = Rect()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Loading -> {
                setButtonText("Downloading")
                setButtonColor("#004349")
                valueAnimator= ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        progress = animatedValue as Float
                        invalidate()
                    }
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    duration = 3000
                    start()
                }
                disableCustomButton()
            }

            ButtonState.Completed -> {
                setButtonText("Download")
                setButtonColor("#07C2AA")
                valueAnimator.cancel()
                resetProgress()
                enableCustomButton()
            }

            ButtonState.Clicked -> {
            }
        }
        invalidate()
    }


    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0, 0).apply {

            try {
                buttonText = getString(R.styleable.LoadingButton_text).toString()
                buttonBgColor = ContextCompat.getColor(context, R.color.colorPrimary)
            } finally {
                recycle()
            }
        }
    }


    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val inProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    private val inProgressCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val cornerRadius = 10.0f
        val backgroundWidth = measuredWidth.toFloat()
        val backgroundHeight = measuredHeight.toFloat()

        canvas?.drawColor(buttonBgColor)
        textPaint.getTextBounds(buttonText, 0, buttonText.length, textRect)
        canvas?.drawRoundRect(0f, 0f, backgroundWidth, backgroundHeight, cornerRadius, cornerRadius, backgroundPaint)

        if (buttonState == ButtonState.Loading) {
            var progressVal = progress * measuredWidth.toFloat()
            canvas?.drawRoundRect(0f, 0f, progressVal, backgroundHeight, cornerRadius, cornerRadius, inProgressBackgroundPaint)

            val arcDiameter = cornerRadius * 2
            val arcRectSize = measuredHeight.toFloat() - paddingBottom.toFloat() - arcDiameter

            progressVal = progress * 360f
            canvas?.drawArc(paddingStart + arcDiameter,
                paddingTop.toFloat() + arcDiameter,
                arcRectSize,
                arcRectSize,
                0f,
                progressVal,
                true,
                inProgressCirclePaint)
        }
        val centerX = measuredWidth.toFloat() / 2
        val centerY = measuredHeight.toFloat() / 2 - textRect.centerY()

        canvas?.drawText(buttonText,centerX, centerY, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val width: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height: Int = resolveSizeAndState(
            MeasureSpec.getSize(width),
            heightMeasureSpec,
            0
        )
        widthSize = width
        heightSize = height
        setMeasuredDimension(width, height)
    }

    private fun resetProgress() {
        progress = 0f
    }

    private fun disableCustomButton() {
        custom_button.isEnabled = false
    }

    private fun enableCustomButton() {
        custom_button.isEnabled = true
    }

    fun setCustomButtonState(state: ButtonState) {
        buttonState = state
    }

    private fun setButtonText(buttonText: String) {
        this.buttonText = buttonText
        invalidate()
        requestLayout()
    }

    private fun setButtonColor(backgroundColor: String) {
        buttonBgColor = Color.parseColor(backgroundColor)
        invalidate()
        requestLayout()
    }
}
package com.atomgroups.progressbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max


class ProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    //UI
    private var labelPaint: TextPaint
    private var subtitlePaint: TextPaint
    private var progressBarPaint: Paint
    private var mainPaint: Paint
    private var progressIndicatorPaint: Paint
    private var diamondPaint: Paint

    //values
    private var labelMode: LabelMode
    private var isLabelEnabled: Boolean
    private var isSubLabelEnabled: Boolean
    private var dataList: List<ProgressObjectValue>? = null
    private var totalValue: Int? = null
    private var maxValue: Int = Int.MAX_VALUE
    var startDx: Float = 0f
    var gapBetweenItems = 0f
    private var widthLine: Int = ZERO_INT
    private var heightLine: Int = ZERO_INT

    private var lineStrokeWidth = 18f

    private var progress: Int = 0
    val path = Path()

    init {
        val typedArray: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.TierProgress, 0, 0)

        val mainColor: Int
        val progressColor: Int
        val indicatorColor: Int
        val diamondColor: Int
        val labelColor: Int
        val subLabelColor: Int

        val labelFontSize: Int
        val subLabelFontSize: Int

        try {

            labelMode = typedArray.getEnum(
                R.styleable.TierProgress_tier_progress_bar_label_mode,
                LabelMode.ALL
            )
            mainColor = typedArray.getColor(
                R.styleable.TierProgress_tier_progress_bar_main_color,
                Color.WHITE
            )
            progressColor =
                typedArray.getColor(R.styleable.TierProgress_tier_progress_bar_color, Color.WHITE)
            indicatorColor = typedArray.getColor(
                R.styleable.TierProgress_tier_progress_bar_indicator_color,
                Color.WHITE
            )
            diamondColor = typedArray.getColor(
                R.styleable.TierProgress_tier_progress_bar_diamond_step_color,
                Color.WHITE
            )
            labelColor = typedArray.getColor(
                R.styleable.TierProgress_tier_progress_bar_label_color,
                Color.WHITE
            )
            subLabelColor = typedArray.getColor(
                R.styleable.TierProgress_tier_progress_bar_sub_label_color,
                Color.WHITE
            )

            labelFontSize = typedArray.getInteger(
                R.styleable.TierProgress_tier_progress_bar_label_font_size,
                DEFAULT_FONT_SIZE
            )
            subLabelFontSize = typedArray.getInteger(
                R.styleable.TierProgress_tier_progress_bar_sub_label_font_size,
                DEFAULT_FONT_SIZE
            )

            isLabelEnabled = typedArray.getBoolean(
                R.styleable.TierProgress_tier_progress_bar_label_enabled,
                false
            )
            isSubLabelEnabled = typedArray.getBoolean(
                R.styleable.TierProgress_tier_progress_bar_sub_label_enabled,
                false
            )

            lineStrokeWidth =
                typedArray.getDimension(R.styleable.TierProgress_tier_progress_stroke_width, 12f)

            progress = typedArray.getInt(R.styleable.TierProgress_tier_progress, 0)

        } finally {
            typedArray.recycle()
        }

        labelPaint = TextPaint()
        subtitlePaint = TextPaint()

        labelPaint.apply {
            isAntiAlias = true
            textSize = labelFontSize * resources.displayMetrics.density
            color = labelColor
        }

        subtitlePaint.apply {
            isAntiAlias = true
            textSize = subLabelFontSize * resources.displayMetrics.density
            color = subLabelColor
        }


        mainPaint = Paint().apply {
            color = mainColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = lineStrokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        progressBarPaint = Paint().apply {
            color = progressColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = lineStrokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        progressIndicatorPaint = Paint().apply {
            color = indicatorColor
            style = Paint.Style.FILL_AND_STROKE
        }

        diamondPaint = Paint().apply {
            color = diamondColor
            style = Paint.Style.FILL_AND_STROKE
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        widthLine = measuredWidth
        heightLine = measuredHeight

        val dimensions = measureDimensions(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(dimensions.first, dimensions.second)

    }

    private fun measureDimensions(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        val requestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val requestedWidthMode = MeasureSpec.getMode(widthMeasureSpec)

        val requestedHeight = MeasureSpec.getSize(heightMeasureSpec)
        val requestedHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        val desiredWidth: Int = measuredWidth + paddingRight + paddingLeft
        val desiredHeight: Int = DESIRED_HEIGHT_MEASURE_DIMENSIONS + paddingTop + paddingBottom

        val width = when (requestedWidthMode) {
            MeasureSpec.EXACTLY -> requestedWidth
            MeasureSpec.AT_MOST -> desiredWidth
            else -> requestedWidth.coerceAtMost(desiredWidth)
        }

        val height = when (requestedHeightMode) {
            MeasureSpec.EXACTLY -> requestedHeight
            MeasureSpec.AT_MOST -> desiredHeight
            else -> requestedHeight.coerceAtMost(desiredHeight)
        }
        return Pair(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            drawMainLine(canvas)
            drawProgress(canvas)
            drawItems(it)
        }

    }

    private fun drawItems(canvas: Canvas) {
        if (!isLabelEnabled) return

        dataList?.forEachIndexed { index, tierValue ->
            drawLabels(canvas, tierValue, index)
        }

    }

    private fun drawLabels(canvas: Canvas, progressObjectValue: ProgressObjectValue, index: Int) {


        val firstElement = index == 0
        val lastElement = index == dataList?.size!! - 1
        val field = dataList?.get(index)
        gapBetweenItems = width * (field?.getValue()?.toFloat()!! / maxValue)
        startDx = if (firstElement) {
            0f
        } else {
            gapBetweenItems
        }


        val bounds = getTextBounds(labelPaint, progressObjectValue.getLabel())
        val subLabel = if (firstElement) "0 points" else progressObjectValue.getSubLabel()
        val boundsSub = getTextBounds(subtitlePaint, subLabel)

        if (lastElement) {
            startDx = width - max(bounds.width(), boundsSub.width()).toFloat()
        }



        when (labelMode) {
            LabelMode.NONE -> return
            LabelMode.ALL -> {}
            LabelMode.SIDES -> {
                if (!firstElement && !lastElement) return
            }

            LabelMode.START -> {
                if (!firstElement) return
            }

            LabelMode.END -> {
                if (!lastElement) return
            }

            LabelMode.MID -> {
                if (firstElement || lastElement) return
            }
        }

        if (!firstElement && !lastElement) {
            drawDiamondIndicator(canvas, startDx)
        }

        if (!firstElement && !lastElement) {
            startDx = gapBetweenItems - bounds.width() / 2.toFloat()
        }
        canvas.drawText(progressObjectValue.getLabel(), startDx, DISTANCE_Y_LABEL_FROM_BAR, labelPaint)
        if (isSubLabelEnabled) {
            canvas.drawText(
                subLabel,
                startDx,
                DISTANCE_Y_LABEL_FROM_BAR + bounds.height() + 10,
                subtitlePaint
            )
        }
    }

    private fun getTextBounds(paint: Paint, text: String): Rect {
        val bounds = Rect()

        paint.getTextBounds(text, 0, text.length, bounds)

        return bounds
    }

    private fun drawProgress(canvas: Canvas) {
        var cx = width * (progress.toFloat() / maxValue.toFloat())
        if (cx<20) {
           cx = 20f
        }
        canvas.drawLine(20f, 15f, cx, 15f, progressBarPaint)
        canvas.drawCircle(cx, 15f, 10f, progressIndicatorPaint)
    }

    private fun drawMainLine(canvas: Canvas) {
        canvas.drawLine(20f, 15f, width.toFloat() - 10, 15f, mainPaint)
    }

    private fun drawDiamondIndicator(canvas: Canvas, startingPoint: Float) {
        path.reset()
        path.moveTo(startingPoint - 7.5f, 15f)
        path.lineTo(startingPoint, 7.5f)
        path.lineTo(startingPoint + 7.5f, 15f)
        path.lineTo(startingPoint, 22.5f)
        path.lineTo(startingPoint - 7.5f, 15f)
        path.close()
        canvas.drawPath(path, diamondPaint)
    }


    fun setDataModelView(dataList: List<ProgressObjectValue>, totalValue: Int = 10) {
        maxValue = dataList.maxWithOrNull(Comparator.comparingInt { it.getValue() })?.getValue()
            ?: Int.MAX_VALUE

        this.dataList = dataList
        this.totalValue = totalValue
        invalidate()

    }


    fun setMainColor(color:Int){
        mainPaint.color = color
        invalidate()
    }

    fun setProgressBarColor(color:Int){
        progressBarPaint.color = color
        invalidate()
    }

    fun setProgressIndicatorColor(color:Int){
        progressIndicatorPaint.color = color
    }

    fun setDiamondColor(color:Int){
        diamondPaint.color = color
        invalidate()
    }

    fun setTitleColor(color:Int){
        labelPaint.color = color
        invalidate()
    }

    fun setSubtitleColor(color:Int){
        subtitlePaint.color = color
        invalidate()
    }

    fun setProgress(progressValue:Int){
        progress = progressValue
        invalidate()
    }

}


enum class LabelMode {
    NONE,
    ALL,
    SIDES,
    START,
    END,
    MID

}

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
    getInt(index, -1).let {
        if (it >= 0) enumValues<T>()[it] else default
    }
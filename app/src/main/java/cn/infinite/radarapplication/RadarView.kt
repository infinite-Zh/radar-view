package cn.infinite.radarapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import kotlin.math.*
import kotlin.random.Random

/**
 * @author kfzhangxu
 * Created on 2019/11/13.
 */
class RadarView : View {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initParams(context, attributeSet, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initParams(context, attributeSet, defStyleAttr)
    }

    companion object {
        const val SWEEP_COLOR = 0x9D00ff00.toInt()
        const val AXIS_WIDTH = 5
        const val AXIS_COLOR = Color.WHITE
        const val CIRCLE_COLOR = Color.WHITE
        const val CIRCLE_WIDTH = 5
        const val CIRCLE_COUNT = 2
        const val SPOT_COLOR = Color.WHITE
        const val SPOT_COUNT = 8
        const val SPOT_RADIUS = 12
        const val RADAR_BACKGROUND = Color.GRAY
    }

    private fun initParams(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RadarView, defStyleAttr, 0)
        sweepColor = ta.getColor(R.styleable.RadarView_sweepColor, SWEEP_COLOR)
        axisColor = ta.getColor(R.styleable.RadarView_axisColor, AXIS_COLOR)
        axisWidth = ta.getDimensionPixelSize(R.styleable.RadarView_axisWidth, AXIS_WIDTH)
        circleColor = ta.getColor(R.styleable.RadarView_circleColor, CIRCLE_COLOR)
        circleWidth = ta.getDimensionPixelSize(R.styleable.RadarView_circleWidth, CIRCLE_WIDTH)
        circleCount = ta.getInt(R.styleable.RadarView_circleCount, CIRCLE_COUNT)
        spotColor = ta.getColor(R.styleable.RadarView_spotColor, SPOT_COLOR)
        spotRadius = ta.getDimensionPixelSize(R.styleable.RadarView_spotRadius, SPOT_RADIUS)
        spotCount = ta.getInt(R.styleable.RadarView_spotCount, SPOT_COUNT)
        radarBackground = ta.getColor(R.styleable.RadarView_radarBackground, RADAR_BACKGROUND)

        ta.recycle()
    }

    @ColorRes
    var sweepColor = SWEEP_COLOR
    var axisColor = AXIS_COLOR
    var axisWidth = AXIS_WIDTH
    var circleColor = CIRCLE_COLOR
    var circleWidth = CIRCLE_WIDTH
    var circleCount = CIRCLE_COUNT
    var spotColor = SPOT_COLOR
    var spotRadius = SPOT_RADIUS
    var spotCount = SPOT_COUNT
    var radarBackground = RADAR_BACKGROUND


    private val mSpotPaint: Paint by lazy {
        Paint().apply {
            color = spotColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0f
        }
    }

    // 扫描区域的画笔
    private val mScanPaint: Paint by lazy {
        Paint().apply {
            shader = SweepGradient(
                mWidth / 2,
                mHeight / 2,
                sweepColor,
                Color.TRANSPARENT
            )
            color = sweepColor
        }
    }

    private val mBgPaint: Paint by lazy {
        Paint().apply {
            color = radarBackground
        }
    }

    private val mLinePaint: Paint by lazy {
        Paint().apply {
            color = axisColor
            strokeWidth = AXIS_WIDTH.toFloat()
        }
    }

    private val mOutlineCirclePaint: Paint by lazy {
        Paint().apply {
            color = circleColor
            strokeWidth = circleWidth.toFloat()
            style = Paint.Style.STROKE
        }
    }

    private val rotateMatrix = Matrix()

    private var mWidth = 0f
    private var mHeight = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(widthSize, heightSize)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = mWidth
        mRadius = mWidth / 2.toFloat()
        mRect = RectF()
        generateSpot(spotCount)
    }

    private var degree = 0
    private var mRadius = 0f
    private lateinit var mRect: RectF
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        rotateMatrix.preRotate(-1f, mRadius, mRadius)

        canvas?.apply {
            drawCircle(mRadius, mRadius, mRadius, mBgPaint)
            drawLine(0f, mHeight / 2, mWidth, mHeight / 2, mLinePaint)
            drawLine(mWidth / 2, 0f, mWidth / 2, mHeight, mLinePaint)


            val each = mRadius / (circleCount + 1)
            for (i in 1 until circleCount + 1) {
                val offset = each * i
                drawArc(mRect.apply {
                    left = mRadius - offset
                    top = mRadius - offset
                    right = mRadius + offset
                    bottom = mRadius + offset
                }, 0f, 360f, false, mOutlineCirclePaint)
            }
            drawSpot(this, degree)

            concat(rotateMatrix)
            drawCircle(mRadius, mRadius, mRadius, mScanPaint)

        }
        degree++
        if (degree == 360) {
            degree = 0
        }
        postInvalidateDelayed(0)
    }

    // 当扫描位置经过当前点时，绘制点
    private fun drawSpot(canvas: Canvas, degree: Int) {
        mSpots.forEach {
            val cx = it.cx
            val cy = it.cy
            /**
             * 由于是逆时针旋转，所以当degree在0-90时，扫描位置在第1象限，
             * 在90-180时，在第4象限
             * 在180-270时，在第3象限
             * 在270-360时，在第2象限
             * */
            when (degree) {
                in 0..90 -> {
                    if (tan(degree.getRadian()).compareTo(abs((cy - mRadius) / (cx - mRadius))) >= 0 && (cx > mRadius && cy < mRadius)) {
                        it.showed = true
                    }
                }
                in 91..180 -> {
                    if (tan((degree - 90).getRadian()).compareTo(abs((cx - mRadius) / (cy - mRadius))) >= 0 && (cx < mRadius && cy < mRadius)) {
                        it.showed = true
                    }

                }
                in 181..270 -> {
                    if (tan((degree - 180).getRadian()).compareTo(abs((cy - mRadius) / (cx - mRadius))) >= 0 && (cx < mRadius && cy > mRadius)) {
                        it.showed = true
                    }
                }
                else -> {
                    if (tan((degree - 270).getRadian()).compareTo(abs((cx - mRadius) / (cy - mRadius))) >= 0 && (cx > mRadius && cy > mRadius)) {
                        it.showed = true
                    }
                }

            }
            if (it.showed) {
                canvas.drawCircle(cx, cy, it.radius, mSpotPaint)
            }

        }
    }

    private fun Int.getRadian(): Double {

        return this * PI / 180
    }

    fun generateSpot(count: Int) {
        mSpots.clear()
        while (mSpots.size < count) {
            val cx = Random.nextInt(mWidth.toInt()).toFloat()
            val cy = Random.nextInt(mWidth.toInt()).toFloat()
            val dx = cx + spotRadius
            val dy = cy + spotRadius

            if ((dx - mRadius).pow(2) + (dy - mRadius).pow(2) < mRadius.pow(2)) {
                mSpots.add(Spot(cx, cy, spotRadius.toFloat(), false))
            }

        }
    }

    private val mSpots = mutableListOf<Spot>()

    data class Spot(var cx: Float, val cy: Float, val radius: Float, var showed: Boolean)
}
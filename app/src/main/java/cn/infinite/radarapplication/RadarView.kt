package cn.infinite.radarapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * @author kfzhangxu
 * Created on 2019/11/13.
 */
class RadarView : View {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {}
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
    }

    val mScanColor = 0x9D00ff00.toInt()

    private val mSpotPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    // 指针的画笔
    private var mPointerPaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 10f
    }
    // 扫描区域的画笔
    private val mScanPaint: Paint by lazy {
        Paint().apply {
            shader = SweepGradient(
                mWidth.toFloat() / 2,
                mHeight.toFloat() / 2,
                Color.GREEN,
                Color.TRANSPARENT
            )
            color = mScanColor
        }
    }

    private val mBgPaint: Paint by lazy {
        Paint().apply {
            color = Color.GRAY
        }
    }

    private val mLinePaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            strokeWidth = 5f
        }
    }

    private val mOutlineCirllePaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
    }

    private var mOutlineCircleNum = 2
    private val rotateMatrix = Matrix()

    private var mWidth = 0f
    private var mHeight = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mRadius = mWidth / 2.toFloat()
        mRect = RectF()
        generateSpot(5)

    }

    private var stopX: Float = 0f
    private var stopY: Float = 0f
    private var degree = 0
    private var mRadius = 0f
    private var angle = 0
    private lateinit var mRect: RectF
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        rotateMatrix.preRotate(-1f, mRadius, mRadius)

        canvas?.apply {
            drawCircle(mRadius, mRadius, mRadius, mBgPaint)
            drawLine(0f, mHeight / 2, mWidth, mHeight / 2, mLinePaint)
            drawLine(mWidth / 2, 0f, mWidth / 2, mHeight, mLinePaint)
            drawCircle(cx.toFloat(), cy.toFloat(), 5f, mSpotPaint)

            val each = mRadius / (mOutlineCircleNum + 1)
            for (i in 1 until mOutlineCircleNum + 1) {
                val offset = each * i
                drawArc(mRect.apply {
                    left = mRadius - offset
                    top = mRadius - offset
                    right = mRadius + offset
                    bottom = mRadius + offset
                }, 0f, 360f, false, mOutlineCirllePaint)
            }
            concat(rotateMatrix)
            drawCircle(mRadius, mRadius, mRadius, mScanPaint)

        }
        degree++
        postInvalidateDelayed(0)
    }

    private fun Int.getRadian(): Double {
        return this * PI / 180
    }

    private fun generateSpot(count: Int) {
        for (i in 0 until count) {
            val cx = Random.nextInt(mWidth.toInt()).toFloat()
            val cy = Random.nextInt(mWidth.toInt()).toFloat()
            mSpots.add(Spot(cx, cy, 5f))
        }
    }

    private val mSpots = mutableListOf<Spot>()

    data class Spot(var cx: Float, val cy: Float, val radius: Float)
}
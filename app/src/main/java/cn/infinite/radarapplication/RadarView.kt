package cn.infinite.radarapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.tan
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
    )

    private val mScanColor = 0x9D00ff00.toInt()

    private val mSpotPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    // 扫描区域的画笔
    private val mScanPaint: Paint by lazy {
        Paint().apply {
            shader = SweepGradient(
                mWidth / 2,
                mHeight / 2,
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
        generateSpot(20)

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

            drawSpot(this,degree)

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
        if (degree == 360) {
            degree = 0
        }
        postInvalidateDelayed(10)
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
                    if (tan(degree.getRadian()).compareTo(cx / cy) >= 0&&(cx>mRadius&&cy<mRadius)) {
                        it.showed=true
                    }
                }
                in 91..180 -> {
                    if (tan((degree - 270).getRadian()).compareTo(cy / cx) >= 0&&(cx<mRadius&&cy<mRadius)) {
                        it.showed=true
                    }

                }
                in 181..270 -> {
                    if (tan((degree - 180).getRadian()).compareTo(cy / cx) >= 0&&(cx<mRadius&&cy>mRadius)) {
                        it.showed=true
                    }
                }
                else -> {
                    if (tan((degree - 90).getRadian()).compareTo(cy / cx) >= 0&&(cx>mRadius&&cy>mRadius)) {
                        it.showed=true
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
        while (mSpots.size<count){
            val cx = Random.nextInt(mWidth.toInt()).toFloat()
            val cy = Random.nextInt(mWidth.toInt()).toFloat()
            if ((cx-mRadius).pow(2)+(cy-mRadius).pow(2)>mRadius.pow(2)){
                continue
            }
            mSpots.add(Spot(cx, cy, 12f, false))
        }
    }

    private val mSpots = mutableListOf<Spot>()

    data class Spot(var cx: Float, val cy: Float, val radius: Float, var showed: Boolean)
}
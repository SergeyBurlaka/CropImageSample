package com.b5eg.sergburlaka.cropimagetest

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView

class CustomCircleImageView : ImageView {

    constructor(context: Context) : this(context, null) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    companion object {
        val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        const val COLORDRAWABLE_DIMENSION = 2
        private val DEFAULT_BORDER_WIDTH = 0
    }

    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mCircleBackgroundPaint = Paint()

    private var mBitmap: Bitmap? = null

    private var mDrawableRadius: Float = 0.toFloat()
    private var mBorderRadius: Float = 0.toFloat()
    private var mReady: Boolean = false
    private var mSetupPending: Boolean = false

    private var mBitmapWidth: Int = 0
    private var mBitmapHeight: Int = 0
    private var mBitmapShader: BitmapShader? = null

    private var mColorFilter: ColorFilter? = null

    private var mBorderWidth = DEFAULT_BORDER_WIDTH


    private fun init() {
        mReady = true

        //на случай перповторного вызова
        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        initBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initBitmap()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        initBitmap()
    }

    private fun initBitmap() {
        mBitmap = getBitmapFromDrawable(drawable)
        setup()
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }

        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap: Bitmap = when (drawable) {

                is ColorDrawable -> Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)

                else -> Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }

            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }

        if (width == 0 && height == 0) {
            return
        }

        val bitmap = mBitmap
        if (bitmap == null) {
            invalidate()
            return
        }

        mBitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.shader = mBitmapShader

//        mBorderPaint.style = Paint.Style.STROKE
//        mBorderPaint.isAntiAlias = true

        mBitmap?.height?.let {
            mBitmapHeight = it
        }
        mBitmap?.width?.let {
            mBitmapWidth = it
        }

        mBorderRect.set(calculateBounds())
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2.0f, (mBorderRect.width() - mBorderWidth) / 2.0f)

        mDrawableRect.set(mBorderRect)
        mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f)

        applyColorFilter()
        updateShaderMatrix()

        invalidate()
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f

        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }

        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate((dx + 0.5f).toInt() + mDrawableRect.left, (dy + 0.5f).toInt() + mDrawableRect.top)

        mBitmapShader?.setLocalMatrix(mShaderMatrix)
    }

    override fun onDraw(canvas: Canvas) {
        if (mBitmap == null) {
            return
        }

        //paint circle with radius and center
        canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint)
    }

    override fun getColorFilter(): ColorFilter? {
        return mColorFilter
    }

    private fun applyColorFilter() {
        mBitmapPaint.colorFilter = mColorFilter
    }

    //Calculates bounds for image view drawing
    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = Math.min(availableWidth, availableHeight)

        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left, top, left + sideLength, top + sideLength)
    }


}
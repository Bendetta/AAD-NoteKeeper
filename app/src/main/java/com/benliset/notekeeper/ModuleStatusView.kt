package com.benliset.notekeeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

const val EDIT_MODE_MODULE_COUNT = 7

class ModuleStatusView : View {

    var moduleStatus = BooleanArray(0)
    private var outlineWidth by Delegates.notNull<Float>()
    private var shapeSize by Delegates.notNull<Float>()
    private var spacing by Delegates.notNull<Float>()
    private var radius by Delegates.notNull<Float>()
    private var outlineColor by Delegates.notNull<Int>()
    private var fillColor by Delegates.notNull<Int>()
    private var maxHorizontalModules: Int = 1
    private lateinit var paintOutline: Paint
    private lateinit var paintFill: Paint
    private lateinit var moduleRectangles: Array<Rect>

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode) {
            setupEditModeValues()
        }
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ModuleStatusView, defStyle, 0
        )

        a.recycle()

        outlineWidth = 6f
        shapeSize = 144f
        spacing = 30f
        radius = (shapeSize - outlineWidth) / 2

        outlineColor = Color.BLACK
        paintOutline = Paint(Paint.ANTI_ALIAS_FLAG)
        paintOutline.style = Paint.Style.STROKE
        paintOutline.strokeWidth = outlineWidth
        paintOutline.color = outlineColor

        fillColor = context.resources.getColor(R.color.pluralsight_orange)
        paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL
        paintFill.color = fillColor
    }

    private fun setupEditModeValues() {
        val exampleModuleValues = BooleanArray(EDIT_MODE_MODULE_COUNT)
        val middle = EDIT_MODE_MODULE_COUNT / 2
        for (i in 0 until middle) {
            exampleModuleValues[i] = true
        }

        moduleStatus = exampleModuleValues
    }

    private fun setupModuleRectangles(width: Int) {
        val availableWidth = width - paddingLeft - paddingRight
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        val maxHorizontalModules = horizontalModulesThatCanFit.coerceAtMost(moduleStatus.size)
        moduleRectangles = Array<Rect>(moduleStatus.size) {
            val column = it % maxHorizontalModules
            val row = it / maxHorizontalModules
            val x = paddingLeft + (column * (shapeSize + spacing)).toInt()
            val y = paddingTop + (row * (shapeSize + spacing)).toInt()
            Rect(x, y, (x + shapeSize).toInt(), (y + shapeSize).toInt())
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = specWidth - paddingLeft - paddingRight
        val horizontalModulesThatCanFit = (availableWidth / (shapeSize + spacing)).toInt()
        maxHorizontalModules = horizontalModulesThatCanFit.coerceAtMost(moduleStatus.size)
        val desiredWidth = ((moduleStatus.size * (shapeSize + spacing)) - spacing).toInt() +
                paddingLeft + paddingRight

        val rows = ((moduleStatus.size - 1) / maxHorizontalModules) + 1
        val desiredHeight = ((rows * (shapeSize + spacing)) - spacing
                + paddingTop + paddingBottom).toInt()

        val width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        setupModuleRectangles(w)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (moduleIndex in 0 until (moduleStatus.size)) {
            val x = moduleRectangles[moduleIndex].centerX().toFloat()
            val y = moduleRectangles[moduleIndex].centerY().toFloat()

            if (moduleStatus[moduleIndex]) {
                canvas.drawCircle(x, y, radius, paintFill)
            }

            canvas.drawCircle(x, y, radius, paintOutline)
        }
    }
}

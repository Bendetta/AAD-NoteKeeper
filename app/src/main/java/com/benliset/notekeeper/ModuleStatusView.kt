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

    var moduleStatus: BooleanArray? = null
    private var outlineWidth by Delegates.notNull<Float>()
    private var shapeSize by Delegates.notNull<Float>()
    private var spacing by Delegates.notNull<Float>()
    private var radius by Delegates.notNull<Float>()
    private var outlineColor by Delegates.notNull<Int>()
    private var fillColor by Delegates.notNull<Int>()
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
        setupModuleRectangles()

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

    private fun setupModuleRectangles() {
        moduleRectangles = Array<Rect>(moduleStatus?.size ?: 0) {
            val x = (it * (shapeSize + spacing)).toInt()
            val y = 0
            Rect(x, y, (x + shapeSize).toInt(), (y + shapeSize).toInt())
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (moduleIndex in 0 until (moduleStatus?.size ?: 0)) {
            val x = moduleRectangles[moduleIndex].centerX().toFloat()
            val y = moduleRectangles[moduleIndex].centerY().toFloat()

            if (moduleStatus?.get(moduleIndex) == true) {
                canvas.drawCircle(x, y, radius, paintFill)
            }

            canvas.drawCircle(x, y, radius, paintOutline)
        }
    }
}

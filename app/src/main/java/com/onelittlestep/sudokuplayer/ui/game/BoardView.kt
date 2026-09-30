package com.onelittlestep.sudokuplayer.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.onelittlestep.sudokuplayer.R
import kotlin.math.min

/**
 * Draws a 9x9 Sudoku board and reports cell taps. All 81 cells are rendered on a single
 * Canvas rather than as separate views, which keeps redraws cheap and guarantees perfectly
 * aligned grid lines regardless of screen density.
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onCellClick: ((Int) -> Unit)? = null

    private var board = IntArray(81)
    private var given = BooleanArray(81)
    private var notes: Array<Set<Int>> = Array(81) { emptySet() }
    private var selectedIndex = -1
    private var hintCells: Set<Int> = emptySet()
    private var errorCells: Set<Int> = emptySet()
    private var conflictCells: Set<Int> = emptySet()
    private var sameNumberCells: Set<Int> = emptySet()
    private var relatedCells: Set<Int> = emptySet()
    private var interactionEnabled = true

    private val boardBg = ContextCompat.getColor(context, R.color.board_background)
    private val colorSelected = ContextCompat.getColor(context, R.color.cell_selected_bg)
    private val colorRelated = ContextCompat.getColor(context, R.color.cell_related_bg)
    private val colorSameNumber = ContextCompat.getColor(context, R.color.cell_same_number_bg)
    private val colorError = ContextCompat.getColor(context, R.color.cell_error_bg)
    private val colorHint = ContextCompat.getColor(context, R.color.cell_hint_bg)
    private val colorGivenText = ContextCompat.getColor(context, R.color.cell_given_text)
    private val colorUserText = ContextCompat.getColor(context, R.color.cell_user_text)
    private val colorErrorText = ContextCompat.getColor(context, R.color.cell_error_text)
    private val colorHintText = ContextCompat.getColor(context, R.color.cell_hint_text)
    private val colorNotesText = ContextCompat.getColor(context, R.color.notes_text)
    private val colorThinLine = ContextCompat.getColor(context, R.color.grid_line_thin)
    private val colorThickLine = ContextCompat.getColor(context, R.color.grid_line_thick)
    private val colorConflict = ContextCompat.getColor(context, R.color.cell_conflict_border)

    private val cellBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thinLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorThinLine
        strokeWidth = resources.displayMetrics.density
    }
    private val thickLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorThickLine
        strokeWidth = resources.displayMetrics.density * 2.5f
    }
    private val conflictPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorConflict
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2f
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = colorNotesText
    }

    private var cellSize = 0f

    fun setInteractionEnabled(enabled: Boolean) {
        interactionEnabled = enabled
    }

    fun setState(
        board: IntArray,
        given: BooleanArray,
        notes: Array<Set<Int>>,
        selectedIndex: Int,
        hintCells: Set<Int>,
        errorCells: Set<Int>,
        conflictCells: Set<Int>,
        sameNumberCells: Set<Int>,
        relatedCells: Set<Int>
    ) {
        this.board = board
        this.given = given
        this.notes = notes
        this.selectedIndex = selectedIndex
        this.hintCells = hintCells
        this.errorCells = errorCells
        this.conflictCells = conflictCells
        this.sameNumberCells = sameNumberCells
        this.relatedCells = relatedCells
        contentDescription = describeSelection()
        invalidate()
    }

    private fun describeSelection(): String {
        if (selectedIndex !in 0 until 81) return resources.getString(R.string.app_name)
        val row = selectedIndex / 9 + 1
        val col = selectedIndex % 9 + 1
        val value = board[selectedIndex]
        val valueDesc = when {
            value == 0 -> resources.getString(R.string.content_desc_cell_empty)
            selectedIndex in hintCells -> resources.getString(R.string.content_desc_cell_hint, value)
            selectedIndex in errorCells -> resources.getString(R.string.content_desc_cell_error, value)
            given[selectedIndex] -> resources.getString(R.string.content_desc_cell_given, value)
            else -> resources.getString(R.string.content_desc_cell_user, value)
        }
        return resources.getString(R.string.content_desc_cell, row, col, valueDesc)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (height > 0) min(width, height) else width
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w / 9f
        numberPaint.textSize = cellSize * 0.55f
        notePaint.textSize = cellSize * 0.24f
    }

    override fun onDraw(canvas: Canvas) {
        if (cellSize <= 0f) return
        canvas.drawColor(boardBg)

        for (index in 0 until 81) {
            drawCellBackground(canvas, index)
        }
        drawGridLines(canvas)
        for (index in 0 until 81) {
            drawCellContent(canvas, index)
            if (index in conflictCells) drawConflictBorder(canvas, index)
        }
    }

    private fun rectFor(index: Int): RectF {
        val row = index / 9
        val col = index % 9
        val left = col * cellSize
        val top = row * cellSize
        return RectF(left, top, left + cellSize, top + cellSize)
    }

    private fun drawCellBackground(canvas: Canvas, index: Int) {
        val bg = when {
            index == selectedIndex -> colorSelected
            index in errorCells -> colorError
            index in hintCells -> colorHint
            index in sameNumberCells -> colorSameNumber
            index in relatedCells -> colorRelated
            else -> null
        }
        if (bg != null) {
            cellBgPaint.color = bg
            canvas.drawRect(rectFor(index), cellBgPaint)
        }
    }

    private fun drawConflictBorder(canvas: Canvas, index: Int) {
        val r = rectFor(index)
        val inset = conflictPaint.strokeWidth / 2f
        r.inset(inset, inset)
        canvas.drawRect(r, conflictPaint)
    }

    private fun drawGridLines(canvas: Canvas) {
        val total = cellSize * 9
        for (i in 0..9) {
            val paint = if (i % 3 == 0) thickLinePaint else thinLinePaint
            val pos = i * cellSize
            canvas.drawLine(pos, 0f, pos, total, paint)
            canvas.drawLine(0f, pos, total, pos, paint)
        }
    }

    private fun drawCellContent(canvas: Canvas, index: Int) {
        val value = board[index]
        val rect = rectFor(index)
        val centerX = rect.centerX()
        val centerY = rect.centerY()

        if (value != 0) {
            numberPaint.color = when {
                index in hintCells -> colorHintText
                index in errorCells -> colorErrorText
                given[index] -> colorGivenText
                else -> colorUserText
            }
            numberPaint.isFakeBoldText = given[index] || index in hintCells
            val fm = numberPaint.fontMetrics
            val baseline = centerY - (fm.ascent + fm.descent) / 2f
            canvas.drawText(value.toString(), centerX, baseline, numberPaint)
            return
        }

        val cellNotes = notes[index]
        if (cellNotes.isNotEmpty()) {
            val sub = cellSize / 3f
            val fm = notePaint.fontMetrics
            for (n in 1..9) {
                if (n !in cellNotes) continue
                val r = (n - 1) / 3
                val c = (n - 1) % 3
                val nx = rect.left + sub * c + sub / 2f
                val ny = rect.top + sub * r + sub / 2f - (fm.ascent + fm.descent) / 2f
                canvas.drawText(n.toString(), nx, ny, notePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!interactionEnabled) return false
        if (event.action == MotionEvent.ACTION_UP) {
            val col = (event.x / cellSize).toInt().coerceIn(0, 8)
            val row = (event.y / cellSize).toInt().coerceIn(0, 8)
            performClick()
            onCellClick?.invoke(row * 9 + col)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

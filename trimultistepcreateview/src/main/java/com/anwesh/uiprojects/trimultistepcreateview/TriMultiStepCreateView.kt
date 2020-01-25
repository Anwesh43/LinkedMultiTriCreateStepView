package com.anwesh.uiprojects.trimultistepcreateview

/**
 * Created by anweshmishra on 25/01/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val delay : Long = 20
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawTriStep(i : Int, scale : Float, w : Float, paint : Paint) {
    val gap : Float = w / (nodes + 1)
    val sf : Float = scale.sinify().divideScale(i, lines)
    save()
    translate(gap * (i + 1), 0f)
    for (j in 0..1) {
        val syi : Float = sf.divideScale(j, 2)
        if (syi == 0f) {
            break
        }
        val sj : Float = 1f - 2 * j
        drawLine(gap / 2 * i, gap / 2 * i, gap/ 2 * i + gap / 2 * syi, gap / 2 * i + (gap / 2) * syi * sj, paint)
    }
    restore()
}

fun Canvas.drawTriStepLines(scale : Float, w : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawTriStep(j, scale, w, paint)
    }
}

fun Canvas.drawTSLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val hGap : Float = h / (nodes + 1)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(0f, hGap * i)
    drawTriStepLines(scale, w, paint)
    restore()
}

class TriMultiStepCreateView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = 0f
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TSLNode(var i : Int, val state : State = State()) {

        private var next : TSLNode? = null
        private var prev : TSLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TSLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTSLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb :  () -> Unit) : TSLNode {
            var curr : TSLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb() // dir == 1 && the current node is last node or dir == -1 && current node is equal to last node
            return this
        }
    }

    data class TriMultiStep(var i : Int) {

        private var curr : TSLNode = TSLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }
}

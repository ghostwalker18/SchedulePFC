package com.ghostwalker18.schedulepfc

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Этот класс используется для реализации обработки смахивания вправо или влево.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
open class OnSwipeListener(context: Context?) : View.OnTouchListener {
    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    /**
     * Этот метод используется для обработки смахивания вверх.
     */
    open fun onSwipeTop() { /*To override*/ }

    /**
     * Этот метод используется для обработки смахивания вниз.
     */
    fun onSwipeBottom() { /*To override*/ }

    /**
     * Этот метод используется для обработки смахивания влево.
     */
    open fun onSwipeLeft() { /*To override*/ }

    /**
     * Этот метод используется для обработки смахивания вправо.
     */
    open fun onSwipeRight() { /*To override*/ }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent,
                             velocityX: Float, velocityY: Float): Boolean {
            val distanceX = e2.x - e1!!.x
            val distanceY = e2.y - e1.y
            if (abs(distanceX) > abs(distanceY)
                && abs(distanceX) > Companion.SWIPE_DISTANCE_THRESHOLD
                && abs(velocityX) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight()
                else
                    onSwipeLeft()
                return true
            }
            if (abs(distanceY) > abs(distanceX)
                && abs(distanceY) > Companion.SWIPE_DISTANCE_THRESHOLD
                && abs(velocityY) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                if (distanceY > 0)
                    onSwipeBottom()
                else
                    onSwipeTop()
                return true
            }
            return false
        }
    }
}
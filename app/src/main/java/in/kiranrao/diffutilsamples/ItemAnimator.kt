package `in`.kiranrao.diffutilsamples

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.mikhaellopez.circleview.CircleView

private const val DEFAULT_ANIMATION_DURATION = 300L
private val DEFAULT_INTERPOLATOR = AccelerateDecelerateInterpolator()
private const val TAG = "ItemAnimator"

fun createItemRemoveAnimator(circleView: CircleView): Animator? {
    return AnimatorSet().apply {
        playTogether(
            circleView.widthAnimator(startWidth = circleView.layoutParams.width, endWidth = 0),
            circleView.heightAnimator(startHeight = circleView.layoutParams.height, endHeight = 0),
            circleView.circleRadiusAnimator(
                startRadius = circleView.constraintLayoutParams.circleRadius,
                endRadius = 0
            )
        )
    }
}

fun createItemInsertAnimator(
    circleView: CircleView,
    targetSize: Int,
    circleRadius: Int
): Animator? {
    return AnimatorSet().apply {
        playTogether(
            circleView.widthAnimator(startWidth = 0, endWidth = targetSize),
            circleView.heightAnimator(startHeight = 0, endHeight = targetSize),
            circleView.circleRadiusAnimator(
                startRadius = 0,
                endRadius = circleRadius
            )
        )
    }
}

fun createItemChangeAnimator(circleView: CircleView, targetSize: Int): Animator? {
    return AnimatorSet().apply {
        playTogether(
            circleView.widthAnimator(
                startWidth = circleView.layoutParams.width,
                endWidth = targetSize
            ),
            circleView.heightAnimator(
                startHeight = circleView.layoutParams.height,
                endHeight = targetSize
            )
        )
    }
}

fun View.widthAnimator(startWidth: Int, endWidth: Int): Animator =
    ValueAnimator.ofInt(startWidth, endWidth).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DEFAULT_INTERPOLATOR
        addUpdateListener {
            this@widthAnimator.layoutParams.width = it.animatedValue as Int
            //this@widthAnimator.requestLayout()
        }
    }

fun View.heightAnimator(startHeight: Int, endHeight: Int): Animator =
    ValueAnimator.ofInt(startHeight, endHeight).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DEFAULT_INTERPOLATOR
        addUpdateListener {
            this@heightAnimator.layoutParams.height = it.animatedValue as Int
            //this@heightAnimator.requestLayout()
        }
    }

fun View.circleAngleAnimator(endAngle: Float): ValueAnimator? {
    val lp = this.layoutParams
    if (lp !is ConstraintLayout.LayoutParams) return null
    val currentColor = (this as? CircleView)?.humanReadableColor
    Log.d(
        TAG,
        "Animating view with id ${this.humanReadableId} and color $currentColor from ${lp.circleAngle} to $endAngle"
    )
    return ValueAnimator.ofFloat(lp.circleAngle, endAngle).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DEFAULT_INTERPOLATOR
        addUpdateListener {
            lp.circleAngle = it.animatedValue as Float
            this@circleAngleAnimator.layoutParams = lp
        }
    }
}

fun View.circleRadiusAnimator(startRadius: Int, endRadius: Int): ValueAnimator? {
    val lp = this.layoutParams
    if (lp !is ConstraintLayout.LayoutParams) return null
    return ValueAnimator.ofInt(startRadius, endRadius).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DEFAULT_INTERPOLATOR
        addUpdateListener {
            lp.circleRadius = it.animatedValue as Int
            this@circleRadiusAnimator.layoutParams = lp
        }
    }
}

val CircleView.constraintLayoutParams: ConstraintLayout.LayoutParams
    get() = this.layoutParams as ConstraintLayout.LayoutParams

val View.humanReadableId: String
    get() = this.resources.getResourceEntryName(this.id)

val CircleView.humanReadableColor: String
    get() = String.format("#%06X", (0xFFFFFF and this.circleColor))
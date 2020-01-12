package `in`.kiranrao.diffutilsamples

import `in`.kiranrao.atomicdiffutil.AtomicDiffResult
import `in`.kiranrao.atomicdiffutil.ItemDiffRecord.*
import `in`.kiranrao.atomicdiffutil.calculateAtomicDiff
import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import com.mikhaellopez.circleview.CircleView

data class CircleInfo(val color: Int, val expanded: Boolean = false)

class ColorCirclesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = this.javaClass.simpleName

    data class ColorCirclesModel(val colors: List<CircleInfo>)

    private var model: ColorCirclesModel? = null
    private var newList = emptyList<CircleInfo>()
    private var internalViews = emptyList<CircleView>()

    private val circleSize: Int
    private val circleSizeSelected: Int
    private val circleRadius: Int

    init {
        val originView = View(context).apply {
            id = R.id.circle_origin
        }
        addView(originView)
        val constraintSet = ConstraintSet().apply {
            clone(this@ColorCirclesView)
            addToHorizontalChain(originView.id, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID)
            constrainWidth(originView.id, 1)
            addToVerticalChain(originView.id, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID)
            constrainHeight(originView.id, 1)
        }
        constraintSet.applyTo(this)

        circleSize = resources.getDimensionPixelSize(R.dimen.circle_size)
        circleSizeSelected = resources.getDimensionPixelSize(R.dimen.circle_size_selected)
        circleRadius = resources.getDimensionPixelSize(R.dimen.circle_radius)
    }

    fun updateUi(model: ColorCirclesModel) {
        this.newList = model.colors
        if (this.model == null) {
            inflateViews(model)
        } else {
            applyDiff(model)
        }
        this.model = model
    }

    private fun inflateViews(model: ColorCirclesModel) {
        val constraintSet = ConstraintSet().apply { clone(this@ColorCirclesView) }
        model.colors.forEachIndexed { index, circleInfo ->
            inflateView(circleInfo, index, constraintSet)
        }
        constraintSet.applyTo(this)
        updateInternalViews()
    }

    private fun inflateView(
        circleInfo: CircleInfo,
        index: Int,
        constraintSet: ConstraintSet,
        preAnimation: Boolean = false
    ): Pair<CircleView, Int> {
        val circleView = CircleView(this.context).apply {
            circleColor = resources.getColor(circleInfo.color)
            id = circleInfo.viewId
        }
        val circleDimensions = if (circleInfo.expanded) circleSizeSelected else circleSize
        addView(circleView)
        constraintSet.constrainCircle(
            circleView.id,
            R.id.circle_origin,
            if(preAnimation) 0 else circleRadius,
            calculateAngle(index)
        )
        constraintSet.constrainHeight(circleView.id, if(preAnimation) 0 else circleDimensions)
        constraintSet.constrainWidth(circleView.id, if(preAnimation) 0 else circleDimensions)
        return circleView to circleDimensions
    }

    private fun applyDiff(model: ColorCirclesModel) {
        this.model?.let {
            val diffResult = calculateAtomicDiff(it.colors, model.colors,
                CircleInfoItemCallback()
            )
            if (BuildConfig.DEBUG) Log.d(TAG, "$diffResult")
            setupDiffAnimations(diffResult)
        }
    }

    private fun setupDiffAnimations(diffResult: AtomicDiffResult<CircleInfo>) {
        val pendingRemoves = mutableListOf<Animator?>()
        val pendingChanges = mutableListOf<Animator?>()
        val pendingInserts = mutableListOf<Animator?>()
        val pendingPositionChanges = mutableListOf<Animator?>()

        diffResult.removalRecords.forEach {
            pendingRemoves.add(setupPendingRemoveAnimations(it))
        }

        diffResult.changeRecords.forEach {
            pendingChanges.add(setupPendingChangeAnimations(it))
        }

        diffResult.insertionRecords.forEach {
            pendingInserts.add(setupPendingInsertAnimations(it))
        }

        diffResult.positionChangeRecords.forEach {
            pendingPositionChanges.add(setupPendingPositionChangeAnimations(it))
        }

        AnimatorSet().apply {
            playSequentially(
                AnimatorSet().apply { playTogether(pendingRemoves) },
                AnimatorSet().apply {
                    playTogether(pendingPositionChanges)
                    playTogether(pendingChanges)
                },
                AnimatorSet().apply { playTogether(pendingInserts) }
            )
            doOnEnd {
                updateInternalViews()
                printDebugViewInfo()
            }
        }.also {
            it.start()
        }

    }

    private fun setupPendingRemoveAnimations(removed: Removed<CircleInfo>): Animator? {
        val viewToBeRemoved = internalViews[removed.oldPosition]
        return createItemRemoveAnimator(
            viewToBeRemoved
        )?.apply {
            doOnEnd {
                removeFromHierarchy(viewToBeRemoved)
            }
        }
    }

    private fun setupPendingChangeAnimations(changed: Changed<CircleInfo>): Animator? {
        val viewToBeResized = internalViews[changed.oldPosition]
        val isExpanded = changed.newItem.expanded
        val targetSize = if (isExpanded) circleSizeSelected else circleSize
        return createItemChangeAnimator(
            viewToBeResized,
            targetSize
        )
    }

    private fun setupPendingInsertAnimations(inserted: Inserted<CircleInfo>): Animator? {
        val constraintSet = ConstraintSet().apply { clone(this@ColorCirclesView) }
        val viewToBeInserted = inflateView(inserted.item, inserted.newPosition, constraintSet, true)
        constraintSet.applyTo(this)
        return createItemInsertAnimator(
            viewToBeInserted.first,
            viewToBeInserted.second,
            circleRadius
        )
    }

    private fun setupPendingPositionChangeAnimations(positionChanged: PositionChanged<CircleInfo>): Animator? {
        return internalViews[positionChanged.oldPosition].circleAngleAnimator(
            calculateAngle(positionChanged.newPosition)
        )
    }

    private fun calculateAngle(index: Int): Float {
        return 360.toFloat() * index / 7
    }

    private fun removeFromHierarchy(viewToBeRemoved: View) {
        this.removeView(viewToBeRemoved)
    }

    private fun updateInternalViews() {
        internalViews = children.filterIsInstance(CircleView::class.java).toList().sortedBy { it.id }
    }

    private val colorsToViewIdMap = mapOf(
        R.color.purple to R.id.circle_1,
        R.color.indigo to R.id.circle_2,
        R.color.blue to R.id.circle_3,
        R.color.green to R.id.circle_4,
        R.color.yellow to R.id.circle_5,
        R.color.orange to R.id.circle_6,
        R.color.red to R.id.circle_7
    )

    private val CircleInfo.viewId: Int
        get() = colorsToViewIdMap.getValue(this.color)

    private fun printDebugViewInfo() {
        val debugString =
            children.filterIsInstance(CircleView::class.java).joinToString(separator = "\n") {
                it.debugToString()
            }
        Log.d(TAG, "LayoutChildren = \n$debugString")

        val internalViewsString = internalViews.joinToString(separator = "\n") {
            it.debugToString()
        }
        Log.d(TAG, "InternalViews = \n$internalViewsString")
    }

    private fun CircleView.debugToString(): String {
        val lp = this.layoutParams as? LayoutParams
        return "CircleView(id = ${this.humanReadableId}, color = ${this.humanReadableColor}, dimens = ${lp?.width}, circleAngle = ${lp?.circleAngle}, circleRadius = ${lp?.circleRadius})"
    }
}
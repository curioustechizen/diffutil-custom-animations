package `in`.kiranrao.atomicdiffutil

import `in`.kiranrao.atomicdiffutil.ItemDiffRecord.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult.NO_POSITION


class AtomicDiffResult<T>(
    val oldList: List<T>,
    val newList: List<T>,
    val rawDiffOperations: List<RawDiffOperation>,
    private val diffResult: DiffUtil.DiffResult
) {

    val insertionRecords: List<Inserted<T>> by lazy {
        computeInsertionRecords()
    }

    val removalRecords: List<Removed<T>> by lazy {
        computeRemovalRecords()
    }

    val changeRecords: List<Changed<T>> by lazy {
        computeChangeRecords()
    }

    private var computedPositionChanges = false

    private val _positionChangeRecords = mutableListOf<PositionChanged<T>>()
    val positionChangeRecords: List<PositionChanged<T>>
        get() {
            if(!computedPositionChanges) {
                computePositionChanges()
                computedPositionChanges = true
            }
            return _positionChangeRecords
        }

    val allChangeRecords: List<ItemDiffRecord<T>>
        get() = changeRecords + removalRecords + insertionRecords + positionChangeRecords

    fun convertNewPositionToOld(newListPosition: Int) =
        diffResult.convertNewPositionToOld(newListPosition)

    fun convertOldPositionToNew(oldListPosition: Int) =
        diffResult.convertOldPositionToNew(oldListPosition)

    private fun computeInsertionRecords(): List<Inserted<T>> {
        val mutableInsertionRecords = mutableListOf<Inserted<T>>()
        newList.forEachIndexed { index, item ->
            val oldPos = convertNewPositionToOld(index)
            if (oldPos == NO_POSITION) {
                mutableInsertionRecords.add(Inserted(item, index))
            }
            if(!computedPositionChanges) {
                if (oldPos != index && oldPos != NO_POSITION) {
                    _positionChangeRecords.add(PositionChanged(item, oldPos, index))
                }

            }
        }
        return mutableInsertionRecords
    }

    private fun computeRemovalRecords(): List<Removed<T>> {
        val mutableRemovalRecords = mutableListOf<Removed<T>>()
        oldList.forEachIndexed { index, item ->
            if (convertOldPositionToNew(index) == NO_POSITION) {
                mutableRemovalRecords.add(Removed(item, index))
            }
        }
        return mutableRemovalRecords
    }

    private fun computeChangeRecords(): List<Changed<T>> {
        val mutableChangeRecords = mutableListOf<Changed<T>>()
        rawDiffOperations.filterIsInstance(RawDiffOperation.Change::class.java).forEach {
            for (i in it.position until it.position + it.count) {
                val newPos = convertOldPositionToNew(i)
                mutableChangeRecords.add(
                    Changed(
                        oldList[i],
                        newList[newPos],
                        i,
                        newPos,
                        it.payload
                    )
                )
            }
        }
        return mutableChangeRecords
    }

    private fun computePositionChanges() {
        newList.forEachIndexed { index, item ->
            val oldPosition = diffResult.convertNewPositionToOld(index)
            if (oldPosition != index && oldPosition != NO_POSITION) {
                _positionChangeRecords.add(PositionChanged(item, oldPosition, index))
            }
        }

    }
}

sealed class RawDiffOperation {
    data class Insert(val position: Int, val count: Int) : RawDiffOperation()
    data class Change(val position: Int, val count: Int, val payload: Any?) : RawDiffOperation()
    data class Remove(val position: Int, val count: Int) : RawDiffOperation()
}

sealed class ItemDiffRecord<T> {
    data class Inserted<T>(val item: T, val newPosition: Int) : ItemDiffRecord<T>()
    data class Removed<T>(val item: T, val oldPosition: Int) : ItemDiffRecord<T>()
    data class Changed<T>(
        val oldItem: T,
        val newItem: T,
        val oldPosition: Int,
        val newPosition: Int,
        val payload: Any?
    ) : ItemDiffRecord<T>()

    data class PositionChanged<T>(val item: T, val oldPosition: Int, val newPosition: Int) :
        ItemDiffRecord<T>()
}


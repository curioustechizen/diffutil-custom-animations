package `in`.kiranrao.atomicdiffutil

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListUpdateCallback

fun <T> calculateAtomicDiff(oldList: List<T>, newList: List<T>, itemCallback: ItemCallback<T>): AtomicDiffResult<T> {
    val listAwareDiffCallback = ListAwareDiffCallback(oldList, newList, itemCallback)
    val diffResult = DiffUtil.calculateDiff(listAwareDiffCallback)
    val atomicListUpdateCallback = AtomicListUpdateCallback()
    diffResult.dispatchUpdatesTo(atomicListUpdateCallback)
    return AtomicDiffResult(
        oldList,
        newList,
        atomicListUpdateCallback.diffOps,
        diffResult
    )
}

internal class ListAwareDiffCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val itemCallback: ItemCallback<T>
) :
    DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemCallback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemCallback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return itemCallback.getChangePayload(oldList[oldItemPosition], newList[newItemPosition])
    }


}

internal class AtomicListUpdateCallback: ListUpdateCallback {
    internal val diffOps = mutableListOf<RawDiffOperation>()
    override fun onChanged(position: Int, count: Int, payload: Any?) {
        diffOps.add(RawDiffOperation.Change(position, count, payload))
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        //Do nothing.
    }

    override fun onInserted(position: Int, count: Int) {
        diffOps.add(RawDiffOperation.Insert(position, count))
    }

    override fun onRemoved(position: Int, count: Int) {
        diffOps.add(RawDiffOperation.Remove(position, count))
    }

}
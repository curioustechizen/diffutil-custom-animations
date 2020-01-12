package `in`.kiranrao.diffutilsamples

import androidx.recyclerview.widget.DiffUtil

class CircleInfoItemCallback : DiffUtil.ItemCallback<CircleInfo>() {
    override fun areItemsTheSame(oldItem: CircleInfo, newItem: CircleInfo) =
        oldItem.color == newItem.color

    override fun areContentsTheSame(oldItem: CircleInfo, newItem: CircleInfo) = oldItem == newItem

}
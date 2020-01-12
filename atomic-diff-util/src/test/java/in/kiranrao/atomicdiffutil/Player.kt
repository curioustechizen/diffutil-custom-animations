package `in`.kiranrao.atomicdiffutil

import androidx.recyclerview.widget.DiffUtil

data class Player(val name: String, val score: Int)

class PlayerItemCallback : DiffUtil.ItemCallback<Player>() {
    override fun areItemsTheSame(oldItem: Player, newItem: Player) =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Player, newItem: Player) = oldItem == newItem
}
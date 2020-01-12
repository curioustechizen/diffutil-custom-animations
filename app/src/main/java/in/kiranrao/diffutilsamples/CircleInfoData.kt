package `in`.kiranrao.diffutilsamples

import `in`.kiranrao.diffutilsamples.ColorCirclesView.ColorCirclesModel

object CircleInfoData {
    private val purple = CircleInfo(R.color.purple)
    private val indigo = CircleInfo(R.color.indigo)
    private val blue = CircleInfo(R.color.blue)
    private val green = CircleInfo(R.color.green, true)
    private val yellow = CircleInfo(R.color.yellow)
    private val orange = CircleInfo(R.color.orange)
    private val red = CircleInfo(R.color.red)

    val firstModel =
        ColorCirclesModel(
            listOf(
                //purple,
                indigo,
                blue,
                green,
                yellow,
                orange,
                red
            )
        )

    val secondModel =
        ColorCirclesModel(
            listOf(
                purple,
                indigo.copy(expanded = true),
                green.copy(expanded = false),
                //yellow,
                orange.copy(expanded = true),
                red
            )
        )
}
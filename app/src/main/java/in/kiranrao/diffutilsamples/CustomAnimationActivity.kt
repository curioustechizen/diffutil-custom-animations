package `in`.kiranrao.diffutilsamples

import `in`.kiranrao.diffutilsamples.CircleInfoData.firstModel
import `in`.kiranrao.diffutilsamples.CircleInfoData.secondModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.act_custom_view.*

class CustomAnimationActivity : AppCompatActivity() {
    private var showingFirstModel = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_custom_view)

        view_color_circles.updateUi(firstModel)
        btn_toggle_data.setOnClickListener {
            toggleCircleInfoData()
        }
    }

    private fun toggleCircleInfoData() {
        val modelToShow = if(showingFirstModel) secondModel else firstModel
        view_color_circles.updateUi(modelToShow)
        showingFirstModel = !showingFirstModel
    }
}
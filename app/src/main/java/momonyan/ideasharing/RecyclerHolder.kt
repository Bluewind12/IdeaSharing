package momonyan.ideasharing

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.debug_row_layout.view.*

class RecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleText: TextView = view.titleRowTextView
    val contentsText: TextView = view.contentsRowTextView
    val dateText: TextView = view.dateRowTextView
}
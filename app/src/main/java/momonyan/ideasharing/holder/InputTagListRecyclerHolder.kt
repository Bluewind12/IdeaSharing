package momonyan.ideasharing.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tag_row_layout.view.*

class InputTagListRecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tagText: TextView = view.tagRowTextView
}
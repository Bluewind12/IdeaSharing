package momonyan.ideasharing

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_row_layout.view.*

class RecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleText: TextView = view.titleRowTextView
    val dateText: TextView = view.dateRowTextView
    val postText: TextView = view.postNameTextView
    val likeText: TextView = view.likeCountTextView
    val disLikeText: TextView = view.disLiseCountTextView
}
package momonyan.ideasharing.holder

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_row_layout.view.*

class RecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleText: TextView = view.titleRowTextView
    val contentText: TextView = view.contentRowTextView
    val dateText: TextView = view.dateRowTextView
    val postText: TextView = view.postNameText
    val likeText: TextView = view.likeCountTextView
    val commentText: TextView = view.commentCountTextView
    val cardView: CardView = view.cardRowCardView
    val recycler: RecyclerView = view.cardTagRecyclerView
}
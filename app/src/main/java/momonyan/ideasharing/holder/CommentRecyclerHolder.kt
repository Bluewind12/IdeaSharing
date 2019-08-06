package momonyan.ideasharing.holder

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment_card_row_layout.view.*

class CommentRecyclerHolder(view: View) : RecyclerView.ViewHolder(view) {
    val commentText: TextView = view.commentContentText
    val postText: TextView = view.commentPostNameText
    val dateText: TextView = view.commentPostDateText
    val card: CardView = view.commentCardView

}
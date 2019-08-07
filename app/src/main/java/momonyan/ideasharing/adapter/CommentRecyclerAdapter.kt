package momonyan.ideasharing.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import momonyan.ideasharing.R
import momonyan.ideasharing.activity.DetailActivity
import momonyan.ideasharing.activity.ProfileDetailActivity
import momonyan.ideasharing.holder.CommentRecyclerHolder

class CommentRecyclerAdapter(
    private val context: Context,
    private val itemList: ArrayList<HashMap<String, Any>>,
    private val postData: String,
    private val activity: DetailActivity
) :
    RecyclerView.Adapter<CommentRecyclerHolder>() {
    override fun onBindViewHolder(holder: CommentRecyclerHolder, position: Int) {
        holder.let {
            it.commentText.text = (itemList[position])["Comment"].toString()
            it.dateText.text = (itemList[position])["Date"].toString()

            val db = FirebaseFirestore.getInstance()
            db.collection("ProfileData")
                .document((itemList[position])["UserId"].toString())
                .get()
                .addOnSuccessListener { profileResult ->
                    it.postText.text = profileResult["UserName"].toString()
                }
            it.card.setOnLongClickListener {
                val intent = Intent(context, ProfileDetailActivity::class.java)
                intent.putExtra("UserId", (itemList[position])["UserId"].toString())
                context.startActivity(intent)
                true
            }
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            var uid = "???"
            if (user != null) {
                uid = user.uid
            }
            it.card.setOnClickListener {
                if (uid == (itemList[position])["UserId"].toString() ) {
                    activity.onCreateCommentEditDialog(
                        (itemList[position])["Comment"].toString(),
                        postData,
                        (itemList[position])["DocumentId"].toString()
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRecyclerHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.comment_card_row_layout, parent, false)
        return CommentRecyclerHolder(mView)
    }
}

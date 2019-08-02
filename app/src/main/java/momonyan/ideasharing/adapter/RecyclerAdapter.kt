package momonyan.ideasharing.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import momonyan.ideasharing.R
import momonyan.ideasharing.activity.DetailActivity
import momonyan.ideasharing.holder.RecyclerHolder

class RecyclerAdapter(private val context: Context, private val itemList:ArrayList<HashMap<String, Any>>) :
    RecyclerView.Adapter<RecyclerHolder>() {
    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        holder.let {
            it.titleText.text = (itemList[position])["Title"].toString()
            it.dateText.text = (itemList[position])["Date"].toString()
            it.likeText.text = (itemList[position])["Like"].toString()
            it.disLikeText.text = (itemList[position])["DisLike"].toString()
            it.postText.text = (itemList[position])["UserNickName"].toString()

            it.recycler.adapter = InputTagListRecyclerAdapter(
                context,
                (itemList[position])["Tag"] as ArrayList<String>
            )
            it.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            it.cardView.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("DocumentId", (itemList[position])["DocumentId"].toString())
                Log.d("TAGTAG", (itemList[position])["DocumentId"].toString())
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.card_row_layout, parent, false)

        Log.d("DEBUGTAGA","NYAN")

        return RecyclerHolder(mView)
    }
}
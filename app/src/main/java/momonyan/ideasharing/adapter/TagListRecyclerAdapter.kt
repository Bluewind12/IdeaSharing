package momonyan.ideasharing.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import momonyan.ideasharing.R
import momonyan.ideasharing.activity.DetailSearchActivity
import momonyan.ideasharing.holder.TagListRecyclerHolder


class TagListRecyclerAdapter(private val context: Context, private val itemList: ArrayList<String>) :
    RecyclerView.Adapter<TagListRecyclerHolder>() {

    override fun onBindViewHolder(holder: TagListRecyclerHolder, position: Int) {
        holder.let {
            it.tagText.text = itemList[position]
            it.tagCard.setOnClickListener {
                val i = Intent(context, DetailSearchActivity::class.java)
                i.putExtra("Title", "")
                i.putExtra("Content", "")
                i.putExtra("Tag", itemList[position])
                i.putExtra("From", "-00:00:00")
                i.putExtra("Since", "-23:59:59")
                context.startActivity(i)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagListRecyclerHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.tag_row_layout, parent, false)

        return TagListRecyclerHolder(mView)
    }
}

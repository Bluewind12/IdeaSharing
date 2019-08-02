package momonyan.ideasharing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import momonyan.ideasharing.R
import momonyan.ideasharing.holder.TagListRecyclerHolder


class TagListRecyclerAdapter(private val context: Context, private val itemList: ArrayList<String>) :
    RecyclerView.Adapter<TagListRecyclerHolder>() {

    override fun onBindViewHolder(holder: TagListRecyclerHolder, position: Int) {
        holder.let {
            it.tagText.text = itemList[position]
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

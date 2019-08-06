package momonyan.ideasharing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import momonyan.ideasharing.R
import momonyan.ideasharing.activity.DetailActivity
import momonyan.ideasharing.activity.MainActivity
import momonyan.ideasharing.holder.TagListRecyclerHolder


class InputTagListRecyclerAdapter(
    private val context: Context,
    private val itemList: ArrayList<String>,
    private val activity: Any
) :
    RecyclerView.Adapter<TagListRecyclerHolder>() {

    override fun onBindViewHolder(holder: TagListRecyclerHolder, position: Int) {
        holder.let {
            it.tagText.text = itemList[position]
            it.tagCard.setOnClickListener {
                itemList.removeAt(position)
                if(activity is MainActivity) {
                    activity.setList(itemList)
                }else if(activity is DetailActivity){
                    activity.setList(itemList)
                }
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

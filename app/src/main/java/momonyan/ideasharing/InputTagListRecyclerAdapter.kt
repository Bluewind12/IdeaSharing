package momonyan.ideasharing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class InputTagListRecyclerAdapter(private val context: Context, private val itemList: ArrayList<String>) :
    RecyclerView.Adapter<InputTagListRecyclerHolder>() {
    override fun onBindViewHolder(holder: InputTagListRecyclerHolder, position: Int) {
        holder.let {
            it.tagText.text = itemList[position]
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputTagListRecyclerHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.tag_row_layout, parent, false)

        return InputTagListRecyclerHolder(mView)
    }
}

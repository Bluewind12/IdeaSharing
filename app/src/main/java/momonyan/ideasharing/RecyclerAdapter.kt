package momonyan.ideasharing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private val context: Context, private val itemList:ArrayList<HashMap<String, Any>>) :
    RecyclerView.Adapter<RecyclerHolder>() {
    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        holder.let {
            it.titleText.text = (itemList[position])["Title"].toString()
            it.contentsText.text = (itemList[position])["Content"].toString()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.debug_row_layout, parent, false)

        Log.d("DEBUGTAGA","NYAN")

        return RecyclerHolder(mView)
    }
}

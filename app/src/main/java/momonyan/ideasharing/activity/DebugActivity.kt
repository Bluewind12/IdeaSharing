package momonyan.ideasharing.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.debug_layout.*
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.RecyclerAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DebugActivity : AppCompatActivity() {
    private val tag = "DEBUG_TAAAAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)
        val db = FirebaseFirestore.getInstance()
        debugAddButton.setOnClickListener {
            val dbMap = HashMap<String, Any>()
            dbMap["Title"] = titleEditText.text.toString()
            dbMap["Contents"] = contentsEditText.text.toString()
            dbMap["Date"] = getToday()
            db.collection("Data")
                .add(dbMap)
                .addOnCompleteListener {
                    loadDatabase()
                }
        }
        loadDatabase()
    }

    private fun loadDatabase() {
        val db = FirebaseFirestore.getInstance()
        val item = ArrayList<HashMap<String, Any>>()
        db.collection("Data")
            .orderBy("Date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    item.add(document.data as HashMap<String, Any>)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(tag, "Error getting documents.", exception)
            }
            .addOnCompleteListener {
                val adapter = RecyclerAdapter(this, item)
                debugRecyclerView.adapter = adapter
                debugRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            }
    }

    private fun getToday(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }
}
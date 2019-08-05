package momonyan.ideasharing.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fav_list_layout.*
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.RecyclerAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class FavoriteListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fav_list_layout)

        //DB
        val db = FirebaseFirestore.getInstance()
        val item = ArrayList<HashMap<String, Any>>()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            db.collection("ProfileData")
                .document(user.uid)
                .get()
                .addOnSuccessListener { profileResult ->
                    val pResult = profileResult.data!!["Favorite"] as ArrayList<String>
                    db.collection("PostData")
                        .orderBy("Date", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                if (pResult.indexOf(document.id) != -1) {
                                    val documentMap = document.data as HashMap<String, Any>
                                    documentMap["DocumentId"] = document.id
                                    item.add(documentMap)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            val adapter = RecyclerAdapter(this, item)
                            favRecyclerView.adapter = adapter
                            favRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                        }
                }
        }
    }
}
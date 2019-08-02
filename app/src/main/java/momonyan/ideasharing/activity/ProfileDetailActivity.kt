package momonyan.ideasharing.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.profile_layout.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.RecyclerAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class ProfileDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)

        val userId = intent.getStringExtra("UserId")

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("ProfileData")
                .document(userId)
                .get()
                .addOnSuccessListener { profileResult ->
                    val profileMap = profileResult.data!!
                    //イメージ
                    profileImageView
                    profileNameTextView.text = profileMap["UserName"].toString()
                    profileCommentCountTextView.text = profileMap["Comment"].toString()
                    prodileUrlTextView

                    //アイコンの表示
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child(profileMap["UserId"].toString() + "ProfileImage")
                    GlideApp.with(this)
                        .load(imageRef)
                        .into(profileImageView)
                }
            val item = ArrayList<HashMap<String, Any>>()
            db.collection("PostData")
                .whereEqualTo("Contributor", userId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val documentMap = document.data as HashMap<String, Any>
                        documentMap["DocumentId"] = document.id
                        item.add(documentMap)
                    }
                    profilePostCountTextView.text = item.count().toString()

                }
                .addOnCompleteListener {
                    val adapter = RecyclerAdapter(this, item)
                    profileRecyclerView.adapter = adapter
                    profileRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                }

        }
    }
}
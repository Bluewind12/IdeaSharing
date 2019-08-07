package momonyan.ideasharing.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

        //ImageView
        val imageColor = resources.getColor(R.color.colorPrimary)
        profileWebImageView.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profileOtherImageView.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profilePostCountImage.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profileScoreCountImage.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profileCommentCountImage.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)

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
                    profileCommentTextView.text = profileMap["Comment"].toString()
                    prodileUrlTextView.text = profileMap["HP"].toString()
                    profileOtherTextView.text = profileMap["Other"].toString()
                    //アイコンの表示
                    val storageRef = FirebaseStorage.getInstance().reference
                    storageRef.child(profileMap["UserId"].toString() + "ProfileImage")
                        .downloadUrl.addOnSuccessListener {
                        GlideApp.with(this)
                            .load(it)
                            .into(profileImageView)
                    }
                }
            val item = ArrayList<HashMap<String, Any>>()
            db.collection("PostData")
                .whereEqualTo("Contributor", userId)
                .get()
                .addOnSuccessListener { result ->
                    var scoreSum = 0
                    var commentSum = 0
                    for (document in result) {
                        val documentMap = document.data as HashMap<String, Any>
                        documentMap["DocumentId"] = document.id
                        scoreSum += documentMap["Like"].toString().toInt()
                        commentSum += documentMap["CommentCount"].toString().toInt()
                        item.add(documentMap)
                    }
                    profilePostCountTextView.text = item.count().toString()
                    profileScoreCountTextView.text = "$scoreSum"
                    profileCommentCountTextView.text = "$commentSum"
                }
                .addOnCompleteListener {
                    val adapter = RecyclerAdapter(this, item)
                    profileRecyclerView.adapter = adapter
                    profileRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    profileProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
                }
            profileProgressBar.bringToFront()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.basic_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.basicBack -> {
                finish()
            }
        }
        return true
    }
}
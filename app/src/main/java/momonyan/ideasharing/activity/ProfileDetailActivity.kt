package momonyan.ideasharing.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
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

        //AD
        val adRequest = AdRequest.Builder().build()
            profileAdView.loadAd(adRequest)


        //ImageView
        val imageColor = resources.getColor(R.color.colorPrimary)
        profileUrlImageView.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profileTwitterImage.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        profileFacebookImage.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
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
                    profileNameTextView.text = profileMap["UserName"].toString()
                    profileCommentTextView.text = if (profileMap["Comment"] == null) {
                        ""
                    } else {
                        profileMap["Comment"].toString()
                    }
                    setContentStringAndImage(profileMap, "Twitter", profileTwitterTextView, profileTwitterImage)
                    setContentStringAndImage(profileMap, "Facebook", profileFacebookTextView, profileFacebookImage)
                    setContentStringAndImage(profileMap, "HP", profileUrlTextView, profileUrlImageView)
                    setContentStringAndImage(profileMap, "Other", profileOtherTextView, profileOtherImageView)
                    //アイコンの表示
                    val storageRef = FirebaseStorage.getInstance().reference
                    storageRef.child(profileMap["UserId"].toString() + "ProfileImage")
                        .downloadUrl.addOnSuccessListener {
                        GlideApp.with(applicationContext)
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

    override fun onStop() {
        super.onStop()
        GlideApp.with(applicationContext)
            .clear(profileImageView)
    }

    private fun setContentStringAndImage(
        map: Map<String, Any>,
        data: String,
        textView: TextView,
        imageView: ImageView
    ) {
        if (map[data] == null || map[data] == "") {
            textView.visibility = View.GONE
            imageView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            textView.text = map[data].toString()
        }
    }
}
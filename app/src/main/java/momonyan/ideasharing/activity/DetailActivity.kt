package momonyan.ideasharing.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.detail_layout.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.CommentRecyclerAdapter
import momonyan.ideasharing.adapter.TagListRecyclerAdapter
import momonyan.ideasharing.getToday


class DetailActivity : AppCompatActivity() {

    private var favStar = false
    private lateinit var db: FirebaseFirestore
    private lateinit var documentId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        documentId = intent.getStringExtra("DocumentId")
        db = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        var uid = "???"
        var likeCount: Int
        if (user != null) {
            uid = user.uid
        }

        db.collection("PostData")
            .document(documentId)
            .get()
            .addOnSuccessListener { result ->
                val dataMap = result.data!!
                detailTitleTextView.text = dataMap["Title"].toString()
                detailContentTextView.text = dataMap["Content"].toString()
                likeCount = dataMap["Like"].toString().toInt() + 1
                detailLikeCountTextView.text = dataMap["Like"].toString()

                //Tag
                detailTagRecyclerView.adapter =
                    TagListRecyclerAdapter(
                        this,
                        dataMap["Tag"] as ArrayList<String>
                    )
                detailTagRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                detailPostCardView.setOnClickListener {
                    //プロフィール詳細画面への遷移
                    val i = Intent(this, ProfileDetailActivity::class.java)
                    i.putExtra("UserId", dataMap["Contributor"].toString())
                    startActivity(i)
                }
                if (uid != dataMap["Contributor"].toString()) {
                    detailLikeCard.setOnClickListener {
                        db.collection("PostData").document(documentId)
                            .update(
                                mapOf("Like" to likeCount)
                            )
                            .addOnSuccessListener {
                                detailLikeCard.setOnClickListener { }
                                detailLikeCountTextView.text = likeCount.toString()
                                Toast.makeText(this, "いいねしました", Toast.LENGTH_LONG).show()
                            }
                    }
                }


                db.collection("ProfileData")
                    .document(dataMap["Contributor"].toString())
                    .get()
                    .addOnSuccessListener { profileResult ->
                        val profileMap = profileResult.data!!
                        detailPostNameTextView.text = profileMap["UserName"].toString()
                        //プロフィールイメージ画像の追加
                        val storageRef = FirebaseStorage.getInstance().reference
                        storageRef.child(dataMap["Contributor"].toString() + "ProfileImage")
                            .downloadUrl.addOnSuccessListener {
                            GlideApp.with(this)
                                .load(it)
                                .into(detailPostImageView)
                        }
                    }
            }
        setCommentList()

        //コメント投稿
        detailCommentAddButton.setOnClickListener {
            val comment = detailCommentEditText.text.toString()
            if (comment != "") {
                AlertDialog.Builder(this)
                    .setTitle("投稿確認")
                    .setMessage("[$comment]で投稿してよろしいでしょうか")
                    .setPositiveButton("OK") { _, _ ->
                        val map = HashMap<String, Any>()
                        map["Comment"] = comment
                        map["UserId"] = uid
                        map["Date"] = getToday()
                        db.collection("PostData/$documentId/Comment")
                            .add(map)
                            .addOnCompleteListener {
                                Toast.makeText(this, "コメント投稿しました", Toast.LENGTH_LONG).show()
                            }
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()
            }
        }

        var favList: ArrayList<String> = arrayListOf()

        db.collection("ProfileData")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                val data = result.data!!
                if (data["Favorite"] != null) {
                    favList = data["Favorite"] as ArrayList<String>
                }
                if (favList.contains(documentId)) {
                    detailFavView.setImageResource(R.drawable.icon_color_star)
                    favStar = true
                } else {
                    detailFavView.setImageResource(R.drawable.icon_mono_star)
                    favStar = false
                }
            }

        detailFavView.setOnClickListener {
            if (favStar) {
                favList.remove(documentId)
                db.collection("ProfileData").document(uid)
                    .update(
                        mapOf(
                            "Favorite" to favList
                        )
                    )
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り解除しました", Toast.LENGTH_LONG).show() }
                detailFavView.setImageResource(R.drawable.icon_mono_star)
                favStar = false
            } else {
                favList.add(documentId)
                db.collection("ProfileData").document(uid)
                    .update(
                        mapOf(
                            "Favorite" to favList
                        )
                    )
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り登録しました", Toast.LENGTH_LONG).show() }
                detailFavView.setImageResource(R.drawable.icon_color_star)
                favStar = true
            }
        }

    }

    private fun setCommentList() {
        db.collection("PostData/$documentId/Comment")
            .orderBy("Date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val item = ArrayList<java.util.HashMap<String, Any>>()
                for (document in result) {
                    val documentMap = document.data as java.util.HashMap<String, Any>
                    Log.d("TestTags", documentMap["Comment"].toString())
                    item.add(documentMap)
                }
                detailCommentRecyclerView.adapter =
                    CommentRecyclerAdapter(this, item)
                detailCommentRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            }
    }
}
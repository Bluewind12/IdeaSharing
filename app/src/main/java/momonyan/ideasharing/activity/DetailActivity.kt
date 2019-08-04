package momonyan.ideasharing.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.detail_layout.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.TagListRecyclerAdapter

class DetailActivity : AppCompatActivity() {

    private var favStar = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        val documentId = intent.getStringExtra("DocumentId")
        val db = FirebaseFirestore.getInstance()
        if (documentId != null) {
            db.collection("PostData")
                .document(documentId)
                .get()
                .addOnSuccessListener { result ->
                    val dataMap = result.data!!
                    detailTitleTextView.text = dataMap["Title"].toString()
                    detailContentTextView.text = dataMap["Content"].toString()
                    detailLikeCountTextView.text = dataMap["Like"].toString()
                    detailDisLikeCountTextView.text = dataMap["DisLike"].toString()

                    //Tag
                    detailTagRecyclerView.adapter =
                        TagListRecyclerAdapter(
                            this,
                            dataMap["Tag"] as ArrayList<String>
                        )
                    detailTagRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                    //TODO コメントリサイクラーの追加

                    detailPostCardView.setOnClickListener {
                        //プロフィール詳細画面への遷移
                        val i = Intent(this, ProfileDetailActivity::class.java)
                        i.putExtra("UserId", dataMap["Contributor"].toString())
                        startActivity(i)
                    }

                    db.collection("ProfileData")
                        .document(dataMap["Contributor"].toString())
                        .get()
                        .addOnSuccessListener { profileResult ->
                            val profileMap = profileResult.data!!
                            detailPostNameTextView.text = profileMap["UserName"].toString()
                            //プロフィールイメージ画像の追加
                            val storageRef = FirebaseStorage.getInstance().reference
                            val imageRef = storageRef.child(dataMap["Contributor"].toString() + "ProfileImage")
                            GlideApp.with(this)
                                .load(imageRef)
                                .into(detailPostImageView)
                        }
                }

        }
        detailCommentAddButton.setOnClickListener {
            //TODO コメントの追加
        }


        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        var uid = "???"
        if (user != null) {
            uid = user.uid
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
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り登録しました", Toast.LENGTH_LONG).show() }
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
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り解除しました", Toast.LENGTH_LONG).show() }
                detailFavView.setImageResource(R.drawable.icon_color_star)
                favStar = true
            }
        }
    }
}
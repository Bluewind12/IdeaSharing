package momonyan.ideasharing.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.detail_layout.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.TagListRecyclerAdapter

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        val documentId = intent.getStringExtra("DocumentId")
        if (documentId != null) {
            val db = FirebaseFirestore.getInstance()
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
    }
}
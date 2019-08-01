package momonyan.ideasharing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.detail_layout.*

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        val documentId = intent.getStringExtra("DocumentId")

        if (documentId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Data")
                .document(documentId)
                .get()
                .addOnSuccessListener { result ->
                    val dataMap = result.data!!
                    detailTitleTextView.text = dataMap["Title"].toString()
                    detailContentTextView.text = dataMap["Content"].toString()
                    //detailLikeCountTextView.text = dataMap["Like"].toString()
                    //detailDisLikeCountTextView.text = dataMap["DisLike"].toString()
                    //TODO Tagリサイクラーの追加
                    //TODO コメントリサイクラーの追加


                    db.collection("ProfileData")
                        .document(dataMap["Contributor"].toString())
                        .get()
                        .addOnSuccessListener { profileResult ->
                            val profileMap = profileResult.data!!
                            detailPostNameTextView.text = profileMap["UserName"].toString()
                            //TODO プロフィールイメージ画像の追加

                            //プロフィール詳細画面への遷移
                            val i = Intent(this, ProfileDetailActivity::class.java)
                            i.putExtra("UserId", dataMap["Contributor"].toString())
                            startActivity(i)

                        }
                }

        }
        detailCommentAddButton.setOnClickListener {
            //TODO コメントの追加
        }
    }
}
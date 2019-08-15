package momonyan.ideasharing.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.comment_edit_layout.view.*
import kotlinx.android.synthetic.main.detail_layout.*
import kotlinx.android.synthetic.main.input_layout.view.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.CommentRecyclerAdapter
import momonyan.ideasharing.adapter.InputTagListRecyclerAdapter
import momonyan.ideasharing.adapter.TagListRecyclerAdapter
import momonyan.ideasharing.getToday
import kotlin.random.Random


class DetailActivity : AppCompatActivity() {

    private var favStar = false
    private lateinit var db: FirebaseFirestore
    private lateinit var documentId: String
    private lateinit var editMenu: MenuItem
    private lateinit var trashMenu: MenuItem
    private lateinit var shareMenu: MenuItem

    private var uid = "???"
    private var likeCount = 0
    private var likeCountPlus = 0
    private var commentCount = 0
    private var content = ""
    private var title = ""
    private var tagArrayList = ArrayList<String>()

    private lateinit var recyclerList: ArrayList<String>
    private lateinit var tagRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        //AD
        val adRequest = AdRequest.Builder().build()
        detailAdView.loadAd(adRequest)
        val mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_in_comment)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        documentId = intent.getStringExtra("DocumentId")
        db = FirebaseFirestore.getInstance()

        loadData()
        setCommentList()
        //コメント投稿
        detailCommentAddButton.setOnClickListener {
            val comment = detailCommentEditText.text.toString().trim()
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
                            .addOnSuccessListener { commentCount++ }
                            .addOnCompleteListener {
                                db.collection("PostData").document(documentId)
                                    .update("CommentCount" , commentCount)
                                    .addOnCompleteListener {
                                        Toast.makeText(this, "コメント投稿しました", Toast.LENGTH_LONG).show()
                                        detailCommentEditText.setText("", TextView.BufferType.NORMAL)
                                        //広告表示
                                        if (mInterstitialAd.isLoaded && Random.nextInt(100) >= 70) {
                                            mInterstitialAd.show()
                                        } else {
                                            Log.d("TAG", "The interstitial wasn't loaded yet.")
                                        }
                                        setCommentList()
                                    }
                            }
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()
            } else {
                Toast.makeText(this, "コメントが書かれていません", Toast.LENGTH_LONG).show()
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
                    .update("Favorite", favList)
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り解除しました", Toast.LENGTH_LONG).show() }
                detailFavView.setImageResource(R.drawable.icon_mono_star)
                favStar = false
            } else {
                favList.add(documentId)
                db.collection("ProfileData").document(uid)
                    .update("Favorite", favList)
                    .addOnSuccessListener { Toast.makeText(this, "お気に入り登録しました", Toast.LENGTH_LONG).show() }
                detailFavView.setImageResource(R.drawable.icon_color_star)
                favStar = true
            }
        }

    }

    private fun loadData() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
        }

        db.collection("PostData")
            .document(documentId)
            .get()
            .addOnSuccessListener { result ->
                val dataMap = result.data!!
                title = dataMap["Title"].toString()
                content = dataMap["Content"].toString()
                tagArrayList = ArrayList(dataMap["Tag"] as ArrayList<String>)
                recyclerList = ArrayList(dataMap["Tag"] as ArrayList<String>)


                detailTitleTextView.text = dataMap["Title"].toString()
                detailContentTextView.text = dataMap["Content"].toString()
                likeCount = dataMap["Like"].toString().toInt()
                likeCountPlus = dataMap["Like"].toString().toInt() + 1
                commentCount = dataMap["CommentCount"].toString().toInt()
                detailLikeCountTextView.text = dataMap["Like"].toString()

                //Tag
                detailTagRecyclerView.adapter = TagListRecyclerAdapter(this, tagArrayList)
                detailTagRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                detailPostCardView.setOnClickListener {
                    //プロフィール詳細画面への遷移
                    val i = Intent(this, ProfileDetailActivity::class.java)
                    i.putExtra("UserId", dataMap["Contributor"].toString())
                    startActivity(i)
                }
                if (uid == dataMap["Contributor"].toString()) {
                    editMenu.isVisible = true
                    trashMenu.isVisible = true
                } else {
                    detailLikeCard.setOnClickListener {
                        setLikeButtonListener(true)
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
                            GlideApp.with(applicationContext)
                                .load(it)
                                .into(detailPostImageView)
                        }
                    }
                    .addOnCompleteListener {
                        detailProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
                    }
                detailProgressBar.bringToFront()
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
                    documentMap["DocumentId"] = document.id
                    item.add(documentMap)
                }
                detailCommentRecyclerView.adapter =
                    CommentRecyclerAdapter(this, item, documentId, this@DetailActivity)
                detailCommentRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            }
    }

    fun onCreateCommentEditDialog(comment: String, path: String, documentId: String) {
        //DB
        val db = FirebaseFirestore.getInstance()
        val view = layoutInflater.inflate(R.layout.comment_edit_layout, null)
        view.commentReEditText.setText(comment, TextView.BufferType.NORMAL)
        AlertDialog.Builder(this)
            .setView(view)
            .setTitle("コメント編集")
            .setPositiveButton("更新") { _, _ ->
                val putComment = view.commentReEditText.text.toString().trim()
                if (putComment == "") {
                    Toast.makeText(this, "コメントが入力されていません", Toast.LENGTH_LONG).show()
                } else {
                    db.collection("PostData/$path/Comment")
                        .document(documentId)
                        .update("Comment", putComment)
                        .addOnCompleteListener {
                            setCommentList()
                        }
                }
            }
            .setNeutralButton("削除") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("削除確認")
                    .setMessage("削除してもよろしいでしょうか")
                    .setPositiveButton("削除") { _, _ ->
                        commentCount--
                        detailProgressBar.visibility = android.widget.ProgressBar.VISIBLE
                        db.collection("PostData/$path/Comment").document(documentId)
                            .delete()
                            .addOnCompleteListener {
                                db.collection("PostData").document(this.documentId)
                                    .update("CommentCount", commentCount)
                                    .addOnCompleteListener {
                                        Toast.makeText(this, "削除しました", Toast.LENGTH_LONG).show()
                                        setCommentList()
                                        detailProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "エラー", Toast.LENGTH_LONG).show()
                                Log.e("Error", "Document", e)
                            }
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()

            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.detail_menu, menu)
        editMenu = menu.findItem(R.id.detailMenuEdit)
        trashMenu = menu.findItem(R.id.detailMenuTrash)
        shareMenu = menu.findItem(R.id.detailShare)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.detailMenuEdit -> {
                createInputEditDialog()
            }
            R.id.detailMenuTrash -> {
                createTrashMenu()
            }
            R.id.detailShare -> {
                val articleURL = "https://play.google.com/store/apps/details?id=momonyan.ideasharing"
                val articleTitle = ""
                val sharedText = "[$title]\n$content\n$articleURL"

                // builderの生成　ShareCompat.IntentBuilder.from(Context context);
                val builder = ShareCompat.IntentBuilder.from(this@DetailActivity)

                // シェアするタイトル
                builder.setSubject(articleTitle)

                // シェアするテキスト
                builder.setText(sharedText)

                // シェアするタイプ（他にもいっぱいあるよ）
                builder.setType("text/plain")

                // Shareアプリ一覧のDialogの表示
                builder.startChooser()
            }
            R.id.detailMenuBack -> {
                finish()
            }
        }
        return true
    }

    //削除
    private fun createTrashMenu() {
        AlertDialog.Builder(this)
            .setTitle("削除確認")
            .setMessage("削除してもよろしいでしょうか")
            .setPositiveButton("削除") { _, _ ->
                detailProgressBar.visibility = android.widget.ProgressBar.VISIBLE
                db.collection("PostData").document(documentId)
                    .delete()
                    .addOnSuccessListener { Toast.makeText(this, "削除しました", Toast.LENGTH_LONG).show() }
                    .addOnCompleteListener { finish() }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "エラー", Toast.LENGTH_LONG).show()
                        Log.e("Error", "Document", e)
                    }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    //編集ダイアログの出力
    private fun createInputEditDialog() {
        val view = layoutInflater.inflate(R.layout.input_layout, null)

        val mDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        //初期化
        val titleEdit = view.inputTitleEditText
        val contentEdit = view.inputContentEditText
        val tagEdit = view.inputTagEditText
        val tagEditAdd = view.inputTagAddButton
        val addButton = view.inputAddButton
        val cancelButton = view.inputCancelButton
        //入れ
        titleEdit.setText(title, TextView.BufferType.NORMAL)
        contentEdit.setText(content, TextView.BufferType.NORMAL)
        addButton.setOnClickListener {
            addButton.isEnabled = false
            cancelButton.isEnabled = false
            view.inputProgressBar.visibility = View.VISIBLE
            val titleText = titleEdit.text.toString().trim()
            val contentText = contentEdit.text.toString().trim()
            if (titleText == "" || contentText == "") {
                Toast.makeText(this, "タイトルか内容が書かれていません", Toast.LENGTH_LONG).show()
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("PostData")
                    .document(documentId)
                    .update(
                        mapOf(
                            "Title" to titleText,
                            "Content" to contentText,
                            "Tag" to recyclerList
                        )
                    )
                    .addOnCompleteListener {
                        mDialog.dismiss()
                        loadData()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"エラーが発生しました",Toast.LENGTH_LONG).show()
                        mDialog.dismiss()
                    }
            }
        }
        cancelButton.setOnClickListener {
            loadData()
            mDialog.dismiss()
            mDialog.cancel()
        }
        mDialog.show()

        //Tagのリサイクラー
        tagRecycler = view.inputTagRecyclerView
        tagRecycler.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
        tagRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        tagEditAdd.setOnClickListener {
            if (recyclerList.indexOf(tagEdit.text.toString()) == -1) {
                recyclerList.add(tagEdit.text.toString())
            }
            tagEdit.setText("", TextView.BufferType.NORMAL)
            tagRecycler.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
            tagRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        }

    }

    fun setList(data: ArrayList<String>) {
        recyclerList = data
        //Tagのリサイクラー
        tagRecycler.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
        tagRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    private fun setLikeButtonListener(flag: Boolean) {
        if (flag) {
            db.collection("PostData").document(documentId)
                .update("Like", likeCountPlus)
                .addOnSuccessListener {

                }
                .addOnCompleteListener {
                    detailLikeCard.setOnClickListener { setLikeButtonListener(false) }
                    detailLikeImageView.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
                    detailLikeCountTextView.text = likeCountPlus.toString()
                    Toast.makeText(this, "いいねしました", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("PostData").document(documentId)
                .update("Like", likeCount)
                .addOnCompleteListener {
                    detailLikeCard.setOnClickListener { setLikeButtonListener(true) }
                    detailLikeImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                    detailLikeCountTextView.text = likeCount.toString()
                    Toast.makeText(this, "いいね解除しました", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onStop() {
        super.onStop()
        GlideApp.with(applicationContext)
            .clear(detailPostImageView)
    }
}
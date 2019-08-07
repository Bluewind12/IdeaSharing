package momonyan.ideasharing.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


class DetailActivity : AppCompatActivity() {

    private var favStar = false
    private lateinit var db: FirebaseFirestore
    private lateinit var documentId: String
    private lateinit var editMenu: MenuItem

    private var uid = "???"
    private var likeCount = 0
    private var commentCount = 0
    private var content = ""
    private var title = ""
    private var tagArrayList = ArrayList<String>()

    private lateinit var recyclerList: ArrayList<String>
    private lateinit var tagRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_layout)
        documentId = intent.getStringExtra("DocumentId")
        db = FirebaseFirestore.getInstance()

        loadData()
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
                                db.collection("PostData").document(documentId)
                                    .update(
                                        mapOf(
                                            "CommentCount" to commentCount
                                        )
                                    )
                                    .addOnCompleteListener {
                                        Toast.makeText(this, "コメント投稿しました", Toast.LENGTH_LONG).show()
                                        detailCommentEditText.setText("", TextView.BufferType.NORMAL)
                                        setCommentList()
                                    }
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
                tagArrayList = dataMap["Tag"] as ArrayList<String>


                detailTitleTextView.text = dataMap["Title"].toString()
                detailContentTextView.text = dataMap["Content"].toString()
                likeCount = dataMap["Like"].toString().toInt()
                commentCount = dataMap["CommentCount"].toString().toInt() + 1
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
                    editMenu.isVisible = false
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
                            GlideApp.with(this)
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
                val putComment = view.commentReEditText.text.toString()
                db.collection("PostData/$path/Comment")
                    .document(documentId)
                    .update("Comment", putComment)
                    .addOnCompleteListener {
                        setCommentList()
                    }
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.detail_menu, menu)
        editMenu = menu.findItem(R.id.detailMenuEdit)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.detailMenuEdit -> {
                createInputEditDialog()
            }
        }
        return true
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
        recyclerList = tagArrayList

        //Tagのリサイクラー
        tagRecycler = view.inputTagRecyclerView
        tagEditAdd.setOnClickListener {
            if (recyclerList.indexOf(tagEdit.text.toString()) == -1) {
                recyclerList.add(tagEdit.text.toString())
            }
            tagEdit.setText("", TextView.BufferType.NORMAL)
            tagRecycler.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
            tagRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        }

        addButton.setOnClickListener {
            val dbMap = java.util.HashMap<String, Any>()
            dbMap["Title"] = titleEdit.text.toString()
            dbMap["Content"] = contentEdit.text.toString()
            dbMap["Tag"] = recyclerList


            val db = FirebaseFirestore.getInstance()
            db.collection("PostData")
                .document(documentId)
                .update(
                    mapOf(
                        "Title" to titleEdit.text.toString(),
                        "Content" to contentEdit.text.toString(),
                        "Tag" to recyclerList
                    )
                )
                .addOnCompleteListener {
                    mDialog.dismiss()
                }
                .addOnFailureListener {
                    Log.e("Error", "ERRORRRRRRRRRRR")
                }
            loadData()
        }
        cancelButton.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    fun setList(data: ArrayList<String>) {
        recyclerList = data
        //Tagのリサイクラー
        tagRecycler.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
        tagRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    private fun setLikeButtonListener(flag: Boolean) {
        if (flag) {
            likeCount++
            db.collection("PostData").document(documentId)
                .update("Like", likeCount)
                .addOnSuccessListener {
                    detailLikeCard.setOnClickListener { setLikeButtonListener(false) }
                    detailLikeImageView.setColorFilter(0xf39800, PorterDuff.Mode.SRC_IN)
                    detailLikeCountTextView.text = likeCount.toString()
                    Toast.makeText(this, "いいねしました", Toast.LENGTH_LONG).show()
                }
        } else {
            likeCount--
            db.collection("PostData").document(documentId)
                .update("Like", likeCount)
                .addOnSuccessListener {
                    detailLikeCard.setOnClickListener { setLikeButtonListener(true) }
                    detailLikeImageView.setColorFilter(0x000000, PorterDuff.Mode.SRC_IN)
                    detailLikeCountTextView.text = likeCount.toString()
                    Toast.makeText(this, "いいね解除しました", Toast.LENGTH_LONG).show()
                }
        }
    }
}
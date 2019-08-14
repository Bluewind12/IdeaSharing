package momonyan.ideasharing.activity

import android.app.DatePickerDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.detail_search_layout.view.*
import kotlinx.android.synthetic.main.input_layout.view.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.InfiniteScrollListener
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.InputTagListRecyclerAdapter
import momonyan.ideasharing.adapter.RecyclerAdapter
import momonyan.ideasharing.getToday
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var sortCheckNew: MenuItem
    private lateinit var sortCheckPop: MenuItem
    private lateinit var sortCheckComment: MenuItem

    private var sort = "Date"

    private var userName = "Unknown"

    private var recyclerList = arrayListOf<String>()

    private lateinit var tagList: RecyclerView
    private lateinit var headerImage: ImageView
    private lateinit var mInterstitialAd: InterstitialAd //AD

    private val limitNum: Long = 10

    private var lastVisible: DocumentSnapshot? = null

    private var item = ArrayList<HashMap<String, Any>>()

    private var loadFrag = true

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //AD
        val adRequest = AdRequest.Builder().build()
        mainAdView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_in_comment)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        //Toolバーへの設定
        setSupportActionBar(toolbar)
        val actionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, mainDrawerLayout, toolbar,
                R.string.open,
                R.string.close
            ) {
                override fun onDrawerStateChanged(newState: Int) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        mainDrawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //色変え
        val navigationDrawer = findViewById<NavigationView>(R.id.navigationView)
        val color = resources.getColor(R.color.colorPrimaryDark)
        val sakuraColor = resources.getColor(R.color.sakuraColor)
        navigationDrawer.menu.findItem(R.id.menuMyPage).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuFav).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuProfileEdit).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuLogOut).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuReview).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuPrivacy).icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        navigationDrawer.menu.findItem(R.id.menuSakuraHp).icon.setColorFilter(sakuraColor, PorterDuff.Mode.SRC_IN)

        //ドロワーメニュー のクリック動作
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuMyPage -> {
                    val i = Intent(this, ProfileDetailActivity::class.java)
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    var uid = "???"
                    if (user != null) {
                        uid = user.uid
                    }
                    i.putExtra("UserId", uid)
                    startActivity(i)
                }
                R.id.menuFav -> {
                    startActivity(Intent(this, FavoriteListActivity::class.java))
                }
                R.id.menuProfileEdit -> {
                    startActivity(Intent(this, ProfileEditActivity::class.java))
                }
                R.id.menuLogOut -> {
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                R.id.menuPrivacy -> {
                    val uri = Uri.parse(getString(R.string.privacy_url))
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(i)
                }
                R.id.menuReview -> {
                    val uri = Uri.parse(getString(R.string.review_url))
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(i)
                }
                R.id.menuSakuraHp -> {
                    val uri = Uri.parse(getString(R.string.sakura_hp_url))
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(i)
                }

            }

            true
        }

        mainSwipeRefresh.setOnRefreshListener {
            loadFrag = true
            loadDatabase()
        }
        //FAB
        floatingActionButton.setOnClickListener {
            createInputDialog()
            Log.e("TESTTAGS", item.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)

        sortCheckNew = menu.findItem(R.id.sortCheckNew)
        sortCheckNew.isChecked = true
        sortCheckPop = menu.findItem(R.id.sortCheckPop)
        sortCheckComment = menu.findItem(R.id.sortCheckComment)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        //検索
        val mSearchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                val item = ArrayList<HashMap<String, Any>>()
                db.collection("PostData")
                    .orderBy(sort, Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val documentMap = document.data as HashMap<String, Any>
                            if (documentMap["Title"].toString().indexOf(s) != -1) {
                                documentMap["DocumentId"] = document.id
                                item.add(documentMap)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        val adapter = RecyclerAdapter(this@MainActivity, item)
                        mainRecyclerView.adapter = adapter
                        mainRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
                    }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sortCheckNew -> {
                sortCheckNew.isChecked = true
                sort = "Date"
                loadDatabase()
            }
            R.id.sortCheckPop -> {
                sortCheckPop.isChecked = true
                sort = "Like"
                loadDatabase()
            }
            R.id.sortCheckComment -> {
                sortCheckComment.isChecked = true
                sort = "CommentCount"
                loadDatabase()
            }
            R.id.app_ber_details ->{
                detailedSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDatabase() {
        item = ArrayList()
        var next: Query = db.collection("PostData")
        db.collection("PostData")
            .orderBy(sort, Query.Direction.DESCENDING)
            .limit(limitNum)
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val documentMap = document.data as HashMap<String, Any>
                    documentMap["DocumentId"] = document.id
                    item.add(documentMap)
                }

                next = db.collection("PostData")
                    .orderBy(sort, Query.Direction.DESCENDING)
                    .startAfter(result.documents[result.size() - 1])
                    .limit(limitNum)
            }
            .addOnCompleteListener {
                val adapter = RecyclerAdapter(this, item)
                val manage = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                mainRecyclerView.adapter = adapter
                mainRecyclerView.layoutManager = manage
                if (limitNum <= item.size.toLong()) {
                    mainRecyclerView.addOnScrollListener(InfiniteScrollListener(manage) {
                        mainProgressBar.visibility = android.widget.ProgressBar.VISIBLE
                        if (loadFrag) {
                            next.get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        val documentMap = document.data as HashMap<String, Any>
                                        documentMap["DocumentId"] = document.id
                                        if (item.indexOf(documentMap) == -1) {
                                            Log.d("TAGTAG", "ITEMADD${document.data as HashMap<String, Any>}")
                                            item.add(documentMap)
                                        } else {
                                            Log.e("TAGTAG", "ITEM_FALSE${document.data as HashMap<String, Any>}")
                                            loadFrag = false
                                        }
                                    }
                                    lastVisible = result.documents[result.size() - 1]
                                }
                                .addOnCompleteListener {
                                    mainRecyclerView.adapter?.notifyDataSetChanged()
                                }
                        }
                    })
                }
                if (mainSwipeRefresh.isRefreshing) {
                    mainSwipeRefresh.isRefreshing = false
                }
                mainProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
            }
    }


    private fun loadHeader() {
        //ヘッダーの内容変更
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            db.collection("ProfileData")
                .document(user.uid)
                .get()
                .addOnSuccessListener { profileResult ->
                    val profileMap = profileResult.data
                    if (profileMap != null) {
                        headerImage = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.navImageView)
                        val headerText = navigationView.getHeaderView(0).findViewById<TextView>(R.id.navNameTextView)
                        userName = profileMap["UserName"].toString()
                        headerText.text = profileMap["UserName"].toString()
                        //ヘッダーのイメージの変更
                        val storageRef = FirebaseStorage.getInstance().reference
                        storageRef.child(user.uid + "ProfileImage")
                            .downloadUrl.addOnSuccessListener {
                            GlideApp.with(applicationContext)
                                .load(it)
                                .into(headerImage)
                        }
                    }
                }
        }
    }

    //Inputダイアログの出力
    private fun createInputDialog() {
        val view = layoutInflater.inflate(R.layout.input_layout, null)

        val mDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val titleEdit = view.inputTitleEditText
        val contentEdit = view.inputContentEditText
        val tagEdit = view.inputTagEditText
        val tagEditAdd = view.inputTagAddButton
        val addButton = view.inputAddButton
        val cancelButton = view.inputCancelButton
        recyclerList = arrayListOf()
        //Tagのリサイクラー
        tagList = view.inputTagRecyclerView
        tagEditAdd.setOnClickListener {
            if (recyclerList.indexOf(tagEdit.text.toString()) == -1) {
                recyclerList.add(tagEdit.text.toString())
            }
            tagEdit.setText("", TextView.BufferType.NORMAL)
            tagList.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
            tagList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        }

        addButton.setOnClickListener {
            addButton.isEnabled = false
            cancelButton.isEnabled = false
            view.inputProgressBar.visibility = View.VISIBLE
            val titleText = titleEdit.text.toString().trim()
            val contentText = contentEdit.text.toString().trim()
            if (titleText == "" || contentText == "") {
                Toast.makeText(this, "タイトルか内容が書かれていません", Toast.LENGTH_LONG).show()
            } else {
                val dbMap = HashMap<String, Any>()
                dbMap["Title"] = titleText
                dbMap["Content"] = contentText
                dbMap["Tag"] = recyclerList

                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    dbMap["Contributor"] = uid
                } else {
                    dbMap["Contributor"] = "???"
                }
                dbMap["Like"] = 0
                dbMap["CommentCount"] = 0
                dbMap["Date"] = getToday()

                db.collection("PostData")
                    .add(dbMap)
                    .addOnCompleteListener {
                        //広告表示
                        if (mInterstitialAd.isLoaded && Random.nextInt(100) >= 70) {
                            mInterstitialAd.show()
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.")
                        }
                        mDialog.dismiss()
                        loadDatabase()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                        mDialog.dismiss()
                    }
            }
            }

        cancelButton.setOnClickListener {
            mDialog.dismiss()
            mDialog.cancel()
        }

        mDialog.show()
    }


    fun setList(data: ArrayList<String>) {
        recyclerList = data
        //Tagのリサイクラー
        tagList.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
        tagList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        loadHeader()
        loadDatabase()
    }

    private fun detailedSearchDialog(){
        val view = layoutInflater.inflate(R.layout.detail_search_layout, null)

        val titleView = view.detailSearchTitle
        val contentView = view.detailSearchContent
        val tagView = view.detailSearchTag
        val fromView = view.detailSearchFrom
        val fromDate = Calendar.getInstance()
        fromView.setOnClickListener {
            //Calendarインスタンスを取得
            //DatePickerDialogインスタンスを取得
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    //setした日付を取得して表示
                    fromView.setText(String.format("%d/%02d/%02d", year, month + 1, dayOfMonth))
                },
                fromDate.get(Calendar.YEAR),
                fromDate.get(Calendar.MONTH),
                fromDate.get(Calendar.DATE)
            )
            //dialogを表示
            datePickerDialog.show()
        }
        val sinceView = view.detailSearchSince
        val sinceDate = Calendar.getInstance()
        sinceView.setOnClickListener {
            //Calendarインスタンスを取得
            //DatePickerDialogインスタンスを取得
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    //setした日付を取得して表示
                    sinceView.setText(String.format("%d/%02d/%02d", year, month + 1, dayOfMonth))
                },
                sinceDate.get(Calendar.YEAR),
                sinceDate.get(Calendar.MONTH),
                sinceDate.get(Calendar.DATE)
            )
            //dialogを表示
            datePickerDialog.show()
        }
        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("検索") { _, _ ->
                val i = Intent(this, DetailSearchActivity::class.java)
                i.putExtra("Title", titleView.text.toString())
                i.putExtra("Content", contentView.text.toString())
                i.putExtra("Tag", tagView.text.toString())
                i.putExtra("From", fromView.text.toString() + "-00:00:00")
                i.putExtra("Since", sinceView.text.toString() + "-23:59:59")
                startActivity(i)
            }
            .setNegativeButton("キャンセル", null)
            .create()
            .show()
    }

    override fun onStop() {
        super.onStop()
        GlideApp.with(applicationContext)
            .clear(headerImage)
    }

}

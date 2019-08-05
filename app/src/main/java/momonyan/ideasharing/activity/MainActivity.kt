package momonyan.ideasharing.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.input_layout.view.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.InputTagListRecyclerAdapter
import momonyan.ideasharing.adapter.RecyclerAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var sortCheckNew: MenuItem
    private lateinit var sortCheckPop: MenuItem

    private var sort = "Date"

    private var userName = "Unknown"

    private var recyclerList = arrayListOf<String>()

    private lateinit var tagList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        toolbar.inflateMenu(R.menu.search_menu)
        //検索
        val mSearchView = toolbar.menu.findItem(R.id.app_bar_search).actionView as SearchView
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
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
                }
                R.id.menuReviewHp -> {
                }
                R.id.menuSakuraHp -> {
                }

            }

            true
        }

        //FAB
        floatingActionButton.setOnClickListener {
            createInputDialog()
        }
        loadDatabase()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)

        sortCheckNew = menu.findItem(R.id.sortCheckNew)
        sortCheckNew.isChecked = true
        sortCheckPop = menu.findItem(R.id.sortCheckPop)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.app_bar_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDatabase() {
        val db = FirebaseFirestore.getInstance()
        val item = ArrayList<HashMap<String, Any>>()
        db.collection("PostData")
            .orderBy(sort, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val documentMap = document.data as HashMap<String, Any>
                    documentMap["DocumentId"] = document.id
                    item.add(documentMap)
                }
            }
            .addOnCompleteListener {
                val adapter = RecyclerAdapter(this, item)
                mainRecyclerView.adapter = adapter
                mainRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            }


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
                        val headerImage = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.navImageView)
                        val headerText = navigationView.getHeaderView(0).findViewById<TextView>(R.id.navNameTextView)
                        userName = profileMap["UserName"].toString()
                        headerText.text = profileMap["UserName"].toString()
                        //ヘッダーのイメージの変更
                        val storageRef = FirebaseStorage.getInstance().reference
                        storageRef.child(user.uid + "ProfileImage").downloadUrl.addOnSuccessListener {
                            GlideApp.with(this /* context */)
                                .load(it)
                                .into(headerImage)
                        }
                    }
                }
        } else {
            //error()
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
            val dbMap = HashMap<String, Any>()
            dbMap["Title"] = titleEdit.text.toString()
            dbMap["Content"] = contentEdit.text.toString()
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
            dbMap["Date"] = getToday()

            val db = FirebaseFirestore.getInstance()
            db.collection("PostData")
                .add(dbMap)
                .addOnCompleteListener {
                    loadDatabase()
                    mDialog.dismiss()
                }
                .addOnFailureListener {
                    Log.e("Error", "ERRORRRRRRRRRRR")
                }
        }
        cancelButton.setOnClickListener {
            loadDatabase()
            mDialog.dismiss()
        }
        mDialog.show()
    }


    private fun getToday(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

    fun setList(data: ArrayList<String>) {
        recyclerList = data
        //Tagのリサイクラー
        tagList.adapter = InputTagListRecyclerAdapter(this, recyclerList, this)
        tagList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        loadDatabase()
    }

}

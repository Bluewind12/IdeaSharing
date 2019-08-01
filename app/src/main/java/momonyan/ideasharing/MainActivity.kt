package momonyan.ideasharing

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.input_layout.view.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var sortCheckNew: MenuItem
    private lateinit var sortCheckPop: MenuItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        val actionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, mainDrawerLayout, toolbar, R.string.open, R.string.close) {
                override fun onDrawerStateChanged(newState: Int) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        mainDrawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        toolbar.inflateMenu(R.menu.search_menu)
        val mSearchView = toolbar.menu.findItem(R.id.app_bar_search).actionView as SearchView
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

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
            }
            R.id.sortCheckPop -> {
                sortCheckPop.isChecked = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDatabase() {
        val db = FirebaseFirestore.getInstance()
        val item = ArrayList<HashMap<String, Any>>()
        db.collection("Data")
            .orderBy("Date", Query.Direction.DESCENDING)
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
    }

    private fun createInputDialog() {
        val view = layoutInflater.inflate(R.layout.input_layout, null)

        val mDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val titleEdit = view.inputTitleEditText
        val contentEdit = view.inputContentEditText
        val tagEdit = view.inputTagEditText
        val tagEditAdd = view.inputTagAddButton
        val tagList = view.inputTagRecyclerView
        val addButton = view.inputAddButton
        val cancelButton = view.inputCancelButton

        val recyclerList = arrayListOf<String>()

        tagEditAdd.setOnClickListener {
            recyclerList.add(tagEdit.text.toString())
            tagEdit.setText("", TextView.BufferType.NORMAL)
            //TODO ここでRecyclerリスナーをいじる
            tagList.adapter = InputTagListRecyclerAdapter(this, recyclerList)
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
            mDialog.dismiss()
        }
        mDialog.show()
    }
}

package momonyan.ideasharing.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.detail_search_out_layout.*
import momonyan.ideasharing.R
import momonyan.ideasharing.adapter.RecyclerAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class DetailSearchActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_search_out_layout)

        //AD
        val adRequest = AdRequest.Builder().build()
        detailSearchAdView.loadAd(adRequest)


        val title = intent.getStringExtra("Title")
        val content = intent.getStringExtra("Content")
        val tag = intent.getStringExtra("Tag")
        val from = intent.getStringExtra("From")
        val since = intent.getStringExtra("Since")

        val df = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss")

        var fromDate: Date? = null
        var sinceDate: Date? = null
        fromDate = if (from != "-00:00:00") {
            df.parse(from)
        } else {
            df.parse("2000/01/01-00:00:00")
        }
        sinceDate = if (since != "-23:59:59") {
            df.parse(since)
        } else {
            df.parse("9999/01/01-00:00:00")
        }
        //DB
        val db = FirebaseFirestore.getInstance()
        val item = ArrayList<HashMap<String, Any>>()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            db.collection("PostData")
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    count = 0
                    for (document in result) {
                        val documentMap = document.data as HashMap<String, Any>
                        val detailTitle = documentMap["Title"].toString()
                        val detailContent = documentMap["Content"].toString()
                        val detailTag = documentMap["Tag"] as ArrayList<String>
                        val detailDateString = documentMap["Date"].toString()
                        val detailDate = df.parse(detailDateString)
                        if (
                            (detailTitle.contains(title) || title == "") &&
                            (detailContent.contains(content) || content == "") &&
                            (detailTag.indexOf(tag) != -1 || tag == "") &&
                            (detailDate.after(fromDate)) &&
                            (detailDate.before(sinceDate))
                        ) {
                            documentMap["DocumentId"] = document.id
                            count++
                            item.add(documentMap)
                        }
                    }
                }
                .addOnCompleteListener {
                    val adapter = RecyclerAdapter(this, item)
                    detailSearchRecyclerView.adapter = adapter
                    detailSearchRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    detailSearchCountText.text = "$count 個のアイデアがヒットしました"
                    detailSearchOutProgressBar.visibility = android.widget.ProgressBar.INVISIBLE
                }
            detailSearchOutProgressBar.bringToFront()

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

}
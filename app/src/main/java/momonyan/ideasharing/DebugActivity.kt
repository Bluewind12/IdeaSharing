package momonyan.ideasharing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debug_layout.*

class DebugActivity : AppCompatActivity() {
    private val tag = "DEBUG_TAAAAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

        val db = FirebaseFirestore.getInstance()

        debugAddButton.setOnClickListener {
            Log.d(tag,"TESTE")
            val dbMap = HashMap<String, Any>()
            dbMap["Title"] = titleEditText.text.toString()
            dbMap["Contents"] = contentsEditText.text.toString()
            db.collection("Data")
                .add(dbMap)
                .addOnSuccessListener { documentReference ->
                    Log.d(tag, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Error adding document", e)
                }

        }

    }
}
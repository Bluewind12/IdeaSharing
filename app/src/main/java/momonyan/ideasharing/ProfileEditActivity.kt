package momonyan.ideasharing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbMap = HashMap<String, Any>()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            dbMap["Contributor"] = uid
        } else {
            dbMap["Contributor"] = "???"
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("ProfileData")
            .add(dbMap)
            .addOnCompleteListener {
            }
            .addOnFailureListener {
                Log.e("Error", "ERRORRRRRRRRRRR")
            }
    }
}
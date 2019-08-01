package momonyan.ideasharing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.profile_edit_layout.*
import java.io.IOException
import java.util.*


class ProfileEditActivity : AppCompatActivity() {
    private val READ_REQUEST_CODE = 1202
    private var uri: Uri? = null
    private var uid: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit_layout)

        val dbMap = HashMap<String, Any>()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
        }
        dbMap["UserId"] = uid
        profileEditImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"

            startActivityForResult(intent, READ_REQUEST_CODE)

        }
        profileAddButton.setOnClickListener {
            if (profileNameEditText.text.toString() == "") {
                Toast.makeText(this, "ニックネームを入力してください", Toast.LENGTH_LONG).show()
            } else {
                dbMap["UserName"] = profileNameEditText.text.toString()
                dbMap["Comment"] = profileCommentEditButton.text.toString()
                dbMap["HpData"] = profileHpEditButton.text.toString()

                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReferenceFromUrl("gs://ideasharing-8a024.appspot.com/")
                val dir = storageRef.child(uid + "ProfileImage")
                val task = dir.putFile(uri!!)
                task.addOnSuccessListener {
                    // 成功したとき
                    Toast.makeText(this, "OK", Toast.LENGTH_LONG).show()
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        dbMap["ImageURL"] = downloadUri.toString()
                        val db = FirebaseFirestore.getInstance()
                        db.collection("ProfileData")
                            .add(dbMap)
                            .addOnCompleteListener {
                                //TODO Mainへの遷移
                            }
                            .addOnFailureListener {

                            }
                    }
                }
                task.addOnFailureListener {
                    // 失敗したとき
                    Toast.makeText(this, "NG", Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uri = resultData.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    profileEditImageButton.setImageBitmap(bitmap)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
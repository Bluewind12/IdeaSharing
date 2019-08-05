package momonyan.ideasharing.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.android.synthetic.main.profile_edit_layout.*
import momonyan.ideasharing.GlideApp
import momonyan.ideasharing.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class ProfileEditActivity : AppCompatActivity() {
    private val READ_REQUEST_CODE = 1202
    private var uri: Uri? = null
    private var uid: String = ""

    private lateinit var bmp :Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit_layout)

        val dbMap = HashMap<String, Any>()
        val db = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
        }
        dbMap["UserId"] = uid

        //プロフ編集のイメージの変更
        val storageRef = FirebaseStorage.getInstance().reference
        storageRef.child(user!!.uid + "ProfileImage").downloadUrl.addOnSuccessListener {
            GlideApp.with(this /* context */)
                .load(it)
                .into(profileEditImageButton)
        }
        db.collection("ProfileData")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                val userMap = result.data!!
                profileNameEditText.setText(userMap["UserName"].toString(), TextView.BufferType.NORMAL)
                profileCommentEditText.setText(userMap["Comment"].toString(), TextView.BufferType.NORMAL)
                profileHpEditText.setText(userMap["HP"].toString(), TextView.BufferType.NORMAL)
                profileOtherEditText.setText(userMap["Other"].toString(), TextView.BufferType.NORMAL)

            }

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
                dbMap["Comment"] = profileCommentEditText.text.toString()
                dbMap["HP"] = profileHpEditText.text.toString()
                dbMap["Other"] = profileOtherEditText.text.toString()
                dbMap["CommentCount"] = 0

                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReferenceFromUrl("gs://ideasharing-8a024.appspot.com/")

                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()

                val baos = ByteArrayOutputStream()
                bmp = (profileEditImageButton.drawable as BitmapDrawable).bitmap
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                storageRef.child(uid + "ProfileImage").putBytes(data, metadata)
                    .addOnSuccessListener {
                        dbMap["ImageURL"] = uid + "ProfileImage"
                        db.collection("ProfileData")
                            .document(uid)
                            .set(dbMap)
                            .addOnCompleteListener {
                                val i = Intent(this, MainActivity::class.java)
                                startActivity(i)
                            }
                            .addOnFailureListener {

                            }
                    }
                    .addOnFailureListener {
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
                    bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    profileEditImageButton.setImageBitmap(bmp)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
package momonyan.ideasharing.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.android.synthetic.main.image_trimming_layout.view.*
import kotlinx.android.synthetic.main.profile_edit_layout.*
import momonyan.ideasharing.R
import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class ProfileFirstEditActivity : AppCompatActivity() {
    private val READ_REQUEST_CODE = 1202
    private var uri: Uri? = null
    private var uid: String = ""
    private val dbMap = HashMap<String, Any>()

    private lateinit var bmp :Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_edit_layout)

        profileEditProgressBar.visibility = android.widget.ProgressBar.INVISIBLE

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
        }
        dbMap["UserId"] = uid

        bmp = BitmapFactory.decodeResource(resources, R.drawable.icon_defalt)
        profileEditImageButton.setImageBitmap(bmp)

        if (RuntimePermissionChecker.hasSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE")
        ) {
            // パーミッションの許可をリクエスト
            RuntimePermissionChecker.requestAllPermissions(this, 1234)
        }
        // パーミッションが許可されている
        else {
            profileEditImageButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, READ_REQUEST_CODE)
            }
        }
        profileAddButton.setOnClickListener {
            addData()
        }
    }

    private fun addData() {
        if (profileNameEditText.text.toString() == "") {
            Toast.makeText(this, "ニックネームを入力してください", Toast.LENGTH_LONG).show()
        } else {
            profileEditProgressBar.visibility = android.widget.ProgressBar.VISIBLE
            profileAddButton.isEnabled = false
            dbMap["UserName"] = profileNameEditText.text.toString()
            dbMap["Comment"] = profileCommentEditText.text.toString()
            dbMap["Twitter"] = profileTwitterEditText.text.toString()
            dbMap["Facebook"] = profileFaceBookEditText.text.toString()
            dbMap["HP"] = profileHpEditText.text.toString()
            dbMap["Other"] = profileOtherEditText.text.toString()

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
                    val db = FirebaseFirestore.getInstance()
                    db.collection("ProfileData")
                        .document(uid)
                        .set(dbMap)
                        .addOnCompleteListener {
                            Toast.makeText(this, "プロフィール登録しました", Toast.LENGTH_LONG).show()
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uri = resultData.data
                try {
                    val view = layoutInflater.inflate(R.layout.image_trimming_layout, null)
                    view.cropImageView.imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton("決定") { _, _ ->
                            bmp = view.cropImageView.croppedBitmap
                            profileEditImageButton.setImageBitmap(bmp)
                        }
                        .setNegativeButton("キャンセル"){ _, _ ->
                            bmp = BitmapFactory.decodeResource(resources, R.drawable.icon_defalt)
                            profileEditImageButton.setImageBitmap(bmp)
                        }
                        .create()
                        .show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.profile_edit_menu, menu)
        menu.findItem(R.id.profileEditBack).isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profileEditBack -> {
                finish()
            }
            R.id.profileEditAdd -> {
                addData()
            }
        }
        return true
    }
}
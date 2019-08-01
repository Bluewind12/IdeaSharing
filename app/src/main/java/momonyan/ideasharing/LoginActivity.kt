package momonyan.ideasharing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.login_layout.*


class LoginActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        loginButton.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls(
                        "https://",
                        "https://")
                    .setLogo(R.drawable.icon_book)
                    .setIsSmartLockEnabled(true)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//Intentの?ないとクラッシュするので注意
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {

                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                db.collection("ProfileData")
                    .whereEqualTo("UserId", user!!.uid)
                    .get()
                    .addOnSuccessListener { result ->
                        Toast.makeText(this, "サインイン成功", Toast.LENGTH_LONG).show()
                        if (result.documents.size != 0) {
                            val i = Intent(this, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                        } else {
                            val i = Intent(this, ProfileEditActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                        }
                    }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "何もせず帰ってきた", Toast.LENGTH_LONG).show()
                    return
                }

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "ネットに繋がっていない", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this, "サインイン失敗", Toast.LENGTH_LONG).show()
                Log.e("firebase", "Sign-in error: ", response.error)
            }
        }
    }
}
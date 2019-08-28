package momonyan.ideasharing.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.login_layout.*
import momonyan.ideasharing.R
import momonyan.ideasharing.startImageAnimation
import momonyan.ideasharing.startTextAnimation


class LoginActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        //Title
        val font = Typeface.createFromAsset(assets, "fonts/ranobe.ttf")
        loginTitleText.typeface = font
        loginTapTextView.typeface = font
        //AdInit
        MobileAds.initialize(this, "ca-app-pub-6499097800180510~7424909357")

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startTextAnimation(loginTapTextView)
        startImageAnimation(loginTapImage)

        loginConstraintLayOut.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls(
                        getString(R.string.privacy_url),
                        getString(R.string.privacy_url)
                    )
                    .setLogo(R.drawable.app_icon)
                    .setIsSmartLockEnabled(true)
                    .build(),
                RC_SIGN_IN
            )
        }
        loginConstraintLayOut.setOnLongClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosAndPrivacyPolicyUrls(
                        getString(R.string.privacy_url),
                        getString(R.string.privacy_url)
                    )
                    .setLogo(R.drawable.app_icon)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN
            )
            true
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
                        if (result.documents.size != 0) {
                            val i = Intent(this, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                        } else {
                            val i = Intent(this, ProfileFirstEditActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                        }
                    }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return
                }

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "ネットワークに接続してください", Toast.LENGTH_LONG).show()
                    return
                }
                Toast.makeText(this, "サインイン失敗", Toast.LENGTH_LONG).show()
            }
        }
    }
}
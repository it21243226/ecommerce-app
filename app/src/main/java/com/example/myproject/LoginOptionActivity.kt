package com.example.myproject

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myproject.databinding.ActivityLoginOptionBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginOptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionBinding

    private companion object{
        private const val TAG="LOGIN_OPTIONS_TAG"
    }
    private lateinit var progressDialog:ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient:GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth=FirebaseAuth.getInstance()

        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient=GoogleSignIn.getClient(this,gso)

        binding.closeBtn.setOnClickListener {
            onBackPressed()

        }
        binding.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.loginGoogleBtn.setOnClickListener {
            beginGoogleLogin()

        }
        binding.loginPhoneBtn.setOnClickListener {
            startActivity(Intent(this,LoginPhoneActivity::class.java))
        }
    }
    private fun beginGoogleLogin(){
        Log.d(TAG, "beginGoogleLogin: ")

        val googleSignIntent=mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignIntent)
    }
    private val googleSignInARL=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result->
        Log.d(TAG, "GoogleSignInARL: ")

        if (result.resultCode== RESULT_OK){
            val data = result.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account=task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL:Account ID: ${account.id} ")
                firebaseAuthWithGoogleAccount(account.idToken)

            }
            catch (e:Exception){
                Log.e(TAG, "googleSSignInARL: ",e )
                Utils.toast(this,"${e.message}")

            }

        }
        else{
            Utils.toast(this,"Canceled...")
        }
    }
    private fun firebaseAuthWithGoogleAccount(idToken:String?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken:$idToken")

        val credential=GoogleAuthProvider.getCredential(idToken,null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult->
                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User,Account created...")
                    updateUserInfoDb()

                }else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User,Logged In...")
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()


                }

            }
            .addOnFailureListener { e->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ",e )
                Utils.toast(this,"${e.message}")
            }

    }
    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")
        progressDialog.setMessage("Saving User Info")
        progressDialog.show()

        val timestamp=Utils.geTimestamp()
        val registeredUserEmail=firebaseAuth.currentUser?.email
        val registeredUserUid=firebaseAuth.uid
        val name= firebaseAuth.currentUser?.displayName

        val hashMap= HashMap<String,Any?>()
        hashMap["name"]="$name"
        hashMap["phoneCode"]=""
        hashMap["phoneNumber"]=""
        hashMap["profileImageUrl"]=""
        hashMap["dob"]=""
        hashMap["userType"]="Google"
        hashMap["typingTo"]=""
        hashMap["timestamp"]=timestamp
        hashMap["OnlineStatus"]=registeredUserEmail
        hashMap["uid"]=registeredUserUid

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User Info saved ")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.e(TAG, "updateUserInfoDb: ",e )
                Utils.toast(this,"Failed to save user info due to ${e.message}")
            }
    }
}
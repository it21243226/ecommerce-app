package com.example.myproject

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.myproject.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{

        private const val TAG = "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)

        progressDialog.setTitle(" Please Wait...")

        progressDialog.setCanceledOnTouchOutside(false)



        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }


        binding.haveAccountTv.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateDate()
        }
    }

    private var email = ""
    private var password = ""
    private var cpassword = ""

    private fun validateDate(){
        email= binding.emailEt.text.toString().trim()
        password= binding.passwordlEt.text.toString().trim()
        cpassword= binding.cpasswordlEt.text.toString().trim()

        Log.d(TAG, "validateDate: email: $email")
        Log.d(TAG, "validateDate: password: $password")
        Log.d(TAG, "validateDate: Confirm Password: $cpassword")

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error = "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }

        else if (password.isEmpty()){
            binding.passwordlEt.error = " Enter Password"
            binding.passwordlEt.requestFocus()
        }

        else if (cpassword.isEmpty()){
            binding.cpasswordlEt.error = " Enter Confirm Password"
            binding.cpasswordlEt.requestFocus()
        }
        else if (password != cpassword){
            binding.cpasswordlEt.error = " Password doesn't Match"
            binding.cpasswordlEt.requestFocus()
        }
        else {
            registerUser()
        }
    }

    private fun registerUser(){
        Log.d(TAG, "registerUser: ")

        progressDialog.setMessage("Creating Acoount")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "registerUser: Register Success")
                updateUserInfo()
            }
            .addOnFailureListener { e->

                Log.d(TAG, "registerUser: ",e)
                progressDialog.dismiss()
                Utils.toast(this, "failed to create account due to ${e.message}")

            }

    }

    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        progressDialog.setMessage(" saving User Info")

        val timestamp= Utils.geTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid


        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] =""
        hashMap["phoneNumber"] =""
        hashMap["profileImageUrl"] =""
        hashMap["dob"] =""
        hashMap["userType"] ="Email"
        hashMap["typingTo"] =""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] ="$registeredUserEmail"
        hashMap["uid"] ="$registeredUserUid"


        val reference= FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "updateUserInfo: User Registered... ")
                progressDialog.dismiss()

                startActivity( Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->

                Log.d(TAG, "updateUserInfo: ",e)
                progressDialog.dismiss()
                Utils.toast(this, " Failded to save user info due to ${e.message}")
            }
    }
}
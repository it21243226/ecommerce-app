package com.example.myproject

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.myproject.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG="LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth= FirebaseAuth.getInstance()


        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }
        //handle forget Password
        binding.forgotPasswordTv.setOnClickListener {

            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

        binding.logiBtn.setOnClickListener {
            validateData()

        }
    }
    private var email=""
    private var password=""

    private fun validateData(){
        email=binding.emailEt.text.toString().trim()
        password=binding.passwordlEt.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")

//        validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error="Invalid email foramat"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordlEt.error="Enter password"
            binding.passwordlEt.requestFocus()
        }
        else{
            loginUser()

        }

    }

    private fun loginUser(){
        Log.d(TAG, "loginUser: ")

        progressDialog.setMessage(("Logging In"))
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                Log.d(TAG, "loginUser: Logged In.." )
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

            }
            .addOnFailureListener{e->
                Log.d(TAG, "loginUser: ",e)
                progressDialog.dismiss()

                Utils.toast( this, "unable to login due to ${e.message}")

            }

    }
}
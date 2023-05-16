package com.example.myproject

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.myproject.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private companion object{
        private const val TAG = "FORGOT_PASSWORD"

    }
    private lateinit var progressDialog:ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth= FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var email=""

    private fun validateData(){

        email = binding.emailEt.text.toString().trim()

        Log.d(TAG, "validateData: email:$email")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            binding.emailEt.error= "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }else{

            sendPasswordRecoveryInstructions()
        }
    }
    private fun sendPasswordRecoveryInstructions(){
        Log.d(TAG, "sendPasswordRecoveryInstructions: ")

        progressDialog.setMessage("Sending password reset instruction to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)

            .addOnSuccessListener {
                Utils.toast(this,"Instructions to rest password sent to $email")

            }
            .addOnFailureListener {e->
                Log.e(TAG, "sendPasswordRecoveryInstructions: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"Failed to send due to ${e.message}")
            }

    }
}
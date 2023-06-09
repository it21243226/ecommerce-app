package com.example.myproject

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bumptech.glide.Glide
import com.example.myproject.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    private companion object{
        private const val TAG="Account_TAG"
    }

    private lateinit var mContext:Context

    private lateinit var progressDialog:ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentAccountBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog= ProgressDialog(mContext)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth= FirebaseAuth.getInstance()

        loadMyInfo()

        //handle logout btn
        binding.logoutCv.setOnClickListener{
            firebaseAuth.signOut()

            startActivity(Intent(mContext,MainActivity::class.java))
            activity?.finishAffinity()

        }

        //handle edit profile
        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext,ProfileEditActivity::class.java))
        }
        //handle verify account
        binding.verifyAccountCv.setOnClickListener {
            verifyAccount()
        }
        //handle delete account
        binding.deleteAccountCv.setOnClickListener {
            startActivity(Intent(mContext, DeleteAccountActivity::class.java))

        }
        binding.changePasswordCv.setOnClickListener {
            startActivity(Intent(mContext,ChangePasswordActivity::class.java))
        }
    }
    private fun loadMyInfo(){

        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    val dob="${snapshot.child("dob").value}"
                    val email="${snapshot.child("email").value}"
                    val name="${snapshot.child("name").value}"
                    val phoneCode="${snapshot.child("phoneCode").value}"
                    val phoneNumber="${snapshot.child("phoneNumber").value}"
                    val profileImageUrl="${snapshot.child("profileImageUrl").value}"
                    var timestamp="${snapshot.child("timestamp").value}"
                    val userType="${snapshot.child("userType").value}"


                    val phone= phoneCode+phoneNumber

                    if(timestamp=="null"){
                        timestamp="0"
                    }
                    val formattedDate=Utils.formatTimestampDate(timestamp.toLong())

                    binding.emailTv.text=email
                    binding.nameTv.text=name
                    binding.dobTv.text=dob
                    binding.phoneTv.text=phone
                    binding.memberSinceTv.text=formattedDate

                    if(userType=="Email"){

                        val isVerified=firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified){

                            binding.verifyAccountCv.visibility =View.GONE
                            binding.verificationTv.text="verified"
                        }
                        else{
                            binding.verifyAccountCv.visibility=View.VISIBLE
                            binding.verificationTv.text="Not verified"
                        }

                    }
                    else{
                        binding.verifyAccountCv.visibility = View.GONE
                        binding.verificationTv.text="verified"
                    }

                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv)

                    }
                    catch (e : Exception){
                        Log.d(TAG, "onDataChange: ",e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
private fun verifyAccount(){
    Log.d(TAG, "verifyAccount: ")
    progressDialog.setMessage("Sending account verification instruction to your email...")
    progressDialog.show()


    firebaseAuth.currentUser!!.sendEmailVerification()
        .addOnSuccessListener {
            Log.d(TAG, "verifyAccount: Successfully sent")
            progressDialog.dismiss()
            Utils.toast(mContext,"Account verifications sent to your email...")
        }
        .addOnFailureListener { e->
            Log.e(TAG, "verifyAccount: ",e )
            progressDialog.dismiss()
            Utils.toast(mContext,"Failed to send due to ${e.message}")
        }

}

}
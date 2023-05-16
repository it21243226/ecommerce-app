package com.example.myproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.myproject.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private companion object {
        private const val TAG="PROFILE_DIT_TAG"
    }
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var myUserType=""

    private var imageUri: Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)



        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth= FirebaseAuth.getInstance()
        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileImgPickFab.setOnClickListener {
            imgPickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name =""
    private var dob =""
    private var email =""
//    private var phoneCode =""
//    private var phoneNumber =""

    private fun validateData(){
        name= binding.nameEt.text.toString().trim()
        dob= binding.dobEt.text.toString().trim()
        email= binding.emailEt.text.toString().trim()

        if(imageUri == null){
            updateProfileDb(null)
        }else{
            uploadProfileImageStorage()
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun uploadProfileImageStorage() {

        Log.d(TAG, "uploadProfileImageStorage: ")

        progressDialog.setMessage("Uploading User profile Image")
        progressDialog.show()

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"
            val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot->

            val progress = 100.0* snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress:$progress")
                progressDialog.setMessage("Uploading profile image: progress: $progress")

            }
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG, "uploadProfileImageStorage: Image uploaded...")

                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                    val uploadedImageUrl=uriTask.result.toString()
                if(uriTask.isSuccessful){
                    updateProfileDb(uploadedImageUrl)

                }

            }
            .addOnFailureListener{ e->

                Log.d(TAG, "uploadProfileImageStorage: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"Failed to Upload due to ${e.message}")
            }


    }

    private fun updateProfileDb(uploadedImageUrl: String?){
        Log.d(TAG, "updateProfileDb: uploadedImageurl: $uploadedImageUrl")
        progressDialog.setMessage("Updating user info")
        progressDialog.show()

        val hashMap = HashMap<String,Any>()
        hashMap["name"]="$name"
//        hashMap["email"]="$email"
        hashMap["dob"]="$dob"
        if(uploadedImageUrl != null){

            hashMap["profileImageUrl"]="$uploadedImageUrl"
        }

//        if (myUserType.equals("Phone",true)){
//            hashMap["email"]="$email"
//
//        }else if (){
//
//        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: Updated...")
                progressDialog.dismiss()
                Utils.toast(this,"Updated...")
                imageUri=null

            }
            .addOnFailureListener {e->
                Log.d(TAG, "updateProfileDb: ",e)
                progressDialog.dismiss()
                Utils.toast(this,"Failed to update due to ${e.message}")

            }
    }

    private fun loadMyInfo(){
        Log.d(TAG, "loadMyInfo: ")

        val ref= FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val dob ="${snapshot.child("dob").value}"
                    val email ="${snapshot.child("email").value}"
                    val name ="${snapshot.child("name").value}"
                    val phoneCode ="${snapshot.child("phoneCode").value}"
                    val phoneNumber ="${snapshot.child("phoneNumber").value}"
                    val profileImgUrl ="${snapshot.child("profileImgUrl").value}"
                    val timestamp ="${snapshot.child("timestamp").value}"
                   myUserType="${snapshot.child("userType").value}"



                    val phone=phoneCode+phoneNumber

                    if (myUserType.equals("Email",true)||myUserType.equals("Google",true)){


                        binding.emailTil.isEnabled=false
                        binding.emailEt.isEnabled=false
                    }
                    else{

                    }

                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)


                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImgUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into((binding.profileIv))
                    }
                    catch (e: Exception){
                        Log.d(TAG, "onDataChange: ",e)
                    }


                }

                override fun onCancelled(error: DatabaseError) {



                }

            })

    }
    private fun imgPickDialog(){

        val popupMenu= PopupMenu(this,binding.profileImgPickFab)

        popupMenu.menu.add(Menu.NONE,1 ,1 ,"Camera")
        popupMenu.menu.add(Menu.NONE,2 ,2,"Camera")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->

            val itemId= item.itemId

            if (itemId == 1){
                Log.d(TAG, "imgPickDialog: Camara Clicked, check if camera permission(s) granted or not ")
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){

                    requestCameraPermissions.launch((arrayOf(android.Manifest.permission.CAMERA)))
                }
                else{

                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }

            }
            else if(itemId == 2){

                Log.d(TAG, "imgPickDialog: Gallery Clicked, check if storage permission granted or not")
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){

                    pickImgGallery()
                }else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

            }
            return@setOnMenuItemClickListener true

        }

    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result->
            Log.d(TAG, "requestCamaraPermissions: result $result ")

            var areAllGranted=true
            for(isGranted in result.values){
                areAllGranted=areAllGranted && isGranted
            }
            if(areAllGranted){
                Log.d(TAG, "requestCameraPermission: All granted e.g.camera,storage ")
                pickImgCamera()
            }else{
                Log.d(TAG, "requestCameraPermissions: All or either one is denied ")
                Utils.toast(this,"Camera or Storage or both permission denied")

            }

        }
    private val requestStoragePermission=
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            Log.d(TAG, "rquestStoragePermission: isGranted $isGranted ")

            if(isGranted){

                pickImgGallery()

            }else{

                Utils.toast(this,"Storage permisiion denied..")
            }

        }


    private fun pickImgCamera(){

        Log.d(TAG, "pickImgCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch((intent))

    }

    private val cameraActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if (result.resultCode == Activity.RESULT_OK){
                Log.d(TAG, "cameraActivityResultLauncher:Image Captured :InageUri:$imageUri ")
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileIv)

                }
                catch (e:java.lang.Exception){
                    Log.d(TAG, "cameraActivityResultLauncher: ",e)
                }
            }else{

                Utils.toast(this,"Cancelled")

            }

        }
    private fun pickImgGallery(){
        Log.d(TAG, "pickImgGallery: ")

        val intent=Intent(Intent.ACTION_PICK)

        intent.type="image/*"
        galleryActivityResultLauncher.launch(intent)

    }
    private val galleryActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if(result.resultCode == Activity.RESULT_OK){

                val data =result.data

                imageUri=data!!.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileIv)
                }
                catch (e: java.lang.Exception){
                    Log.d(TAG, "galleryActivityResultLauncher: ",e)
                }
            }
        }
}
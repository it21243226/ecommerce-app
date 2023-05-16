package com.example.myproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myproject.R
import com.example.myproject.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth=FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser==null){

            startLoginOption()

        }

        showHomeFragment()

        binding.bottomNv.setOnItemSelectedListener { item->

            when(item.itemId){
                R.id.menu_home->{

                    showHomeFragment()

                        true
            }
                R.id.menu_chats->{

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOption()
                        false
                    }
                    else{
                        showChatsFragment()

                        true
                    }




                }
                R.id.menu_my_ads->{

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOption()
                        false
                    }
                    else{
                        showMyAdsFragment()

                        true
                    }



                }
                R.id.menu_account->{

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Login Required")
                        startLoginOption()
                        false
                    }
                    else{
                        showAccountFragment()

                        true
                    }




                }
                else->{

                    false
                }
            }
        }

        binding.sellFab.setOnClickListener {
            startActivity(Intent(this, AdCreateActivity::class.java))
        }
    }
    private fun showHomeFragment(){

        binding.toolbarTitleTv.text="Home"

//        show home fragment
        val fragment= HomeFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment , "HomeFragment")
        fragmentTransaction.commit()

    }
    private fun showChatsFragment(){

        binding.toolbarTitleTv.text="Chats"

        val fragment= ChatsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment , "ChatsFragment")
        fragmentTransaction.commit()

    }
    private fun showMyAdsFragment(){

        binding.toolbarTitleTv.text="My Ads"

        val fragment= MyAdsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment , "MyAdsFragment")
        fragmentTransaction.commit()

    }
    private fun showAccountFragment(){

        binding.toolbarTitleTv.text="Account"

        val fragment= AccountFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment , "AccountFragment")
        fragmentTransaction.commit()

    }

    private fun startLoginOption(){

        startActivity(Intent(this,LoginOptionActivity::class.java))
    }

}
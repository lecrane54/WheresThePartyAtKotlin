package com.example.lefeb.wheresthepartyat.view.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.lefeb.wheresthepartyat.R
import com.example.lefeb.wheresthepartyat.view.model.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val uid:String? = FirebaseAuth.getInstance().currentUser!!.uid
    private val name1 = FirebaseAuth.getInstance().currentUser!!.displayName
    private val characterCount = 150
    private val isEdit = false
    private lateinit var bio: String
    private lateinit var i:Intent
    private var ref: CollectionReference = FirebaseFirestore.getInstance().collection("users")
    private var fbId: String? = ""
    private lateinit var listy:MutableList<User>
    var storage : FirebaseStorage = FirebaseStorage.getInstance()
    var mStorageReference : StorageReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        i = intent
        val detail = i.getStringExtra("details")
        val user = i.getStringExtra("user")

        if(user == null){
            //own profile
            Log.d("dddd","own profile")
            ownProfile()
        }else{
            // someone elses

        }



    }

    private fun ownProfile(){
        about.hint = ""
        about.setText("")
        name.text = name1

        mStorageReference = storage!!.getReference("userImages/").child("$uid.png")

        Glide.with(this)
                .load(mStorageReference)
                .into(profile_image!!)
        //TODO make Bio data model - on creation of user, create bio with nullables


        // then get my stuff
    }
}

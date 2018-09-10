package com.example.lefeb.wheresthepartyat.view.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.lefeb.wheresthepartyat.R
import com.example.lefeb.wheresthepartyat.view.fragments.MapFragment
import com.example.lefeb.wheresthepartyat.view.fragments.PartyListFragment
import com.example.lefeb.wheresthepartyat.view.model.Party
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mapbox.mapboxsdk.Mapbox
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, PartyListFragment.OnFragClick {
    override fun onClick(item: Party) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private var firebaseUser: FirebaseUser? = null
    var mStorageReference :StorageReference? = null
    var storage: FirebaseStorage? = null
    private var db : FirebaseFirestore? = null
    private var name:String? = null
    private var email:String? = null
    private var uid : String? = null
    private var profileImage:CircleImageView? = null
    private var nameText:TextView? = null
    private var emailText:TextView? = null
    private var navView: NavigationView? = null
    private lateinit var fragment: MapFragment
    private lateinit var partyListFragment: PartyListFragment
    private lateinit var manager: android.support.v4.app.FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Mapbox.getInstance(applicationContext,getString(R.string.access_token))

        initDB()
        initNavView()
        manager = supportFragmentManager
        fragment = MapFragment()
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
        fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.OrangeRed))
        nav_view.setNavigationItemSelectedListener(this)
        getUserImage()

        newFragment()


    }

    fun newFragment(){
        val ft: android.support.v4.app.FragmentTransaction = manager.beginTransaction()
        ft.replace(R.id.container,fragment)
        ft.commit()
    }

    fun initNavView(){
        navView = nav_view
        nameText = navView!!.getHeaderView(0).findViewById(R.id.name)
        emailText = navView!!.getHeaderView(0).findViewById(R.id.email)
        profileImage = navView!!.getHeaderView(0).findViewById(R.id.imageView)
    }

    fun initDB(){
        firebaseUser = FirebaseAuth.getInstance().currentUser
        uid = FirebaseAuth.getInstance()!!.uid
        name = firebaseUser!!.displayName
        email = firebaseUser!!.email
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        mStorageReference = storage!!.getReference("userImages/").child("$uid.png")
    }

    override fun onClick(v: View?) {
        if(v == fab){
            val i = Intent(this,PartyStater::class.java)
            startActivityForResult(i,4)
        }else{
            val i = Intent(this,ProfileActivity::class.java)
            startActivity(i)
        }

    }

    private fun getUserImage(){
        if(nameText != null && emailText != null){
            nameText!!.text = name
            emailText!!.text = email
            Glide.with(this)
                    .load(mStorageReference)
                    .into(profileImage!!)
        }else{
            Log.d("dddd","$name $email  figure it out")
        }

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.mapicon -> {
                // Handle the camera action
            }
            R.id.partiesicon -> {
                partyListFragment = PartyListFragment()
                val ft: android.support.v4.app.FragmentTransaction = manager.beginTransaction()
                ft.replace(R.id.container,partyListFragment)
                ft.commit()
            }
            R.id.settingsicon -> {

            }
            R.id.licensesicon -> {

            }

            R.id.log_out -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    @SuppressWarnings("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4) {
            if (resultCode == 1 || resultCode == RESULT_CANCELED) {
                Log.d("dddd","in here")
                fragment.getParties(fragment.locationEngine?.lastLocation)
            }
        }
    }
}

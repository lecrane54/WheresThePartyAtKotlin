package com.example.lefeb.wheresthepartyat.view.activities


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import com.example.lefeb.wheresthepartyat.R
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.io.IOException
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.widget.ImageView
import com.example.lefeb.wheresthepartyat.view.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.*
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_login.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.URL


class LoginActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    private var firestore : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var callbackManager: CallbackManager? = null
    private var ref: StorageReference? = null
    private var name:String? = null
    private var email:String? = null
    private var fbId:String? = null
    private var picUrl : String? = null

    var image: ImageView? = null
    var uid: String? = null
    var context: Context? = null
    private var accessToken: AccessToken? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mAuth = FirebaseAuth.getInstance()
        ref = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()
        callbackManager = CallbackManager.Factory.create()
        var set : MutableList<String> = mutableListOf("public_profile","email")
        image = imageView
        login_button.setReadPermissions(set)
        uid = mAuth!!.uid
        context = this




        login_button.setOnClickListener(View.OnClickListener {

            LoginManager.getInstance().registerCallback(callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {
                            Log.d("MainActivity", "Facebook token: " + loginResult.accessToken.token)

                            accessToken = AccessToken.getCurrentAccessToken()
                            val request = GraphRequest.newMeRequest(accessToken) { `object`, _ ->
                                try {

                                    if (`object`.has("name")) {
                                        name = `object`.getString("name")
                                    }
                                    if (`object`.has("email")) {
                                        email = `object`.getString("email")
                                    }
                                    if (`object`.has("id")) {
                                        fbId = `object`.getString("id")
                                    }
                                    if (`object`.has("picture")) {
                                       picUrl = `object`.getJSONObject("picture").getJSONObject("data").getString("url")
                                    }


                                    handleAccessToken(loginResult.accessToken)
                                    ProfilePhotoAsync(picUrl, uid).execute()

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }


                            val parameters = Bundle()
                            parameters.putString("fields", "id,name,email,picture.type(large)")
                            request.parameters = parameters
                            request.executeAsync()
                        }

                        override fun onCancel() {
                            Log.d("MainActivity", "Facebook onCancel.")

                        }

                        override fun onError(error: FacebookException) {
                            Log.d("MainActivity", "Facebook onError.")

                        }
                    })


        })





    }



    fun handleAccessToken(token:AccessToken){
        Log.d("dddd","token: $token")
        var credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential).addOnSuccessListener{_ ->
            uid = mAuth!!.currentUser!!.uid
            saveUser()
            goToNextActivity()
        }.addOnFailureListener{exception ->
            Log.d("dddd"," $exception")
        }

    }



    fun saveUser(){
        val map = User(fbId!!,name!!,email!!,uid!!).toMap()

        firestore!!.collection("users")
                .add(map as MutableMap<String, Any>)
                .addOnSuccessListener { documentReference ->
                    Log.d("dddd", "DocumentSnapshot written with ID: " + documentReference.id)
                }
                .addOnFailureListener { e ->
                    Log.e("dddd", "Error adding Note document", e)
                }
    }


    fun checkUser(user: FirebaseUser){
        if( user != null){
            goToNextActivity()
        }

    }

    fun goToNextActivity(){
        var i = Intent(context, MainActivity::class.java)
        startActivity(i)
        finish()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        checkUser(firebaseUser!!)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()

    }
}

class ProfilePhotoAsync : AsyncTask<String,String,String>{

    var bitmap: Bitmap? = null
    var url: String? = null
    var uid: String? = null

    constructor(){
        Log.d("dddd","in here")
    }

   constructor(url: String?, uid: String?){
        this.url = url
       this.uid = uid
    }

    override fun doInBackground(vararg params: String?): String {
        bitmap = downloadBitmap(url)
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
       uploadImage()

    }

    private fun downloadBitmap(url: String?) : Bitmap?{

        Log.d("dddd","Downloading pic")

        var bm:Bitmap? = null
        try{
            var options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 1
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis, null, options)
            val stream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.PNG, 0, stream)
            bis.close()
            `is`.close()



        }catch (e:IOException){
            Log.d("dddd",e.localizedMessage)
        }
        return bm
    }

    private fun uploadImage(){
        var storage : FirebaseStorage = FirebaseStorage.getInstance()
        var ref: StorageReference= storage.getReference("userImages/").child(uid+".png")

        var baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 0, baos)
        var data = baos.toByteArray()
        var uploadTask: UploadTask = ref.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->

            Log.d("dddd","Upload successful ${taskSnapshot.bytesTransferred}")

        }.addOnFailureListener(OnFailureListener { exception ->
            Log.d("dddd","Upload sucked: ${exception.message}")
        })
    }


}

package com.example.lefeb.wheresthepartyat.view.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.example.lefeb.wheresthepartyat.R
import com.example.lefeb.wheresthepartyat.view.model.Party
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.jaredrummler.materialspinner.MaterialSpinner
import com.redbooth.WelcomeCoordinatorLayout
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_party_start2.*
import kotlinx.android.synthetic.main.fragment_party_start3.*
import kotlinx.android.synthetic.main.fragment_party_starter1.*
import java.util.*
import com.google.firebase.firestore.CollectionReference
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation


class PartyStater : AppCompatActivity(), View.OnClickListener {


    private lateinit var  coordinatorLayout: WelcomeCoordinatorLayout
    private var partyType : String = "BYOB"
    private var fee :String = "Free"
    private var typeArray: Array<String> = arrayOf("BYOB","Keg","Tailgate","Fraternity","Birthday","Pregame","Other")
    private var feeArray: Array<String> = arrayOf("Free","$5","$10","$20")
    private var partytitle: String? = null
    private var partyTheme: String? = null
    private var partyAddress: String? = null
    private var partyCity: String? = null
    private var age: Int? = null
    private lateinit var geo : GeoPoint
    private var notes: String? = null
    private var time: String? = null
    private lateinit var radioText: String
    private lateinit var feeAdapter: ArrayAdapter<String>
    private lateinit var typeAdapter: ArrayAdapter<String>
    private var isCheck:Boolean = false
    private var maxCharacterCount = 150
    private var hostName = FirebaseAuth.getInstance().currentUser!!.displayName
    private var firestore = FirebaseFirestore.getInstance()
    private lateinit var documentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_stater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        coordinatorLayout = findViewById(R.id.coordinator123)

        coordinatorLayout.addPage(R.layout.fragment_party_starter1,R.layout.fragment_party_start2,R.layout.fragment_party_start3)

        typeAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,typeArray)
        spin2.setAdapter(typeAdapter)
        spin2.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener { _, _, _, item: String -> String
            partyType = item
        })
        feeAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,feeArray)
        spin3.setAdapter(feeAdapter)

        spin3.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener { _, _, _, item: String ->
            fee = item
        })

        complete.setOnClickListener {
            Log.d("dddd","saving party")
            saveParty()
        }

        group.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, checkedId:Int ->
            if(checkedId == R.id.but1){
                try{
                    age = Integer.parseInt(but1.text.toString())
                }catch(n: NumberFormatException){
                    Log.d("dddd",n.message)
                }
            }else{
                try{
                    age = Integer.parseInt(but2.text.toString())

                }catch(n: NumberFormatException){
                    Log.d("dddd",n.message)
                }
            }
        })

        che.addSwitchObserver { _, isChecked: Boolean ->
            if(isChecked){
                isCheck = true
                hideEdits()

            }else{
                isCheck = false
                showEdits()
            }
        }

        edit_note.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateCharacterCount()
            }
        })

    }

    private fun saveParty(){
        val addressCity = address.text.trim().toString() + " " + city.text.trim().toString()
        geo = getLocationFromAddress(addressCity)
        val uid = FirebaseAuth.getInstance().uid


        val party = Party(hostName!!,party_title.text.trim().toString(),partyType,address.text.trim().toString(),edit_note.text.trim().toString(),age!!,fee,geo,uid!!,geo.latitude,geo.longitude)

        val map = party.toMap()

        firestore!!.collection("parties")
                .add(map as MutableMap<String, Any>)
                .addOnSuccessListener { documentReference ->
                    Log.d("dddd", "DocumentSnapshot written with ID: " + documentReference.id)
                    documentId = documentReference.id
                    saveLocation()
                }
                .addOnFailureListener { e ->
                    Log.e("dddd", "Error adding Note document", e)
                    Toasty.error(this,"Something went Wrong",5).show()
                }
    }


    private fun getCompleteAddressString(lat: Double, lng: Double):String{
        var strAdd = ""
        val geocoder = Geocoder(this,Locale.getDefault())

        try{
            val addresses = geocoder.getFromLocation(lat,lng,1)
            if (addresses != null){
                val returnedAddress = addresses.get(0)

                Log.d("dddd",returnedAddress.toString())

                val builder = StringBuilder("")

                builder.append(returnedAddress.getAddressLine(0)).append("\n")

                strAdd = builder.toString()

            }
        }catch (e: Exception){
            Log.d("dddd",e.message)
        }

        return strAdd
    }

    private fun getLocationFromAddress(address1: String) : GeoPoint{
        var lat: Double
        var lng: Double
        var loc: GeoPoint? = null

        var geocoder  = Geocoder(this,Locale.getDefault())
        try{
            val address : MutableList<Address> = geocoder.getFromLocationName(address1,1)

            if(!address.isEmpty()){
                lat = address.get(0).latitude
                lng = address.get(0).longitude
                loc = GeoPoint(lat,lng)
            }
        }catch (e: Exception){
            Log.d("dddd",e.message)
        }
        return loc!!
    }


    private fun updateCharacterCount(){
        val characterCountString:String = String.format(Locale.getDefault(),"%d/%d",edit_note.text.length,maxCharacterCount)
        characterCount.text = characterCountString
    }
    private fun showEdits(){
        city_mat.visibility = View.VISIBLE
        city_mat.isEnabled = true
        city.isEnabled = true
        city.visibility = View.VISIBLE
        address_mat.visibility = View.VISIBLE
        address_mat.isEnabled = true
        address.visibility = View.VISIBLE
        address.isEnabled = true

    }

    private fun hideEdits(){
        city_mat.visibility = View.GONE
        city_mat.isEnabled = false
        city.isEnabled = false
        city.visibility = View.GONE
        address_mat.visibility = View.GONE
        address_mat.isEnabled = false
        address.visibility = View.GONE
        address.isEnabled = false
    }

    private fun saveLocation(){
        val ref = FirebaseFirestore.getInstance().collection("locations")
        val geofire = GeoFire(ref)
        geofire.setLocation(documentId, GeoLocation(geo.latitude,geo.longitude), GeoFire.CompletionListener { _, error ->

            if(error == null){
                Log.d("dddd","saved location")
                val intent : Intent = getIntent()
                setResult(1,intent)


            }else{
                Log.d("dddd","didn't save")

            }

        })
        finish()

    }

    override fun onClick(v: View?) {
        val id = v!!.id

        Log.d("dddd","${id.toString()}  ${R.id.complete}")
        when(id){
            R.id.fab->{
                Log.d("dddd","clicked")
            }
            R.id.complete->{
                Log.d("dddd","Saving party")
                saveParty()
            }
        }

    }
}

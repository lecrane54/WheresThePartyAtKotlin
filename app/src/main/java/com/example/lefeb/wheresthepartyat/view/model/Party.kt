package com.example.lefeb.wheresthepartyat.view.model

import android.graphics.Bitmap
import android.location.Location
import android.provider.Telephony
import android.util.Log
import com.google.firebase.firestore.GeoPoint

class Party{

    var host: String = ""
    var partyTitle: String = ""
    var partyType: String = ""
    var uid: String = ""
    var address: String = ""
    var partyNotes: String = ""
    var ageLimit: Int = 0
    var fee: String = ""
  //  var profileImage: Bitmap = ""
   // var partyPhoto: Bitmap = ""
    var partyLocation: GeoPoint = GeoPoint(0.0,0.0)
    var partyLat: Double = 0.0
    var partyLng: Double = 0.0

    constructor(){

    }


    constructor(host:String,title:String,type:String,address:String,notes:String,age:Int,fee:String,location:GeoPoint,uid: String, lat:Double,lng:Double){
        this.host = host
        this.partyTitle = title
        this.partyType = type
        this.address = address
        this.partyNotes = notes
        this.ageLimit = age
        this.fee = fee
        this.partyLocation = location
        this.uid = uid
        this.partyLat = lat
        this.partyLng = lng
        Log.d("dddd","$this")
    }




    fun toMap(): Map<String, Any> {

        val result = HashMap<String, Any>()
        result.put("host", host)
        result.put("title", partyTitle)
        result.put("location",partyLocation)
        result.put("notes",partyNotes)
        result.put("type", partyType)
        result.put("address", address)
        result.put("age",ageLimit)
        result.put("fee",fee)
        result.put("latitude",partyLat)
        result.put("longitude",partyLng)
       //result.put("name", profileImage)
       // result.put("email", partyPhoto)
        result.put("uid",uid)
        return result
    }
}
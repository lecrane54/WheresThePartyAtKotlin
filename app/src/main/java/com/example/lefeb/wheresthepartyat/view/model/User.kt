package com.example.lefeb.wheresthepartyat.view.model

import android.R.attr.name



class User {

     var name: String? = null
     var email: String? = null
     var fbId: String? = null
    var uid: String? = null


    constructor(){

    }

    constructor(fbId: String, name: String, email: String, uid: String) {
        this.fbId = fbId
        this.name = name
        this.email = email
        this.uid = uid
    }



    fun toMap(): Map<String, Any> {

        val result = HashMap<String, Any>()
        result.put("name", name!!)
        result.put("email", email!!)
        result.put("fbId",fbId!!)
        result.put("uid",uid!!)
        return result
    }
}
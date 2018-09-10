package com.example.lefeb.wheresthepartyat.view.fragments

import android.content.Context
import android.os.Bundle
import android.provider.Telephony
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.lefeb.wheresthepartyat.R
import com.example.lefeb.wheresthepartyat.view.fragments.dummy.DummyContent
import com.example.lefeb.wheresthepartyat.view.fragments.dummy.DummyContent.DummyItem
import com.example.lefeb.wheresthepartyat.view.model.Party
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.nio.channels.MulticastChannel


class PartyListFragment : Fragment() {

    // TODO: Customize parameters
    private val mColumnCount = 1
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mUser  = FirebaseAuth.getInstance().currentUser
    private var mListener: OnFragClick? = null
    private var partyList : MutableList<Party> = getParties()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    fun getParties(): MutableList<Party> {
        val list: MutableList<Party> = mutableListOf()
        db.collection("parties")
                .whereEqualTo("uid", mUser!!.uid)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->


                    if(task.isComplete) {

                        val iterator = task.result.iterator()

                        iterator.forEach {
                            list.add(toParty(it.data))

                        }
                } }
        return list
    }

    private fun toParty(documentSnapshot: MutableMap<String,Any>): Party {
        val title = documentSnapshot["title"].toString()

        val lat: Double = documentSnapshot["latitude"] as Double
        val lng :Double = documentSnapshot["longitude"] as Double
        val host = documentSnapshot["host"].toString()
        val type = documentSnapshot["type"].toString()
        val fee = documentSnapshot["fee"].toString()
        val address = documentSnapshot["address"].toString()
        val notes = documentSnapshot["notes"].toString()
        val uid = documentSnapshot["uid"].toString()
        val loc : GeoPoint = documentSnapshot["location"] as GeoPoint
        val age = documentSnapshot["age"] as Long

        return Party(host,title,type,address,notes,age.toInt(),fee,loc,uid,lat,lng)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_party_list, container, false)
        // Set the adapter


        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            Log.d("dddd","$partyList")
            view.adapter = MyPartyRecyclerViewAdapter(partyList, mListener)
        }
        return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragClick) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragClick {
        // TODO: Update argument type and name
        fun onClick(item: Party)
    }
}

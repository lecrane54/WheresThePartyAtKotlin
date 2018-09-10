package com.example.lefeb.wheresthepartyat.view.fragments

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.lefeb.wheresthepartyat.R

import com.example.lefeb.wheresthepartyat.view.fragments.PartyListFragment.OnFragClick
import com.example.lefeb.wheresthepartyat.view.fragments.dummy.DummyContent.DummyItem
import com.example.lefeb.wheresthepartyat.view.model.Party

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPartyRecyclerViewAdapter(private val mValues: MutableList<Party>, private val mListener: PartyListFragment.OnFragClick?) : RecyclerView.Adapter<MyPartyRecyclerViewAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_party, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val party = mValues[position]

        holder.mItem = party
        holder.mContentView.text = party.partyTitle

        holder.mView.setOnClickListener {
            mListener?.onClick(holder.mItem!!)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView
        var mItem: Party? = null

        init {
            mContentView = mView.findViewById<View>(R.id.content) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}

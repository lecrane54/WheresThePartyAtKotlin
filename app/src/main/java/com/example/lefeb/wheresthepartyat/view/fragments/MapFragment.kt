package com.example.lefeb.wheresthepartyat.view.fragments


import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lefeb.wheresthepartyat.R
import com.example.lefeb.wheresthepartyat.view.model.Party
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import com.koalap.geofirestore.GeoFire
import com.koalap.geofirestore.GeoLocation
import com.koalap.geofirestore.GeoQuery
import com.koalap.geofirestore.GeoQueryEventListener
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.Style
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import kotlinx.android.synthetic.main.map_fragment.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : Fragment(), PermissionsListener,LocationEngineListener {


    private lateinit var mapView: MapView
    private lateinit var map:MapboxMap
    private lateinit var permissionManager: PermissionsManager
    private lateinit var  originLocation: Location
    private var ref: CollectionReference = FirebaseFirestore.getInstance().collection("parties")
    private var geoRef : CollectionReference = FirebaseFirestore.getInstance().collection("locations")
    private var geoFire: GeoFire = GeoFire(geoRef)
    var locationEngine: LocationEngine? = null
    private var locationLayerPlugin: LocationLayerPlugin? = null
    private lateinit var latLngBounds: LatLngBounds



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.map_fragment, container, false)
        mapView = v.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{mapboxMap ->

            map = mapboxMap
            map.setMaxZoomPreference(15.0)
            map.setMinZoomPreference(12.0)
            mapView.setStyleUrl(Style.LIGHT)
           // map.setLatLngBoundsForCameraTarget(latLngBounds)
            enableLocation()


        }


        return v
    }

    private fun enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this.context)){
            initLocationEngine()
            initLocationLayer()
        }else{
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this.activity)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initLocationEngine(){
        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.BALANCED_POWER_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if(lastLocation != null){
            originLocation = lastLocation
            setCameraPosition(lastLocation)
            getParties(lastLocation)
        }else{
            locationEngine?.addLocationEngineListener(this)
        }

    }

    private fun setCameraPosition(location: Location){
        map.easeCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude,location.longitude),12.0) )
    }
    @SuppressWarnings("MissingPermission")
    private fun initLocationLayer(){
        locationLayerPlugin = LocationLayerPlugin(mapView,map,locationEngine)

        locationLayerPlugin?.isLocationLayerEnabled = true

        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL

    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {


    }

     fun getParties(location: Location?){
        var query: GeoQuery = geoFire.queryAtLocation(GeoLocation( location!!.latitude,location.longitude),2.0)
        val list: MutableList<String> = ArrayList()
        query.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                Log.i("dddd", String.format("new party -%s is within range [%f,%f]", key, location.latitude, location.longitude))
                list.add(key)
            }

            override fun onKeyExited(key: String) {
                Log.i("dddd", String.format("Provider %s is no longer in the search area", key))
                list.remove(key)
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                Log.i("dddd", String.format("Provider %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
            }

            override fun onGeoQueryReady() {
                Log.i("dddd", "onGeoQueryReady")
                getListOfParties(list)
            }

            override fun onGeoQueryError(error: Exception) {
                Log.e("dddd", "error: " + error)
            }
        })
    }


    private fun getListOfParties(list: MutableList<String>){
        for(i in list){
            if( i != ""){
                ref.document(i).get().addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                   val party = toParty(documentSnapshot)
                    displayMarkers(party.partyTitle,party.partyLat,party.partyLng, party.partyType)
                })
            }
        }
    }

    private fun toParty(documentSnapshot: DocumentSnapshot):Party{
        val title = documentSnapshot.getString("title")
        val lat = documentSnapshot.getDouble("latitude")
        val lng = documentSnapshot.getDouble("longitude")
        val host = documentSnapshot.getString("host")
        val type = documentSnapshot.getString("type")
        val fee = documentSnapshot.getString("fee")
        val address = documentSnapshot.getString("address")
        val notes = documentSnapshot.getString("notes")
        val uid = documentSnapshot.getString("uid")
        val loc = documentSnapshot.getGeoPoint("location")
        val age = documentSnapshot.getLong("age")
        var a = 0
        if(age is Long){
            a = age.toInt()
        }

        return Party(host!!,title!!,type!!,address!!,notes!!,a,fee!!,loc!!,uid!!,lat!!,lng!!)
    }

    private fun displayMarkers(title: String?, lat: Double?, lng: Double?, type:String?){
        val latLng = LatLng(lat!!,lng!!)
        var icon: Icon? = null
        when(type) {
            "BYOB" -> {
                icon = IconFactory.getInstance(context!!).fromResource(R.drawable.byob)
            }
            "Keg" -> {
                 icon = IconFactory.getInstance(context!!).fromResource(R.drawable.keg)
            }
            "Tailgate" -> {
                 icon = IconFactory.getInstance(context!!).fromResource(R.drawable.tailgate)
            }
            "Fraternity" -> {
                 icon = IconFactory.getInstance(context!!).fromResource(R.drawable.fraternity)
            }
            "Birthday" -> {
                 icon = IconFactory.getInstance(context!!).fromResource(R.drawable.party)
            }
            "Pregame" -> {
                //TODO get pregame icon
            }
            "Other"->{
                 icon = IconFactory.getInstance(context!!).fromResource(R.drawable.other)
            }


        }
        createMarker(latLng,title,icon)
    }

    private fun createMarker(latlng: LatLng, title: String?,icon: Icon?){
        map.addMarker(MarkerOptions()
                .position(latlng)
                .title(title)
                .icon(icon)
        )
    }

    override fun onPermissionResult(granted: Boolean) {
       if(granted){
           enableLocation()
       }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }
    override fun onLocationChanged(location: Location?) {

        location?.let {
            originLocation = location
            setCameraPosition(location)
            getParties(location)
        }

    }
    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }
    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if(PermissionsManager.areLocationPermissionsGranted(this.context)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine?.deactivate()
        mapView.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        locationLayerPlugin?.onStart()
        locationEngine?.removeLocationUpdates()
        mapView.onStop()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(outState != null){
            mapView.onSaveInstanceState(outState)
        }

    }
}

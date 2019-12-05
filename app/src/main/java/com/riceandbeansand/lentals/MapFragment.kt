package com.riceandbeansand.lentals

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MapFragment : Fragment(), OnMapReadyCallback {

    private val REQUEST_CODE = 101
    private var googleMap: GoogleMap? = null
    private val dbsize = 0

    //should i change double to LatLng
    var coll_items: HashMap<String, LatLng> = hashMapOf();
    //each item in array list is a mapping of an item name to an array containing the LAT_LONG coords


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.map, container, false)
        (activity as AppCompatActivity).supportActionBar!!.title = "Map"

        return view

    }

    override fun onStart() {
        super.onStart()

        val mFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction().replace(R.id.fragment_child_container, mFragment).commit()
        mFragment.getMapAsync(this as OnMapReadyCallback)

    }

    private suspend fun fetchLocation(): Location? {
        //can also Task.await(task) instead of success listeners and async
        return suspendCoroutine { cont ->
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
            val task = fusedLocationProviderClient.getLastLocation()
            task.addOnSuccessListener { cont.resume(it) }
            task.addOnFailureListener {cont.resumeWithException(it)}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                        activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        activity!!.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
        } else {
            initializeMap()
        }
    }

    fun initializeMap() {
        GlobalScope.launch(Dispatchers.Main) {
            val currentLocation = fetchLocation()!!
            val latLng = LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
            val db = FirebaseFirestore.getInstance()

            db.collection("items")
                    .get()
                    .addOnSuccessListener { result ->
                        var stringyboi = 0
                        for (document in result) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            stringyboi += 1
                            //definitely did this wrong
                            val latlong = document.get("lat_long") as List<Double>
                            val latlongObj = LatLng(latlong[0], latlong[1])
                            coll_items.put(document.getString("name").toString(), latlongObj)
                        }
                        for (item in coll_items) {
                            //did i call this correctly
                            val markerOptions = MarkerOptions().position(item.value).title(item.key)
                            googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
                            googleMap!!.addMarker(markerOptions)
                        }
                        val markerOptions = MarkerOptions().position(latLng).title( "Available things to borrow: " + stringyboi)
                        googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                        googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
                        googleMap!!.addMarker(markerOptions)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }


        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMap()
                }
                else {
                    //not granted
                }
            }
        }
    }

}

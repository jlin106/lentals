package com.riceandbeansand.lentals

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private val REQUEST_CODE = 101


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
            if (ActivityCompat.checkSelfPermission(
                            activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity!!.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            }
            val task = fusedLocationProviderClient.getLastLocation()
            task.addOnSuccessListener { cont.resume(it) }
            task.addOnFailureListener {cont.resumeWithException(it)}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        GlobalScope.launch(Dispatchers.Main) {
            val currentLocation = fetchLocation()!!
            val latLng = LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
            val markerOptions = MarkerOptions().position(latLng).title("I am here!")
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            googleMap.addMarker(markerOptions)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission not granted
                }
                else {
                    //permission granted
                }
            }
        }
    }

}

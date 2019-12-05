package com.riceandbeansand.lentals

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.media.ThumbnailUtils
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import java.math.BigDecimal
import java.math.RoundingMode

class MapFragment : Fragment(), OnMapReadyCallback {

    private val REQUEST_CODE = 101
    private var googleMap: GoogleMap? = null
    private val dbsize = 0

    //should i change double to LatLng
    var coll_items: HashMap<String, LatLng> = hashMapOf()
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

    fun getDistanceBetween(latLng1: LatLng, latLng2: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(latLng1!!.latitude, latLng1!!.longitude,
                latLng2!!.latitude, latLng2!!.longitude, result)
        val decimal = BigDecimal(result[0].toDouble() / 1609.344).setScale(2, RoundingMode.HALF_EVEN)
        return decimal.toDouble()
    }

    fun addBorder(bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(bmp.width + borderSize * 2, bmp.height + borderSize * 2, bmp.getConfig());
        val canvas = Canvas(bmpWithBorder);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null);
        return bmpWithBorder;
    }

    fun initializeMap() {
        GlobalScope.launch(Dispatchers.Main) {
            val currentLocation = fetchLocation()!!
            val currentLatLng = LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
            val db = FirebaseFirestore.getInstance()

            db.collection("items")
                    .get()
                    .addOnSuccessListener { result ->
                        var numCloseItems = 0
                        for (document in result) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            numCloseItems += 1
                            val latlong = document.get("lat_long") as List<Double>
                            val itemPosition = LatLng(latlong[0], latlong[1])
                            var dist = getDistanceBetween(currentLatLng, itemPosition)
                            getImageFileFromGSUrlWithCache(document.getString("imagePath")!!, activity!!.cacheDir){file ->
                                var imageBitmap = BitmapFactory.decodeFile(file.absolutePath) //should scale this down
                                imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, 100, 100)
                                imageBitmap = addBorder(imageBitmap, 4);
                                val itemImageIcon = BitmapDescriptorFactory.fromBitmap(imageBitmap);
                                val markerOptions = MarkerOptions().position(itemPosition)
                                        .title(document.getString("name"))
                                        .snippet(dist.toString() +  " mi away")
                                        .icon(itemImageIcon)
                                //googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                                //googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                                googleMap!!.addMarker(markerOptions)
                            }

                        }
                        //val markerOptions = MarkerOptions().position(currentLatLng).title( "Available things to borrow: " + numCloseItems)
                       // googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        val soup = LatLng(39.3292205,-76.6189553)
                        googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(soup, 16f))
                        //googleMap!!.addMarker(markerOptions)
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

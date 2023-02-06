package com.example.convoyapp

import android.Manifest
import android.app.Dialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.nio.charset.Charset

class Map: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_end_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim) }
    private var clicked = false
    private lateinit var mapView: MapView
    private var marker: Marker? = null
    var map: GoogleMap? = null

    private lateinit var startButton: ExtendedFloatingActionButton
    private lateinit var joinButton: ExtendedFloatingActionButton
    private lateinit var leaveButton: ExtendedFloatingActionButton
    private lateinit var extendButton:FloatingActionButton
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapping)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        preferences = getSharedPreferences("currentData",MODE_PRIVATE)

        startButton = findViewById(R.id.startButton)
        joinButton = findViewById(R.id.joinButton)
        leaveButton = findViewById(R.id.leaveButton)
        extendButton = findViewById(R.id.extendButton)

        val currentUser = preferences.getString(Const.USERNAME,"not exist")
        val currentSession = preferences.getString(Const.SESSION_KEY,"not exist")

        locationManager = getSystemService(LocationManager::class.java)

        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), 10)
        }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latLng = LatLng(location.latitude, location.longitude)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                if (map != null) {
                    map!!.animateCamera(cameraUpdate)
                    if (marker == null) {
                        map!!.clear()
                        map!!.addMarker(
                            MarkerOptions().position(latLng)
                                .title("My Current Location")
                        )
                    } else {
                        marker!!.position = latLng
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        extendButton.setOnClickListener {
            onExtendButtonClicked()
        }
        startButton.setOnClickListener{
            val convoyPost = Const.ACTION+"="+ Const.CREATE +"&"+
                    Const.USERNAME +"="+currentUser+"&"+
                    Const.SESSION_KEY +"="+ currentSession
            Log.d("convoyPost", convoyPost)

            val dialogBinding = layoutInflater.inflate(R.layout.custom_dialog, null)
            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)
            myDialog.show()

            val queue = Volley.newRequestQueue(this)
            val stringReq : StringRequest =
                object : StringRequest(
                    Method.POST, Const.CONVOY_API,
                    Response.Listener { response ->
                        // response
                        val strResp = JSONObject(response)
                        Log.d("API", strResp.toString())

                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            val currentConvoyId = preferences.edit()
                            currentConvoyId.putString(Const.CONVOYID, strResp.getString(Const.CONVOYID))
                            currentConvoyId.apply()
                            if(savedInstanceState == null) { // initial transaction should be wrapped like this
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainerView, MessageFragment.newInstance("Convoy Key",preferences.getString(Const.CONVOYID,"not exist").toString()))
                                    .commitAllowingStateLoss()
                            }
                        }
                        else if(strResp.getString(Const.STATUS).equals(Const.ERROR)){
                            Toast.makeText(this, strResp.getString("message"), Toast.LENGTH_SHORT).show()
                            Log.d("Error",strResp.getString("message"))
                        }

                    },
                    Response.ErrorListener { error ->
                        Log.d("API", "error => $error")
                    }
                )
                {
                    override fun getBody(): ByteArray {
                        return convoyPost.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)

        }
        leaveButton.setOnClickListener {
            val convoyPost = Const.ACTION+"="+ Const.END +"&"+
                    Const.USERNAME +"="+currentUser+"&"+
                    Const.SESSION_KEY +"="+ currentSession +"&"+
                    Const.CONVOYID +"="+preferences.getString(Const.CONVOYID,"not exist")
            val queue = Volley.newRequestQueue(this)
            val stringReq : StringRequest =
                object : StringRequest(
                    Method.POST, Const.CONVOY_API,
                    Response.Listener { response ->
                        // response
                        val strResp = JSONObject(response)
                        Log.d("API", strResp.toString())

                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            Toast.makeText(this,"End Convoy",Toast.LENGTH_SHORT).show()
                        }
                        else if(strResp.getString(Const.STATUS).equals(Const.ERROR)){
                            Toast.makeText(this, strResp.getString("message"), Toast.LENGTH_SHORT).show()
                            Log.d("Error",strResp.getString("message"))
                        }

                    },
                    Response.ErrorListener { error ->
                        Log.d("API", "error => $error")
                    }
                )
                {
                    override fun getBody(): ByteArray {
                        return convoyPost.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }

        joinButton.setOnClickListener {
            val convoyPost = Const.ACTION+"="+ Const.QUERY +"&"+
                    Const.USERNAME +"="+currentUser+"&"+
                    Const.SESSION_KEY +"="+ currentSession
            Log.d("convoyPost", convoyPost)
            val queue = Volley.newRequestQueue(this)
            val stringReq : StringRequest =
                object : StringRequest(
                    Method.POST, Const.CONVOY_API,
                    Response.Listener { response ->
                        // response
                        val strResp = JSONObject(response)
                        Log.d("API", strResp.toString())

                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            val currentConvoyId = preferences.edit()
                            currentConvoyId.putString(Const.CONVOYID, strResp.getString(Const.CONVOYID))
                            currentConvoyId.apply()
                            Log.d("convoyId", preferences.getString(Const.CONVOYID,"not exist").toString())
                        }
                        else if(strResp.getString(Const.STATUS).equals(Const.ERROR)){
                            Toast.makeText(this, strResp.getString("message"), Toast.LENGTH_SHORT).show()
                            Log.d("Error",strResp.getString("message"))
                        }

                    },
                    Response.ErrorListener { error ->
                        Log.d("API", "error => $error")
                    }
                )
                {
                    override fun getBody(): ByteArray {
                        return convoyPost.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }

        findViewById<View>(R.id.updateButton).setOnClickListener {

            if (checkCallingOrSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, 1f,
                locationListener
            )
        }
    }
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
    private fun onExtendButtonClicked(){
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }
    private fun setAnimation(clicked: Boolean){
        if(!clicked){
            startButton.visibility =  View.VISIBLE
            joinButton.visibility = View.VISIBLE
            leaveButton.visibility = View.VISIBLE
        }
        else{
            startButton.visibility =  View.INVISIBLE
            joinButton.visibility = View.INVISIBLE
            leaveButton.visibility = View.INVISIBLE
        }
    }
    private fun setVisibility(clicked: Boolean){
        if(!clicked){
            startButton.startAnimation(fromBottom)
            joinButton.startAnimation(fromBottom)
            leaveButton.startAnimation(fromBottom)
            extendButton.startAnimation(rotateOpen)
        }
        else{
            startButton.startAnimation(toBottom)
            joinButton.startAnimation(toBottom)
            leaveButton.startAnimation(toBottom)
            extendButton.startAnimation(rotateClose)
        }
    }
    private fun setClickable(clicked: Boolean){
        if(!clicked){
            startButton.isClickable = true
            joinButton.isClickable = true
            leaveButton.isClickable = true
        }
        else{
            startButton.isClickable = false
            joinButton.isClickable = false
            leaveButton.isClickable = false
        }
    }

}



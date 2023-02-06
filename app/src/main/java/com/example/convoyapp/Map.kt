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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.nio.charset.Charset

class Map: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var mapView: MapView
    private var marker: Marker? = null
    var map: GoogleMap? = null
    private lateinit var startButton: FloatingActionButton
    private lateinit var joinButton: FloatingActionButton
    private lateinit var leaveButton: FloatingActionButton
    private lateinit var preferences: SharedPreferences
    private lateinit var dialog: Dialog
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
                        var strResp = JSONObject(response)
                        Log.d("API", strResp.toString())

                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            val currentConvoyId = preferences.edit()
                            currentConvoyId.putString(Const.CONVOYID, strResp.getString(Const.CONVOYID))
                            currentConvoyId.apply()
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
                        var strResp = JSONObject(response)
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
                        var strResp = JSONObject(response)
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
    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title("My Location")
        marker = map?.addMarker(markerOptions)
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
}



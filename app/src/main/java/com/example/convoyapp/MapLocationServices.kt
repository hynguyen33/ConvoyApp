package com.example.convoyapp

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresApi

import com.google.android.gms.maps.model.LatLng



class MapLocationServices: Service() {
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val CHANNEL_ID = "ForegroundService Kotlin"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(LocationManager::class.java)
        createNotificationChannel()
        val notificationIntent = Intent(this, MapLocationServices::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent,  PendingIntent.FLAG_IMMUTABLE
        )
        val channel = createNotificationChannel()

        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Convoy App Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        startForeground(1, notification)
        locationListener = LocationListener { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            var notificationContent = latLng.latitude.toString() + "," + latLng.longitude.toString()
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(notificationContent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
            Log.d("location",  notificationContent)
        }

        checkUserLocation()
        return START_NOT_STICKY
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun checkUserLocation(){
        if (checkCallingOrSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000, 10f,
            locationListener
        )
    }
}
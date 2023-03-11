package edu.temple.convoy

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), DashboardFragment.DashboardInterface, ConvoyFragment.ConvoyControlInterface {

    private var serviceIntent: Intent? = null
    private val convoyViewModel : ConvoyViewModel by lazy {
        ViewModelProvider(this)[ConvoyViewModel::class.java]
    }

    // Update ViewModel with location data whenever received from LocationService
    private var locationHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            convoyViewModel.setLocation(msg.obj as LatLng)
        }
    }

    // Receiver to get data of convoy participants from FCM
    private val convoyBroadcastReceiver = object: BroadcastReceiver () {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val participantArray = JSONArray(p1!!.getStringExtra(FCMService.UPDATE_KEY))
            val convoy = Convoy()
            var participantObject: JSONObject
            for (i in 0 until participantArray.length()) {
                participantObject = participantArray.getJSONObject(i)
                convoy.addParticipant(
                    Participant(
                        participantObject.getString("username"),
                        LatLng(
                            participantObject.getDouble("latitude"),
                            participantObject.getDouble("longitude")
                        )
                    )
                )
            }

            convoyViewModel.setConvoy(convoy)
        }
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

            // Provide service with handler
            (iBinder as LocationService.LocationBinder).setHandler(locationHandler)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        serviceIntent = Intent(this, LocationService::class.java)

        convoyViewModel.getConvoyId().observe(this) {
            if (!it.isNullOrEmpty())
                supportActionBar?.title = "GRPR - $it"
            else
                supportActionBar?.title = "GRPR"
        }

        Helper.user.getConvoyId(this)?.run {
            convoyViewModel.setConvoyId(this)
            startLocationService()
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(convoyBroadcastReceiver, IntentFilter(FCMService.UPDATE_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(convoyBroadcastReceiver)
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("default", "Active Convoy", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun createConvoy() {
        Helper.api.createConvoy(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
            override fun processResponse(response: JSONObject) {
                if (Helper.api.isSuccess(response)) {
                    convoyViewModel.setConvoyId(response.getString("convoy_id"))
                    Helper.user.saveConvoyId(this@MainActivity, convoyViewModel.getConvoyId().value!!)
                    startLocationService()
                } else {
                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    override fun endConvoy() {
        AlertDialog.Builder(this).setTitle("Close Convoy")
            .setMessage("Are you sure you want to close the convoy?")
            .setPositiveButton("Yes"
            ) { _, _ -> Helper.api.closeConvoy(
                this,
                Helper.user.get(this),
                Helper.user.getSessionKey(this)!!,
                convoyViewModel.getConvoyId().value!!,
                object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            convoyViewModel.setConvoyId("")
                            Helper.user.clearConvoyId(this@MainActivity)
                            stopLocationService()
                        } else
                            Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                    }

                }
            )}
            .setNegativeButton("Cancel") { p0, _ -> p0.cancel() }
            .show()
    }

    override fun joinConvoy() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_convoyFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", true)
            })
    }

    override fun leaveConvoy() {
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_convoyFragment, Bundle().apply {
                putBoolean("JOIN_ACTION", false)
            })
    }

    override fun logout() {
        Helper.user.clearSessionData(this)
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_loginFragment)
    }

    private fun startLocationService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

    override fun joinConvoyFlow(convoyId: String) {
        Helper.api.joinConvoy(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            convoyId,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.saveConvoyId(this@MainActivity, convoyId)
                    startLocationService()
                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

    override fun leaveConvoyFlow(convoyId: String) {
        Helper.api.leaveConvoy(
            this,
            Helper.user.get(this),
            Helper.user.getSessionKey(this)!!,
            Helper.user.getConvoyId(this)!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.user.clearConvoyId(this@MainActivity)
                    stopLocationService()

                    // Refresh action bar menu items
                    invalidateOptionsMenu()
                }

            }
        )
    }

}
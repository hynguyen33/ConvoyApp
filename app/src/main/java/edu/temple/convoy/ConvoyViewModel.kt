package edu.temple.convoy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

// A single View Model is used to store all data we want to retain
// and/or observe
class ConvoyViewModel : ViewModel() {

    // This property will store all users displayed in the map
    // and is not observed; as such it is not a LiveData
    private val participants by lazy {
        Convoy()
    }

    private val location by lazy {
        MutableLiveData<LatLng>()
    }

    private val convoyId by lazy {
        MutableLiveData<String>()
    }

    private val convoy by lazy {
        MutableLiveData<Convoy>()
    }

    fun setConvoyId(id: String) {
        convoyId.value = id
    }

    fun setLocation(latLng: LatLng) {
        location.value = latLng
    }

    fun getLocation(): LiveData<LatLng> {
        return location
    }

    fun getConvoyId(): LiveData<String> {
        return convoyId
    }

    fun setConvoy(convoy: Convoy) {
        participants.updateConvoy(convoy)

        // Inform observers that a new convoy was provided
        this.convoy.value = convoy
    }

    // LiveData to observe
    fun getConvoyToObserve(): LiveData<Convoy> {
        return convoy
    }

    // Actual data
    fun getConvoy(): Convoy {
        return participants
    }

}
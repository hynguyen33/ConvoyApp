package com.example.convoyapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel: ViewModel() {
    private val userData: MutableLiveData<UserInfo>? by lazy {
        MutableLiveData()
    }
    fun getUserData(): LiveData<UserInfo>?{
        return userData
    }
    fun setUserData(userData: UserInfo){
        this.userData?.value =userData
    }
}

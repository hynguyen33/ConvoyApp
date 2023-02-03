package com.example.convoyapp

class DataList {
    private val dataList : ArrayList<UserInfo> by lazy {
        ArrayList()
    }
    fun add(userInfo: UserInfo) {
        dataList.add(userInfo)
    }
    fun remove (userInfo: UserInfo) {
        dataList.remove(userInfo)
    }

    operator fun get(index: Int) = dataList[index]

    fun size() = dataList.size
}
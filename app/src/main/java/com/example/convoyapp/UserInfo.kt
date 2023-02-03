package com.example.convoyapp
import org.json.JSONObject

const val LASTNAME = "lastname"
const val FIRSTNAME = "firstname"
const val USERNAME = "username"
const val PASSWORD = "password"
const val ACTION = "action"
const val SESSION_KEY = "session_key"
data class UserInfo (val lastname: String,val firstname: String , val username: String,val password:String, val action: String , val session_key: String){

    constructor(userData: JSONObject): this(
        userData.getString(USERNAME),
        userData.getString(LASTNAME),
        userData.getString(FIRSTNAME),
        userData.getString(PASSWORD),
        userData.getString(ACTION),
        userData.getString(SESSION_KEY)
    )
}

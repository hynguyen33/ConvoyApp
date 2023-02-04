package com.example.convoyapp
import org.json.JSONObject

object Const{
    const val LASTNAME = "lastname"
    const val FIRSTNAME = "firstname"
    const val USERNAME = "username"
    const val PASSWORD = "password"
    const val ACTION = "action"
    const val SESSION_KEY = "session_key"
    const val REGISTER = "REGISTER"
    const val LOGIN = "LOGIN"
    const val LOGOUT = "LOGOUT"
    const val STATUS = "status"
    const val SUCCESS = "SUCCESS"
    const val ERROR = "ERROR"
    const val ACCOUNT = "https://kamorris.com/lab/convoy/account.php"
}
data class UserInfo (val username: String,val firstname: String,val lastname: String){

    constructor(userData: JSONObject): this(
        userData.getString(Const.USERNAME),
        userData.getString(Const.FIRSTNAME),
        userData.getString(Const.LASTNAME)
    )

}

package com.example.convoyapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var userName: EditText
    private lateinit var password: EditText
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = getSharedPreferences("currentData",MODE_PRIVATE)

        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        userName = findViewById(R.id.signInUserName)
        password = findViewById(R.id.signInPassword)
        signUpButton.setOnClickListener{
            val intent = Intent(this@MainActivity,Register::class.java)
            startActivity(intent)
        }
        signInButton.setOnClickListener{

            val signPost = Const.ACTION+"="+ Const.LOGIN +"&"+
                    Const.USERNAME +"="+userName.text.toString()+"&"+
                    Const.PASSWORD +"="+ password.text.toString()

            Log.d("SignInPost",signPost)
            val queue = Volley.newRequestQueue(this)
            val stringReq : StringRequest =
                object : StringRequest(
                    Method.POST, Const.ACCOUNT,
                    Response.Listener { response ->
                        // response
                        var strResp = JSONObject(response)
                        Log.d("API", strResp.toString())

                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            Log.d("Status",Const.STATUS)
                            val currentUserName = preferences.edit()
                            currentUserName.putString(Const.USERNAME,userName.text.toString())
                            currentUserName.apply()
                            val currentSessionKey = preferences.edit()
                            currentSessionKey.putString(Const.SESSION_KEY, strResp.getString(Const.SESSION_KEY))
                            currentSessionKey.apply()
                            Log.d("CurrentUser",preferences.getString(Const.USERNAME,"DEFAULT") + preferences.getString(Const.SESSION_KEY,"DEFAULT"))
                            val user = preferences.getString(Const.USERNAME,"DEFAULT")
                            val ses = preferences.getString(Const.SESSION_KEY,"DEFAULT")
                            Toast.makeText(this, user + ses , Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity,Map::class.java)
                            startActivity(intent)

                        }else if(strResp.getString(Const.STATUS).equals(Const.ERROR)){
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
                        return signPost.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }
    }
}
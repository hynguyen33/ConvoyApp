package com.example.convoyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import java.io.FileOutputStream
import java.nio.charset.Charset

class Register: AppCompatActivity() {
    private lateinit var signUpLastName: EditText
    private lateinit var signUpFistName: EditText
    private lateinit var signUpUserName: EditText
    private lateinit var signUpPassword: EditText
    private lateinit var signUpConfirmPassword: EditText
    private lateinit var submitButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)
        signUpLastName = findViewById(R.id.signUplastName)
        signUpFistName = findViewById(R.id.signUpfirstName)
        signUpUserName = findViewById(R.id.signUpuserName)
        signUpPassword = findViewById(R.id.signUppassword)
        signUpConfirmPassword = findViewById(R.id.signUpConfirmPassword)
        submitButton = findViewById(R.id.submitSignUp)


        submitButton.setOnClickListener {
//            if(signUpPassword.toString() == signUpConfirmPassword.toString()){
//
//            }
            val postInfo = Const.ACTION+"="+ Const.REGISTER +"&"+ Const.USERNAME +"="+signUpUserName.text.toString()+"&"+
                    Const.FIRSTNAME+"="+ signUpFistName.text.toString()+"&" + Const.LASTNAME +"="+ signUpLastName.text.toString()+"&"+
                    Const.PASSWORD +"="+ signUpPassword.text.toString()
            Log.d("PostInfo", postInfo)
            Log.d("Json",getSignUpInfo().toString())
            val queue = Volley.newRequestQueue(this)
            val stringReq : StringRequest =
                object : StringRequest(Method.POST, Const.ACCOUNT,
                    Response.Listener { response ->
                        // response
                        var strResp = JSONObject(response)
                        Log.d("API", strResp.toString())
                        if(strResp.getString(Const.STATUS).equals(Const.SUCCESS)){
                            Log.d("Status",Const.STATUS)
                            Toast.makeText(this, strResp.getString("session_key"), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Register,MainActivity::class.java)
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
                        return postInfo.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)

        }
    }
    fun getSignUpInfo(): JSONObject{
        var jObj = JSONObject()
        try{
            jObj.put(Const.ACTION,Const.REGISTER)
            jObj.put(Const.USERNAME,signUpUserName.text.toString())
            jObj.put(Const.FIRSTNAME,signUpFistName.text.toString())
            jObj.put(Const.LASTNAME,signUpLastName.text.toString())
            jObj.put(Const.PASSWORD,signUpPassword.text.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val subset = JSONObject(jObj, arrayOf(Const.USERNAME,Const.FIRSTNAME,Const.LASTNAME))
        storedData(subset)
        return jObj
    }
    fun storedData(subset:JSONObject){
        val gson = Gson()
        val userList: String = gson.toJson(subset)
        val fileOutputStream: FileOutputStream
        val file = "userData"
        try{
            fileOutputStream = openFileOutput(file, Context.MODE_APPEND)
            fileOutputStream.write(userList.toByteArray())
        }
        catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }
}
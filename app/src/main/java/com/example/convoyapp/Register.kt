package com.example.convoyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

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
        submitButton.setOnClickListener{
        }
    }
    fun getInfo(){

    }

}
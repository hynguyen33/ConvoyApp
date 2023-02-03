package com.example.convoyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private val controlURL = "https://kamorris.com/lab/convoy/convoy.php"
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var userName: EditText
    private lateinit var password: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        userName = findViewById(R.id.signInUserName)
        password = findViewById(R.id.signInPassword)
        signUpButton.setOnClickListener{
            val intent = Intent(this@MainActivity,Register::class.java)
            startActivity(intent)
        }
        signInButton.setOnClickListener{
        }
    }
}
package com.example.studentregistration

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.ComponentActivity

class Home : ComponentActivity() {

    private lateinit var register: Button
    private lateinit var profile: Button
    private var firstImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.themecolor) // Change to your color resource
        }

        // Retrieve the registration number passed from Faceregistration1
        val registrationNo = intent.getStringExtra("registrationnumber")

        val password = intent.getStringExtra("password")

        val token = intent.getStringExtra("auth_token")

        val firstImageUriString = intent.getStringExtra("firstImageUri")
        firstImageUri = firstImageUriString?.let { Uri.parse(it) }  // Convert the string back to a Uri


        // Use the registration number as needed
        Log.d("HomeActivity", "Received registration number: $registrationNo")

        register = findViewById(R.id.register)
        profile = findViewById(R.id.profile)

        register.setOnClickListener {
            // Check permissions
            val intent = Intent(this, Registrationform2::class.java)
            startActivity(intent)
        }
        profile.setOnClickListener {
            // Create an Intent to start the Profile activity
            val intent = Intent(this, Profile::class.java)

            // Pass the registration number as an extra
            intent.putExtra("registrationnumber", registrationNo)

            intent.putExtra("password", password)

            intent.putExtra("auth_token", token)


            // Pass the first image URI as an extra
            firstImageUri?.let {
                intent.putExtra("firstImageUri", it.toString())
            }

            startActivity(intent)
        }

    }
}
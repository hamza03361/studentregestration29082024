package com.example.studentregistration

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.studentregistration.Apis.MyApis
import com.example.studentregistration.Apis.RetrofitClient
import com.example.studentregistration.Apis.Updatepassword
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

class Updatepassword : ComponentActivity() {

    private lateinit var smallImageViewContainer: FrameLayout
    private lateinit var cancelbutton: Button
    private lateinit var nextbutton: Button
    private lateinit var confirmpassword: EditText
    private lateinit var currentpassword: EditText
    private lateinit var newpassword: EditText

    private lateinit var eyeIcon: ImageView
    private var isPasswordVisible = false
    private lateinit var eyeIconn: ImageView
    private lateinit var eyeIconnn: ImageView
    private lateinit var nestedScrollView: NestedScrollView

    private lateinit var animationView: LottieAnimationView

    private lateinit var blurView: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.updatepassword)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor)
        }


        animationView = findViewById(R.id.animationView)
        //for blurview
        blurView = findViewById(R.id.blurview)

        // Retrieve the registration number and password passed from Profile
        val registrationNo = intent.getStringExtra("registrationnumber")
        val password = intent.getStringExtra("password")

        confirmpassword = findViewById(R.id.confirmpasswordedittext)
        currentpassword = findViewById(R.id.currentpasswordedittext)
        newpassword = findViewById(R.id.passwordedittext)


        smallImageViewContainer = findViewById(R.id.smallImageViewContainer)
        cancelbutton = findViewById(R.id.cancelbutton)
        nextbutton = findViewById(R.id.nextbutton)

        eyeIcon = findViewById(R.id.eyeIcon)
        eyeIconn = findViewById(R.id.eyeIconn)
        eyeIconnn = findViewById(R.id.eyeIconnn)

        smallImageViewContainer.setOnClickListener {
            onBackPressed()
        }

        cancelbutton.setOnClickListener {
            onBackPressed()
        }

        nextbutton.setOnClickListener {

            animationView.visibility = View.VISIBLE
            disableInputs()

            val enteredCurrentPassword = currentpassword.text.toString().trim()
            val newPassword = confirmpassword.text.toString().trim()

            // Check if the entered current password matches the passed password
            if (enteredCurrentPassword != password) {
                Toast.makeText(this, "Wrong current password", Toast.LENGTH_SHORT).show()
                enableInputs()
                animationView.visibility = View.GONE

            } else if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show()
                enableInputs()
                animationView.visibility = View.GONE

            } else if (registrationNo == null) {
                Toast.makeText(this, "Registration number is missing", Toast.LENGTH_SHORT).show()
                enableInputs()
                animationView.visibility = View.GONE

            } else {
                // Proceed to update the password
                updatePassword(newPassword, enteredCurrentPassword, registrationNo)
            }
        }

        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                currentpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_open) // Set icon to 'closed eye'
            } else {
                // Show password
                currentpassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_icon) // Set icon to 'open eye'
            }
            isPasswordVisible = !isPasswordVisible
            // Move cursor to the end of the text
            currentpassword.setSelection(currentpassword.text.length)
        }

        eyeIconn.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                newpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_open) // Set icon to 'closed eye'
            } else {
                // Show password
                newpassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_icon) // Set icon to 'open eye'
            }
            isPasswordVisible = !isPasswordVisible
            // Move cursor to the end of the text
            newpassword.setSelection(newpassword.text.length)
        }

        eyeIconnn.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                confirmpassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_open) // Set icon to 'closed eye'
            } else {
                // Show password
                confirmpassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_icon) // Set icon to 'open eye'
            }
            isPasswordVisible = !isPasswordVisible
            // Move cursor to the end of the text
            confirmpassword.setSelection(confirmpassword.text.length)
        }

        nestedScrollView=findViewById(R.id.nestedscrollview)
        nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            nestedScrollView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = nestedScrollView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open, scroll to the Next button
                nestedScrollView.scrollTo(0, nextbutton.bottom)
            } else {
                // Keyboard is closed, handle any other actions if necessary
            }
        }

    }

    private fun updatePassword(newPassword: String, currentPassword: String, registrationNo: String) {
        lifecycleScope.launch {
            try {
                // Create a JSON request body with the new password and current password
                val jsonBody = """
                {
                    "currentPassword": "$currentPassword",
                    "newPassword": "$newPassword"
                }
                """.trimIndent()

                Log.d("UpdatepasswordActivity", "Request Body: $jsonBody")

                val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

                // Logging interceptor to capture the request and response body
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                // Configure the OkHttpClient
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor) // Add logging
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()
                            .header("Content-Type", "application/json") // Ensure Content-Type header is set
                            .method(original.method, original.body)
                        val request = requestBuilder.build()
                        chain.proceed(request)
                    }
                    .build()

                // Create a Retrofit instance using the OkHttpClient
                val BASE_URLLLs = "https://sync-matic-live.vercel.app/api/student/"
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URLLLs)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val apiService = retrofit.create(MyApis::class.java)

                // Make the API call to update the password using the registration number
                val response: Response<Updatepassword> = apiService.newpassword(registrationNo, requestBody)

                if (response.isSuccessful) {
                    // Handle successful response
                    Toast.makeText(this@Updatepassword, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    animationView.visibility = View.GONE
                    enableInputs()
                } else {
                    // Handle error response
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        Log.e("UpdatepasswordActivity", "Failed to update password: $errorBody")
                        animationView.visibility = View.GONE
                        enableInputs()
                    } else {
                        Log.e("UpdatepasswordActivity", "Failed to update password: Unknown error")
                        animationView.visibility = View.GONE
                        enableInputs()
                    }
                    Toast.makeText(this@Updatepassword, "Failed to update password", Toast.LENGTH_SHORT).show()
                    animationView.visibility = View.GONE
                    enableInputs()
                }
            } catch (e: Exception) {
                // Handle network or other errors
                Log.e("UpdatepasswordActivity", "Error updating password", e)
                Toast.makeText(this@Updatepassword, "Error updating password", Toast.LENGTH_SHORT).show()
                animationView.visibility = View.GONE
                enableInputs()
            }
        }
    }

    fun disableInputs() {
        confirmpassword .apply {
            isEnabled = false
        }
        currentpassword .apply {
            isEnabled = false
        }
        newpassword .apply {
            isEnabled = false
        }
    }

    fun enableInputs() {
        confirmpassword .apply {
            isEnabled = true
        }
        currentpassword .apply {
            isEnabled = true
        }
        newpassword .apply {
            isEnabled = true
        }
    }


}

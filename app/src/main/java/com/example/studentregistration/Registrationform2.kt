package com.example.studentregistration

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import org.json.JSONObject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.graphics.Canvas
import android.text.InputType
import androidx.core.widget.NestedScrollView
import com.example.studentregistration.Apis.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import com.airbnb.lottie.LottieAnimationView
import okhttp3.RequestBody.Companion.toRequestBody

class Registrationform2 : ComponentActivity() {

    private lateinit var cancelbutton: Button
    private lateinit var registerbutton: Button
    private lateinit var registrationnumberEditText: EditText
    private lateinit var passwordedittext: EditText
    private lateinit var blurView: ConstraintLayout
    private lateinit var nestedScrollView: NestedScrollView
    private val loginApi = RetrofitClient.loginApi

    private lateinit var eyeIcon: ImageView
    private var isPasswordVisible = false

    private val gson = Gson()

    private lateinit var animationView: LottieAnimationView
    private var capturedImagePaths: ArrayList<String>? = null

    private val uploadRes = RetrofitClient.uploadResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrationform2)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor)
        }


        //for animition
        animationView = findViewById(R.id.animationView)
        //for blurview
        blurView = findViewById(R.id.blurview)


        // Retrieve captured images from the Intent
        capturedImagePaths = intent.getStringArrayListExtra("capturedImagePaths") ?: arrayListOf()


        passwordedittext = findViewById(R.id.passwordedittext)

        registrationnumberEditText = findViewById(R.id.registrationnumberedittext)

        registerbutton = findViewById(R.id.nextbutton)



        eyeIcon = findViewById(R.id.eyeIcon)

        // Set click listener on eye icon to toggle password visibility
        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                passwordedittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_open) // Set icon to 'closed eye'
            } else {
                // Show password
                passwordedittext.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_icon) // Set icon to 'open eye'
            }
            isPasswordVisible = !isPasswordVisible
            // Move cursor to the end of the text
            passwordedittext.setSelection(passwordedittext.text.length)
        }

        // Inside the registerbutton.setOnClickListener

        registerbutton.setOnClickListener {

            animationView.visibility = View.VISIBLE
            disableInputs()
            blurView(blurView, this)

            val registrationNumber = registrationnumberEditText.text.toString().trim()
            val password = passwordedittext.text.toString().trim()

            // Validate inputs
            if (registrationNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both registration number and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a JSON object for the request body
            val jsonObject = JSONObject().apply {
                put("reg_no", registrationNumber)
                put("password", password)
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

            // Use a coroutine to make the API call
            lifecycleScope.launch {
                try {
                    val response = loginApi.login(requestBody)

                    if (response.isSuccessful) {
                        // Parse the response body to extract the token
                        val loginData = response.body()

                        // Save the token in SharedPreferences
                        loginData?.let {
                            val token = it.token
                            saveToken(token)

                            Log.d(TAG, "Token: $token")
                            Toast.makeText(this@Registrationform2, "Login successful", Toast.LENGTH_LONG).show()

                            // Proceed to the next step (pass data to the next activity)
                            passdata()
                        }
                    } else {
                        // Handle error responses
                        handleErrorResponse(response.code()) {
                            Toast.makeText(this@Registrationform2, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    // Handle exceptions (e.g., network errors)
                    Toast.makeText(this@Registrationform2, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    removeBlur(blurView)
                    animationView.visibility = View.GONE
                    enableInputs()
                }
            }

        }





        cancelbutton = findViewById(R.id.cancelbutton)
        cancelbutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        nestedScrollView=findViewById(R.id.nestedscrollview)
        nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            nestedScrollView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = nestedScrollView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open, scroll to the Next button
                nestedScrollView.scrollTo(0, registerbutton.bottom)
            } else {
                // Keyboard is closed, handle any other actions if necessary
            }
        }
    }

    private fun saveToken(token: Any) {

            val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("auth_token", token.toString()).apply()

    }

    fun disableInputs() {
        registrationnumberEditText.apply {
            isEnabled = false
        }
    }

    fun enableInputs() {
        registrationnumberEditText.apply {
            isEnabled = true
        }
    }

    private fun passdata() {
        val token = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("auth_token", null)

        val intent = Intent(this, Faceregistration1::class.java)
        intent.putExtra("registrationnumber", registrationnumberEditText.text.toString())
        intent.putExtra("password", passwordedittext.text.toString())
        intent.putExtra("auth_token", token)  // Pass the token here
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        navigatetomainactivity()
    }

    private fun navigatetomainactivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun removeBlur(view: View) {
        // Check if the parent is a FrameLayout
        val parent = view.parent as? FrameLayout
            ?: throw IllegalStateException("The parent of the view must be a FrameLayout")

        // Find the blurred ImageView that was added as an overlay
        val blurredImageView = parent.getChildAt(parent.childCount - 1)

        // Remove the blurred ImageView
        parent.removeView(blurredImageView)
    }

    fun blurView(view: View, context: Context) {
        // Create a Bitmap with the same dimensions as the view
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas and draw the view onto the Bitmap
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // Initialize RenderScript
        val renderScript = RenderScript.create(context)

        // Create Allocation objects for input and output
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createTyped(renderScript, input.type)

        // Create the blur script
        val blurScript = ScriptIntrinsicBlur.create(renderScript, input.element)

        // Set the blur radius (0 < radius <= 25)
        blurScript.setRadius(10f)

        // Set the input for the blur script
        blurScript.setInput(input)

        // Execute the script and copy the result into the output allocation
        blurScript.forEach(output)
        output.copyTo(bitmap)

        // Create an ImageView and set the blurred bitmap as its image
        val blurredImageView = ImageView(context)
        blurredImageView.setImageBitmap(bitmap)

        // Check if the parent is a FrameLayout
        val parent = view.parent as? FrameLayout
            ?: throw IllegalStateException("The parent of the view must be a FrameLayout")

        // Add the ImageView as an overlay to the original view
        parent.addView(blurredImageView)

        // Set the layout parameters of the ImageView to match the original view
        blurredImageView.layoutParams = view.layoutParams
        blurredImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        blurredImageView.translationX = view.translationX
        blurredImageView.translationY = view.translationY
    }

    private fun handleErrorResponse(code: Int, callback: (String) -> Unit) {
        when (code) {
            401 -> callback.invoke("Invalid email or password")
            404 -> callback.invoke("User not found")
            else -> callback.invoke("Unknown Error")
        }
    }
    companion object {
        private const val TAG = "RegistrationForm2"
    }
}

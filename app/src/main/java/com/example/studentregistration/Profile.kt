package com.example.studentregistration

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.studentregistration.Apis.MonitoringLog
import com.example.studentregistration.Apis.RetrofitClient.getprofileData
import com.example.studentregistration.Apis.StudentProfile
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class Profile : ComponentActivity() {

    private lateinit var viewModel: ProfileViewModel

    // UI elements to display the profile data
    private lateinit var usernameTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var registrationNumberTextView: TextView
    private lateinit var programTextView: TextView
    private lateinit var profileImageView: CircleImageView

    private lateinit var smallImageViewContainer: FrameLayout
    private lateinit var forgetpasswordButton: Button

    private lateinit var registrationNoo :  String


    private lateinit var blurView: ConstraintLayout

    // Spinners for filtering by month and year
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    private var firstImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)


        // Set the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor)
        }



        //for blurview
        blurView = findViewById(R.id.blurview)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)


        // Get the registration number, password, and token from intent extras
        val registrationNo = intent.getStringExtra("registrationnumber")
        val password = intent.getStringExtra("password")
        val token = intent.getStringExtra("auth_token")

        if (registrationNo != null) {
            registrationNoo = registrationNo
        }


        val firstImageUriString = intent.getStringExtra("firstImageUri")
        firstImageUri = firstImageUriString?.let { Uri.parse(it) }  // Convert the string back to a Uri

        // Initialize the UI elements
        usernameTextView = findViewById(R.id.username)
        userIdTextView = findViewById(R.id.userid)
        registrationNumberTextView = findViewById(R.id.userregistrationnumber)
        programTextView = findViewById(R.id.program)
        profileImageView = findViewById(R.id.profile)

        monthSpinner = findViewById(R.id.monthSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)

        // Attach OnItemSelectedListener to monthSpinner
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Call function when item is selected
                reloadProfileData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle if nothing is selected, if needed
            }
        }

        // Attach OnItemSelectedListener to yearSpinner
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Call function when item is selected
                reloadProfileData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle if nothing is selected, if needed
            }
        }

        smallImageViewContainer = findViewById(R.id.smallImageViewContainer)
        forgetpasswordButton = findViewById(R.id.updatepassword)

        // Restore the instance state if available
        savedInstanceState?.let {
            usernameTextView.text = it.getString("username") ?: "N/A"
            userIdTextView.text = it.getString("userId") ?: "N/A"
            registrationNumberTextView.text = it.getString("registrationNumber") ?: "N/A"
            programTextView.text = it.getString("program") ?: "N/A"
        }


        // Check ViewModel and fetch profile data
        registrationNo?.let {
            registrationNumberTextView.text = it
            if (viewModel.studentProfile != null) {
                updateUI(viewModel.studentProfile!!)
            } else {
                fetchProfileData(it)
                fetchProfileDataa(it)
            }
        }

        // Set up click listeners
        forgetpasswordButton.setOnClickListener {
            val intent = Intent(this, Updatepassword::class.java)
            intent.putExtra("password", password)
            intent.putExtra("auth_token", token)
            intent.putExtra("registrationnumber", registrationNo)
            startActivity(intent)
        }

        smallImageViewContainer.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }


    }


    private fun reloadProfileData() {

        // Get the selected values from the spinners
        val selectedMonth = monthSpinner.selectedItem.toString()
        val selectedYear = yearSpinner.selectedItem.toString()

        // Log or use the selected month and year as needed
        Log.d("ProfileActivity", "Selected Month: $selectedMonth, Selected Year: $selectedYear")
        registrationNoo?.let { fetchProfileDataaa(it, selectedMonth, selectedYear) }
    }

    private fun fetchProfileDataaa(registrationNo: String, month: String, year: String) {

        // Convert the month string to its corresponding number
        val monthNumber = getMonthNumber(month)

        blurView(blurView, this)

        lifecycleScope.launch {
            try {
                // Make the API call to get the monitoring logs for the specific registration number
                val response = getprofileData.getStudenttime(registrationNo)

                if (response.isSuccessful) {
                    // Retrieve the monitoring logs from the response
                    val profileResponse = response.body()

                    if (profileResponse != null) {
                        // Filter the monitoring logs based on the month and year
                        val filteredLogs = profileResponse.monitoringLogs.filter { log ->
                            val logDateParts =
                                log.date?.split("/") // Split the date into [MM, DD, YYYY]
                            val logMonth = logDateParts?.get(0) // Get the month part
                            val logYear = logDateParts?.get(2) // Get the year part

                            logMonth == monthNumber && logYear == year // Compare with input month and year
                        }

                        Log.d("ProfileActivity", "Filtered Monitoring Logs for $monthNumber/$year: $filteredLogs")
                        setupRecyclerView(filteredLogs)
                        removeBlur(blurView)
                    } else {
                        Log.e("ProfileActivity", "Monitoring logs not found for registration number: $registrationNo")
                        removeBlur(blurView)
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to fetch monitoring logs: ${response.errorBody()?.string()}")
                    removeBlur(blurView)
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error fetching monitoring logs", e)
                removeBlur(blurView)
            }
        }
    }

    private fun getMonthNumber(monthName: String): String {
        val monthMap = mapOf(
            "January" to "01",
            "February" to "02",
            "March" to "03",
            "April" to "04",
            "May" to "05",
            "June" to "06",
            "July" to "07",
            "August" to "08",
            "September" to "09",
            "October" to "10",
            "November" to "11",
            "December" to "12"
        )
        return monthMap[monthName] ?: "01" // Default to January if month is not found
    }



    private fun fetchProfileData(registrationNo: String) {

        blurView(blurView, this)

        lifecycleScope.launch {
            try {
                // Make the API call to get the profile for the specific registration number
                val response = getprofileData.getStudentProfile(registrationNo)

                if (response.isSuccessful) {
                    // Retrieve the student profile directly from the wrapped response
                    val profileResponse = response.body()

                    profileResponse?.studentProfile?.let { profile ->
                        viewModel.studentProfile = profile // Store data in ViewModel
                        updateUI(profile)
                    }

                    if (profileResponse != null) {
                        val profile = profileResponse.studentProfile
                        Log.d("ProfileActivity", "Profile data: $profile")
                        updateUI(profile)
                        removeBlur(blurView)
                    } else {
                        Log.e("ProfileActivity", "Profile not found for registration number: $registrationNo")
                        removeBlur(blurView)
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to fetch profile data: ${response.errorBody()?.string()}")
                    removeBlur(blurView)
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error fetching profile data", e)
                removeBlur(blurView)
            }
        }
    }

    private fun fetchProfileDataa(registrationNo: String) {

        blurView(blurView, this)

        lifecycleScope.launch {
            try {
                // Make the API call to get the monitoring logs for the specific registration number
                val response = getprofileData.getStudenttime(registrationNo)

                if (response.isSuccessful) {
                    // Retrieve the monitoring logs from the response
                    val profileResponse = response.body()

                    if (profileResponse != null) {
                        val monitoringLogs = profileResponse.monitoringLogs
                        Log.d("ProfileActivity", "Monitoring Logs: $monitoringLogs")
                        setupRecyclerView(monitoringLogs)
                        removeBlur(blurView)
                    } else {
                        Log.e("ProfileActivity", "Monitoring logs not found for registration number: $registrationNo")
                        removeBlur(blurView)
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to fetch monitoring logs: ${response.errorBody()?.string()}")
                    removeBlur(blurView)
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error fetching monitoring logs", e)
                removeBlur(blurView)
            }
        }
    }

    private fun updateUI(profile: StudentProfile) {

        // Concatenate first name and last name
        val fullName = "${profile.firstName ?: ""} ${profile.lastName ?: ""}".trim()

        // Update the TextView with the full name
        usernameTextView.text = if (fullName.isNotEmpty()) fullName else "N/A"

        userIdTextView.text = profile.id ?: "N/A"
        registrationNumberTextView.text = profile.registrationNo ?: "N/A"
        programTextView.text = profile.department ?: "N/A"

        profile.imageUrls?.firstOrNull()?.let { imageUrl ->
            Glide.with(this).load(imageUrl).into(profileImageView)
        }
    }

    private fun setupRecyclerView(timings: List<MonitoringLog>) {
        val recyclerView = findViewById<RecyclerView>(R.id.pointsTableRecyclerView)

        // Set the LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set the Adapter
        val adapter = InOutTimeAdapter(timings)
        recyclerView.adapter = adapter
    }

    data class Timing(
        val date: String,
        val start_time: String,
        val end_time: String
    )


    fun blurView(view: View, context: Context) {
        // Ensure the view has been measured before creating the bitmap
        view.post {
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
            blurredImageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            // Check if the parent is a ViewGroup (more general)
            val parent = view.parent as? ViewGroup ?: throw IllegalStateException("The parent of the view must be a ViewGroup")

// If the parent is a FrameLayout, perform your logic
            if (parent is FrameLayout) {
                parent.addView(blurredImageView)
                blurredImageView.bringToFront()
            } else {
               // throw IllegalStateException("Expected FrameLayout as parent, but found ${parent::class.simpleName}")
            }

        }
    }

    fun removeBlur(view: View) {
        // Check if the parent is a FrameLayout
        val parent = view.parent as? FrameLayout
            ?: throw IllegalStateException("The parent of the view must be a FrameLayout")

        // Remove all ImageViews (blurs) from the parent
        val blurredImageViews = parent.children.filterIsInstance<ImageView>()
        blurredImageViews.forEach { parent.removeView(it) }
    }

}

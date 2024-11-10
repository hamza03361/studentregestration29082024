package com.example.studentregistration

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Size
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import java.io.File
import androidx.activity.ComponentActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.studentregistration.Apis.RetrofitClient
import com.example.studentregistration.Registrationform2.Companion
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Faceregistration1 : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var firstpreview: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var smallImageViewContainer: FrameLayout
    private lateinit var progressBar: ProgressBar
    private val capturedImagePaths = mutableListOf<String>()
    private var imageCapture: ImageCapture? = null
    private var countdownTimer: CountDownTimer? = null
    private lateinit var countdownTextView: TextView
    private var isDialogShowing = false
    private var capturedImagePathss: ArrayList<String>? = null
    private var image = false
    private var i = 0
    private var x = 0
    private var y = 0
    private var u = 0
    private var j = 0
    private var n = 0
    var currentInstructionIndex = 0
    private lateinit var animationView: LottieAnimationView
    private val gson = Gson()
    private val uploadRes = RetrofitClient.uploadResponse

    private var firstImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.faceregistration1)

        previewView = findViewById(R.id.previewView)
        firstpreview = findViewById(R.id.firstpreview)
        progressBar = findViewById(R.id.progressBar)
        countdownTextView = findViewById(R.id.countdownTextView)

        animationView = findViewById(R.id.animationView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = getColor(R.color.secondthemecolor) // Change to your color resource
        }

        smallImageViewContainer = findViewById(R.id.smallImageViewContainer)
        smallImageViewContainer.setOnClickListener {
            val intent = Intent(this, Registrationform2::class.java)
            startActivity(intent)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        // Start the countdown timer
        startCountdownTimer()
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Set up the preview for display in firstpreview
            val firstPreview = Preview.Builder()
                .setTargetResolution(Size(900, 900))
                .build()
                .also {
                    val view = findViewById<PreviewView>(R.id.firstpreview)
                    it.setSurfaceProvider(view.surfaceProvider)
                }

            // Set up the smaller preview for display in previewView (250x250)
            val secondPreview = Preview.Builder()
                .setTargetResolution(Size(250, 250))
                .build()
                .also {
                    val view = findViewById<PreviewView>(R.id.previewView)
                    it.setSurfaceProvider(view.surfaceProvider)
                }

            // Set up the image capture for the smaller preview
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(250, 250))  // Match the preview size
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setJpegQuality(100)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            // Bind the lifecycle to the camera with both previews and the image capture use case
            val camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, firstPreview, secondPreview, imageCapture
            )

            // Enable auto-focus and tap-to-focus (if needed)
            enableAutoFocusAndTapToFocus(camera)
        }, ContextCompat.getMainExecutor(this))
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun enableAutoFocusAndTapToFocus(camera: Camera) {
        val cameraControl = camera.cameraControl
        val cameraInfo = camera.cameraInfo

        // Enable auto-focus
        cameraControl.startFocusAndMetering(
            FocusMeteringAction.Builder(
                SurfaceOrientedMeteringPointFactory(1f, 1f)
                    .createPoint(0.5f, 0.5f)
            ).build()
        )

        // Enable tap-to-focus
        previewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point).build()
                cameraControl.startFocusAndMetering(action)
                true
            } else {
                false
            }
        }
    }

    private fun capturePhoto(filename: String) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImagePaths.add(file.absolutePath)

                    // Resize the captured image to 250x250 if necessary
                    resizeImageTo250x250(file.absolutePath) { resizedFile ->
                        // Correct image orientation and show dialog with the resized image
                        correctImageOrientation(resizedFile.absolutePath) { correctedBitmap ->
                            showImagePreviewDialog(correctedBitmap, resizedFile.absolutePath, object : DialogDismissListener {
                                override fun onDialogDismissed() {
                                    isDialogShowing = false
                                    // Restart the countdown timer
                                    if (capturedImagePaths.size < 5) {
                                        startCountdownTimer()
                                    }
                                    else
                                    {
                                        uploadData()
                                    }
                                }
                            })
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun resizeImageTo250x250(imagePath: String, callback: (File) -> Unit) {
        val originalBitmap = BitmapFactory.decodeFile(imagePath)

        // Get the original image's EXIF orientation
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        // Resize the bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 250, 250, true)

        // Rotate the bitmap to correct orientation
        val rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(resizedBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(resizedBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(resizedBitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(resizedBitmap, horizontal = true, vertical = false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(resizedBitmap, horizontal = false, vertical = true)
            else -> resizedBitmap
        }

        // Save the corrected bitmap to the file
        val resizedFile = File(imagePath)
        FileOutputStream(resizedFile).use { outputStream ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        callback(resizedFile)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) postScale(-1f, 1f)
            if (vertical) postScale(1f, -1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun correctImageOrientation(imagePath: String, callback: (Bitmap) -> Unit) {
        val bitmapOptions = BitmapFactory.Options().apply {
            inSampleSize = 1  // Adjust this based on your image quality and memory considerations
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions)
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()

        // Apply rotation based on EXIF data
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)
        }

        // Create a new bitmap with the correct orientation
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Apply horizontal flip if needed (for front-facing camera images)
        val correctedBitmap = if (isFrontFacingCamera()) {
            val flipMatrix = Matrix().apply { preScale(-1.0f, 1.0f) }
            Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.width, rotatedBitmap.height, flipMatrix, true)
        } else {
            rotatedBitmap
        }

        callback(correctedBitmap)  // Pass the corrected bitmap to the callback
    }

    private fun isFrontFacingCamera(): Boolean {
        // Implement your logic here to check if the camera is front-facing
        // This can be based on the CameraSelector or any other method you use to determine the camera type
        return true  // Assuming it's front-facing for this example
    }

    interface DialogDismissListener {
        fun onDialogDismissed()
    }

    private fun showImagePreviewDialog(bitmap: Bitmap, filename: String, listener: DialogDismissListener) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_image_preview, null)
        val imageView: ImageView = dialogView.findViewById(R.id.imageView)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonSave)
        val buttonDelete: Button = dialogView.findViewById(R.id.buttonDelete)

        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setImageBitmap(bitmap)  // Set bitmap here

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.setOnDismissListener {
            listener.onDialogDismissed()
        }

        dialog.show()
        isDialogShowing = true

        buttonSave.setOnClickListener {
            saveBitmapToGallery(bitmap, filename)
            updateProgressBar()
            dialog.dismiss()
        }

        buttonDelete.setOnClickListener {
            deleteImage(filename)
            dialog.dismiss()
        }
    }

    private fun updateProgressBar() {
        val progress = (capturedImagePaths.size * 20) // Assuming each image is 20% of progress
        progressBar.progress = progress
    }

    private fun deleteImage(filename: String) {
        capturedImagePaths.remove(filename)
        val file = File(filename)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, filename: String) {

        n++


        image=true
        if (x==1){
            y++
        }
        if (y==1){
            u++
        }
        if (u==1){
            j++
        }
        x++

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Faceregistration")
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
            uploadData()
        }

        if (n == 1) {
            firstImageUri = uri  // Store the URI of the first saved image
        }

    }


    private fun uploadData() {

        if (capturedImagePaths.size == 5) {

            animationView.visibility = View.VISIBLE

            capturedImagePathss = ArrayList(capturedImagePaths)

            val registrationNo = intent.getStringExtra("registrationnumber") ?: ""

            if (capturedImagePathss.isNullOrEmpty()) {
                Log.e(Faceregistration1.TAG, "No images to upload.")
                return
            }

            printDataAsJson() // Print data as JSON before uploading

            val capturedImages = convertPathsToMultipartBodyParts(capturedImagePathss!!)

            if (isInternetAvailable()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = uploadRes.uploadData(
                            registrationNo, // Passing registration number as path parameter
                            capturedImages
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string()
                                // Inflate the custom layout
                                val inflater = LayoutInflater.from(this@Faceregistration1)
                                val layout: View = inflater.inflate(R.layout.custom_toast, null)

                                // Find the TextView and set the message
                                val textView = layout.findViewById<TextView>(R.id.toast_message)
                                showSuccessDialog()

                                // Create and show the Toast
                                val toast = Toast(applicationContext)
                                toast.duration = Toast.LENGTH_SHORT
                                toast.view = layout
                                toast.show()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e(Faceregistration1.TAG, "Server returned error: $errorBody")

                                // Check if the specific error message is returned by the server
                                if (errorBody?.contains("Failed to create student record: 400: Student already exists in the student data collection") == true) {
                                    showSuccessDialog() // Call showSuccessDialog if this specific error occurs
                                }

                                // Inflate the custom layout
                                val inflater = LayoutInflater.from(this@Faceregistration1)
                                val layout: View = inflater.inflate(R.layout.custom_toast, null)

                                // Find the TextView and set the message
                                val textView = layout.findViewById<TextView>(R.id.toast_message)
                               /* textView.text = "Failed to create student"
                                // Create and show the Toast
                                val toast = Toast(applicationContext)
                                toast.duration = Toast.LENGTH_SHORT
                                toast.view = layout
                                toast.show()*/
                            }
                            animationView.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e(Faceregistration1.TAG, "Data upload failed: ${e.message}", e)
                            // Inflate the custom layout
                            val inflater = LayoutInflater.from(this@Faceregistration1)
                            val layout: View = inflater.inflate(R.layout.custom_toast, null)

                            // Find the TextView and set the message
                            val textView = layout.findViewById<TextView>(R.id.toast_message)
                            textView.text = "Data upload failed"

                            // Create and show the Toast
                            val toast = Toast(applicationContext)
                            toast.duration = Toast.LENGTH_SHORT
                            toast.view = layout
                            toast.show()

                            animationView.visibility = View.GONE

                        }
                    }
                }
            } else {
                Log.e(Faceregistration1.TAG, "No internet connection")
                animationView.visibility = View.GONE
            }
        }
    }

    private fun printDataAsJson() {
        // Create an instance of RegistrationData
        val registrationNo = intent.getStringExtra("registrationnumber") ?: ""
        val capturedImagePaths = capturedImagePathss ?: arrayListOf()

        // Create an instance of RegistrationData
        val registrationData = RegistrationData(
            registrationNo = registrationNo,
            capturedImagePaths = capturedImagePaths
        )

        // Convert the data to JSON
        val json = gson.toJson(registrationData)

        // Print the formatted JSON string
        Log.d(Faceregistration1.TAG, "Registration Data in JSON format: $json")
    }

    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setCancelable(false)

        val alert = builder.create()

        // Set the button click listener
        val okayButton = dialogView.findViewById<Button>(R.id.okayButton)
        okayButton.setOnClickListener {
            alert.dismiss() // Dismiss the alert dialog

            // Retrieve the registration number from the intent
            val registrationNo = intent.getStringExtra("registrationnumber") ?: ""

            val password = intent.getStringExtra("password") ?: ""
            // Create an Intent to start the Home activity

            val intent = Intent(this, Home::class.java)

            // Pass the registration number as an extra
            intent.putExtra("registrationnumber", registrationNo)
            intent.putExtra("password", password)


            // Pass the URI of the first saved image as an extra (if it exists)
            firstImageUri?.let {
                intent.putExtra("firstImageUri", it.toString())  // Convert URI to string before passing
            }

            // Start the Home activity
            startActivity(intent)

            // Close the current activity
            finish()
        }


        alert.show()
    }

    private fun convertPathsToMultipartBodyParts(paths: List<String>): List<MultipartBody.Part> {
        return paths.mapNotNull { path ->
            val file = File(path)
            if (file.exists()) {
                val fileName = file.name
                val sanitizedFileName = sanitizeFileName(fileName)
                Log.d(Faceregistration1.TAG, "Sanitized file name: $sanitizedFileName")
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                val requestFile = file.asRequestBody(mediaType)
                MultipartBody.Part.createFormData("images", sanitizedFileName, requestFile)
            } else {
                Log.e(Faceregistration1.TAG, "File not found: $path")
                null
            }
        }
    }

    private fun startCountdownTimer() {

         i++


        val instructions = listOf(
            "  ",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards",
            "Look straight into the camera",
            "Tilt your face to the left",
            "Tilt your face to the right",
            "Tilt your face upwards",
            "Tilt your face downwards"
        )


        val instructionCount = instructions.size

        fun showNextInstruction() {
            if (currentInstructionIndex >= instructionCount) {
                countdownTextView.text = "Done"
                return
            }

            countdownTextView.text = instructions[currentInstructionIndex] // Display the current instruction

            countdownTimer = object : CountDownTimer(3000, 1000) { // 3 seconds countdown for each instruction
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    countdownTextView.text = "${instructions[currentInstructionIndex]} ($secondsRemaining seconds)"
                }

                override fun onFinish() {
                    countdownTextView.text = "Capturing photo..."

                    // Capture the photo
                    if (!isDialogShowing) {
                        capturePhoto("captured_${System.currentTimeMillis()}.jpg")
                    }
                }
            }.start()
        }
        if (i==1 || image==true && x==1 || image==true && y==1 || image==true && u==1 || image==true && j==1) {

            currentInstructionIndex++

            if (x==1){
                x=0
            }
        }
        showNextInstruction()
    }


    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        cameraExecutor.shutdown()
    }

    // Function to sanitize filenames
    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {
        private const val TAG = "Faceregistration1"
    }

    data class RegistrationData(
        val registrationNo: String,
        val capturedImagePaths: ArrayList<String>
    )
}

package com.example.studentregistration.Apis

import com.google.gson.annotations.SerializedName
import java.io.File

data class LoginData(
    @SerializedName("reg_no") val reg_no: String,
    @SerializedName("password") val userPassword: String,
    @SerializedName("token") val token: String,
    )

data class Updatepassword(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)


data class Timing(
    @SerializedName("start_time") val start_time: String,
    @SerializedName("end_time") val end_time: String,
    @SerializedName("institute_id") val institute_id: String,
    )

data class UploadFile(
    val name: String,
    val type: String,
    val content: String
)

data class StudentProfileResponse(
    @SerializedName("studentData") val studentProfile: StudentProfile
)

data class StudentinandoutResponse(
    @SerializedName("monitoringLogs") val monitoringLogs: List<MonitoringLog>
)



data class StudentProfile(
    @SerializedName("_id") val id: String?,
    @SerializedName("First_name") val firstName: String?,
    @SerializedName("Last_name") val lastName: String?,
    @SerializedName("Father_name") val fatherName: String?,
    @SerializedName("Email") val email: String?,
    @SerializedName("Gender") val gender: String?,
    @SerializedName("Date_of_birth") val dateOfBirth: String?,
    @SerializedName("Registration_No") val registrationNo: String?,
    @SerializedName("Contact_No") val contactNo: String?,
    @SerializedName("Batch") val batch: String?,
    @SerializedName("Address") val address: String?,
    @SerializedName("Semester") val semester: String?,
    @SerializedName("Department") val department: String?,
    @SerializedName("Enrollment_year") val enrollmentYear: String?,
    @SerializedName("institute_id") val instituteId: String?,
    @SerializedName("isVerified") val isVerified: Boolean?,
    @SerializedName("image_urls") val imageUrls: List<String>?
)


data class MonitoringLog(
    val id: String,
    @SerializedName("Registration_No") val registrationNo: String,
    @SerializedName("entry_status") val entryStatus: String?,
    val date: String?,
    @SerializedName("time_in") val timeIn: String?,
    @SerializedName("time_out") val timeOut: String?,
    @SerializedName("mac_address") val macAddress: String?,
    @SerializedName("camera_location") val cameraLocation: String?
)



data class UploadResponse(
    @SerializedName("Registration_No") val Registration_No: String,
    @SerializedName("images") val images: List<UploadFile>,
)


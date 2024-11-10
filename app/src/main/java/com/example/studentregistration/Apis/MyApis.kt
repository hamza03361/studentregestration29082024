package com.example.studentregistration.Apis

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MyApis {

    @POST("auth/signin")
    @Headers("Content-Type: application/json") // Add content type header
    suspend fun login(@Body user: RequestBody): Response<LoginData>

    @GET("get-data/{registrationNo}")
    suspend fun getStudentProfile(@Path("registrationNo") registrationNo: String): Response<StudentProfileResponse>

    @GET("get-data/{registrationNo}")
    suspend fun getStudenttime(@Path("registrationNo") registrationNo: String): Response<StudentinandoutResponse>


    @PUT("auth/update-password/{registrationNo}")
    @Headers("Content-Type: application/json")
    suspend fun newpassword(
            @Path("registrationNo") registrationNo: String,
            @Body user: RequestBody
        ): Response<Updatepassword>




    @Multipart
    @POST("students/{Registration_No}")
    suspend fun uploadData(
        @Path("Registration_No") registrationNo: String,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>



}

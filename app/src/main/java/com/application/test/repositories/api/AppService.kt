package com.application.test.repositories.api

import com.application.test.models.OSRMRouteResponse
import retrofit2.Call
import retrofit2.http.*


interface AppService {

    @GET("route/v1/driving/{coordinates}")
    fun getDirections(
        @Path("coordinates") coordinates: String
    ): Call<OSRMRouteResponse>

}
package com.application.test.repositories

import com.application.test.models.OSRMRouteResponse
import com.application.test.repositories.api.AppService
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hessam.R on 7/25/23 - 4:20 PM
 */
@Singleton
class MainActivityRepository @Inject internal constructor( private val appService: AppService) {

    fun getDirections(sourceLat: Double, sourceLng: Double, destLat: Double, destLng: Double): Call<OSRMRouteResponse> {
        val coordinates = "$sourceLng,$sourceLat;$destLng,$destLat"
        return appService.getDirections(coordinates)
    }

}

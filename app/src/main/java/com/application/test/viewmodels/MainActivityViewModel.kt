package com.application.test.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.application.test.models.OSRMRouteResponse
import com.application.test.models.Route
import com.application.test.repositories.MainActivityRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Hessam.R on 7/25/23 - 4:20 PM
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(private val mainActivityRepository: MainActivityRepository) :
    ViewModel() {

    val directionsResult: MutableLiveData<List<LatLng>> = MutableLiveData()

    fun fetchRouteAndDisplayPolyline(from: LatLng, destination: LatLng) {
        mainActivityRepository.getDirections(from.latitude, from.longitude, destination.latitude, destination.longitude)
            .enqueue(object : Callback<OSRMRouteResponse> {
                override fun onResponse(call: Call<OSRMRouteResponse>, response: Response<OSRMRouteResponse>) {
                    if (response.isSuccessful) {
                        val routeObject = response.body()
                        val mRoute: Route = routeObject!!.routes[0]
                            val decodedPolyline = PolyUtil.decode(mRoute.geometry)
                            directionsResult.postValue(decodedPolyline)
                    } else {
                        directionsResult.postValue(emptyList())
                    }
                }

                override fun onFailure(call: Call<OSRMRouteResponse>, t: Throwable) {
                    directionsResult.postValue(emptyList())
                }
            })
    }


    fun calculateBearing(currentLatLng: LatLng, nextLatLng: LatLng): Float {
        val dLon = nextLatLng.longitude - currentLatLng.longitude
        val y = sin(dLon) * cos(nextLatLng.latitude)
        val x = cos(currentLatLng.latitude) * sin(nextLatLng.latitude) -
                sin(currentLatLng.latitude) * cos(nextLatLng.latitude) * cos(dLon)
        val bearing = (Math.toDegrees(atan2(y, x)) + 360) % 360
        return bearing.toFloat()
    }

//    private fun calculateTimeInterval(speedKmPerHour: Double): Long {
//        val speedMetersPerMs = speedKmPerHour * 1000.0 / 3600.0
//        return (1000.0 / speedMetersPerMs).toLong()
//    }
}
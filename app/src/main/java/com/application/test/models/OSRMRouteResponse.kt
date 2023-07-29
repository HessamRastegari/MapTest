package com.application.test.models

/**
 * Created by Hessam.R on 7/29/23 - 7:47 AM
 */
data class OSRMRouteResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: String,
    val duration: Double,
    val distance: Double
)
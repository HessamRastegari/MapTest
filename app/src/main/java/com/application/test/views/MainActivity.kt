package com.application.test.views

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.application.test.R
import dagger.hilt.android.AndroidEntryPoint
import com.application.test.bases.BaseActivity
import com.application.test.databinding.ActivityMainBinding
import com.application.test.viewmodels.MainActivityViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.GeoApiContext

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityViewModel>(MainActivityViewModel::class.java),
    OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMap: GoogleMap
    private var fromMarker: MarkerOptions? = null
    private var destinationMarker: MarkerOptions? = null
    private var fromMarkerOnMap: Marker? = null
    private var destinationMarkerOnMap: Marker? = null
    private lateinit var geoApiContext: GeoApiContext
    private var polyline: Polyline? = null
    private var currentLatLngIndex = 0
    private lateinit var myResponse: List<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@MainActivity)

            startButton.setOnClickListener {
                if (myResponse.isNotEmpty()) {
                    startMovingOnRoute(myResponse)
                    setCaption(getString(R.string.caption_enjoy_your_ride))
                }
            }
        }


        geoApiContext = GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setOnMapLongClickListener { latLng ->
            if (fromMarker == null) {
                fromMarker = MarkerOptions().position(latLng).title("From")
                fromMarkerOnMap = fromMarker?.let {
                    googleMap.addMarker(it)
                }
                setCaption(getString(R.string.caption_please_select_your_destination))

            } else if(destinationMarker == null) {
                destinationMarker = MarkerOptions().position(latLng).title("Destination")
                destinationMarkerOnMap = destinationMarker?.let {
                    googleMap.addMarker(it)
                }
                setCaption(getString(R.string.caption_please_wait))
                fetchRouteAndDisplayPolyline()
            }else{
                polyline?.remove()
                fromMarkerOnMap?.remove()
                destinationMarkerOnMap?.remove()
                fromMarker = null
                destinationMarker = null
                fromMarker = MarkerOptions().position(latLng).title("From")
                fromMarkerOnMap = fromMarker?.let {
                    googleMap.addMarker(it)
                }
                setCaption(getString(R.string.caption_please_select_your_destination))
                binding.apply {
                    lottieAnimationView.visibility = View.VISIBLE
                    startButton.visibility = View.GONE
                }
            }
        }

        val defaultLocation = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    private fun fetchRouteAndDisplayPolyline() {
        val fromLatLng = fromMarker?.position
        val destinationLatLng = destinationMarker?.position

        if (fromLatLng != null && destinationLatLng != null) {
            binding.apply {
                lottieAnimationView.visibility = View.GONE
                startButton.visibility = View.VISIBLE
                setCaption(getString(R.string.caption_here_is_your_route))
            }

            viewModel.fetchRouteAndDisplayPolyline(fromLatLng, destinationLatLng)
            viewModel.directionsResult.observe(this) { response ->
                if (response != null) {
                    myResponse = response
                    drawPolyline(response)
                } else {
                    Toast.makeText(this, "Failed to fetch directions.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please set 'From' and 'Destination' markers.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun drawPolyline(response: List<LatLng>?) {
        if (response.isNullOrEmpty()) {
            Toast.makeText(this, "Failed to fetch directions.", Toast.LENGTH_SHORT).show()
            return
        }

        if (polyline != null) {
            polyline?.remove()
        }

        val polylineOptions = PolylineOptions()
            .addAll(response)
            .color(Color.BLUE)
            .width(10f)

        polyline = googleMap.addPolyline(polylineOptions)

        val builder = LatLngBounds.builder()
        for (latLng in response) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }




    private fun startMovingOnRoute(decodedPolyline: List<LatLng>) {
        val handler = Handler(Looper.getMainLooper())
        currentLatLngIndex = 0
        binding.startButton.visibility = View.GONE
        val runnable = object : Runnable {
            override fun run() {
                if (currentLatLngIndex < decodedPolyline.size - 1) {
                    val currentLatLng = decodedPolyline[currentLatLngIndex]
                    val nextLatLng = decodedPolyline[currentLatLngIndex + 1]
                    val bearing = viewModel.calculateBearing(currentLatLng, nextLatLng)

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                        .target(currentLatLng)
                        .bearing(bearing)
                        .tilt(30f)
                        .zoom(15f)
                        .build()))

                    currentLatLngIndex++

                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable)

        googleMap.setOnCameraIdleListener {
            binding.startButton.visibility = View.VISIBLE
            currentLatLngIndex = 0
        }
    }


    private fun setCaption(caption: String){
        binding.apply {
            tvState.animate()
                .alpha(0f)
                .setDuration(500) // Set the duration of the fade-out animation (in milliseconds)
                .withEndAction {
                    // Change the text and set alpha to 1 (fully visible)
                    tvState.text = caption
                    tvState.alpha = 1f

                    // Fade in the new text
                    tvState.animate()
                        .alpha(1f).duration = 500 // Set the duration of the fade-in animation (in milliseconds)
                }
        }

    }



    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    companion object {
        private const val API_KEY = "AIzaSyDJTtF4R8sHUam7puKPxwFGRwPhyWS9Qug"
    }
}


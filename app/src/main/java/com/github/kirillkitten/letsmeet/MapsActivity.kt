package com.github.kirillkitten.letsmeet

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var map: GoogleMap? = null

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var location: Location? = null
        set(value) {
            if (value == null) return
            val latitude = value.latitude
            val longitude = value.longitude
            val bearing = value.bearing

            val position = LatLng(latitude, longitude)
            if (userMarker == null) {
                map?.addMarker(
                    MarkerOptions()
                        .position(position)
                        .rotation(bearing)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.round_navigation_white_24))
                ).also { userMarker = it }
            } else {
                userMarker?.position = position
                userMarker?.rotation = bearing
            }

            field = value
        }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 33
            fastestInterval = 16
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                location = locationResult?.lastLocation
            }
        }
    }

    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestLocationPermission()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map = googleMap
        moveToUser()
    }

    private fun moveToUser() {
        val latitude = location?.latitude ?: return
        val longitude = location?.longitude ?: return

        val position = LatLng(latitude, longitude)
//        map?.addMarker(MarkerOptions().position(position).title("User"))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17f))
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                TODO("Show explanation")
            } else {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST)
            }
        } else {
            fetchUserLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation()
            } else {
                TODO("Handle permission deny")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocation() {
        locationClient.lastLocation.addOnSuccessListener {
            location = it
            moveToUser()
        }

        locationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 228
    }
}

package com.example.qiblaway2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.qiblaway2.R
import com.example.qiblaway2.databinding.FragmentQiblaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.Locale
import kotlin.math.*

class Qibla : Fragment(), SensorEventListener {
    private lateinit var binding: FragmentQiblaBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Sensor values
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Location and Qibla
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var qiblaBearing: Float? = null
    private var currentAzimuth: Float = 0f
    private var isLocationSet = false

    // Mecca coordinates
    private val MECCA_LATITUDE = 21.4225
    private val MECCA_LONGITUDE = 39.8262

    // Animation
    private var lastCompassRotation = 0f
    private var lastQiblaRotation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQiblaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if sensors are available
        if (accelerometer == null || magnetometer == null) {
            binding.tvInstructions.text = getString(R.string.compass_not_available)
            return
        }

        // Request location permission and get user location
        requestLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listeners
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        magnetometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to save battery
        sensorManager.unregisterListener(this)
    }

    // Request location permission
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchUserLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation()
            } else {
                binding.tvLocation.text = getString(R.string.location_permission_required)
                binding.tvQiblaDirection.text = ""
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Fetch user location
    @SuppressLint("MissingPermission")
    private fun fetchUserLocation() {
        binding.progressBar.visibility = View.VISIBLE
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            binding.progressBar.visibility = View.GONE
            if (location != null) {
                userLatitude = location.latitude
                userLongitude = location.longitude
                
                // Calculate Qibla bearing
                qiblaBearing = calculateQiblaBearing(userLatitude, userLongitude)
                isLocationSet = true
                
                // Update location display
                updateLocationDisplay()
                
                // Update Qibla direction text
                updateQiblaDirection()
            } else {
                // Try to get location using location request
                requestLocationUpdate()
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            binding.tvLocation.text = getString(R.string.location_error)
            Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT)
                .show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    locationResult.lastLocation?.let { location ->
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        qiblaBearing = calculateQiblaBearing(userLatitude, userLongitude)
                        isLocationSet = true
                        updateLocationDisplay()
                        updateQiblaDirection()
                    }
                }
            },
            null
        )
    }

    private fun updateLocationDisplay() {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val city = addresses[0].locality ?: ""
                val country = addresses[0].countryName ?: ""
                binding.tvLocation.text = if (city.isNotEmpty() && country.isNotEmpty()) {
                    "$city, $country"
                } else {
                    String.format(Locale.getDefault(), "%.4f°N, %.4f°E", userLatitude, userLongitude)
                }
            } else {
                binding.tvLocation.text = String.format(
                    Locale.getDefault(),
                    "%.4f°N, %.4f°E",
                    userLatitude,
                    userLongitude
                )
            }
        } catch (e: Exception) {
            binding.tvLocation.text = String.format(
                Locale.getDefault(),
                "%.4f°N, %.4f°E",
                userLatitude,
                userLongitude
            )
        }
    }

    private fun updateQiblaDirection() {
        qiblaBearing?.let { bearing ->
            val directionText = String.format(
                Locale.getDefault(),
                getString(R.string.qibla_direction),
                bearing.toInt()
            )
            binding.tvQiblaDirection.text = directionText
        }
    }

    // Calculate bearing from user location to Mecca
    private fun calculateQiblaBearing(lat1: Double, lon1: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(MECCA_LATITUDE)
        val deltaLon = Math.toRadians(MECCA_LONGITUDE - lon1)

        val y = sin(deltaLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)

        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360 // Normalize to 0-360

        return bearing.toFloat()
    }

    // Sensor event listener
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
        }

        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun updateOrientationAngles() {
        // Get rotation matrix
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // Get orientation angles
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Azimuth is in radians, convert to degrees
        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        currentAzimuth = (azimuth + 360) % 360 // Normalize to 0-360

        // Rotate compass (compensate for device rotation)
        rotateCompass(-currentAzimuth)

        // Rotate Qibla indicator (relative to compass)
        if (isLocationSet && qiblaBearing != null) {
            val qiblaRotation = -currentAzimuth + qiblaBearing!!
            rotateQiblaIndicator(qiblaRotation)
        }
    }

    private fun rotateCompass(degrees: Float) {
        val rotation = RotateAnimation(
            lastCompassRotation,
            degrees,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        binding.compassImage.startAnimation(rotation)
        lastCompassRotation = degrees
    }

    private fun rotateQiblaIndicator(degrees: Float) {
        val rotation = RotateAnimation(
            lastQiblaRotation,
            degrees,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        binding.qiblaIndicator.startAnimation(rotation)
        lastQiblaRotation = degrees
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 200
    }
}

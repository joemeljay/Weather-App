package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.RetrofitClient
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val API_KEY = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}" // Replace with your OpenWeatherMap API key
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            } else -> {
                showError(getString(R.string.error_location_permission))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener {
            val cityName = binding.etCity.text.toString().trim()
            if (cityName.isEmpty()) {
                showError(getString(R.string.error_enter_valid_city))
                return@setOnClickListener
            }
            
            if (!isValidCityName(cityName)) {
                showError(getString(R.string.error_enter_valid_city))
                return@setOnClickListener
            }
            
            getWeatherByCity(cityName)
        }
        
        binding.btnGetLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun isValidCityName(cityName: String): Boolean {
        return cityName.matches(Regex("^[a-zA-Z\\s\\-']+$")) && cityName.length >= 2
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @Suppress("MissingPermission")
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        getWeatherByCoordinates(location.latitude, location.longitude)
                    } else {
                        requestNewLocationData()
                    }
                }
                .addOnFailureListener {
                    showError(getString(R.string.error_no_data_network))
                }
        } catch (e: SecurityException) {
            showError(getString(R.string.error_location_permission))
        }
    }

    @Suppress("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            showError(getString(R.string.error_location_permission))
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                getWeatherByCoordinates(location.latitude, location.longitude)
            }
        }
    }

    private fun getWeatherByCity(cityName: String) {
        showLoading(true)
        
        RetrofitClient.instance.getWeatherByCity(cityName, API_KEY)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    showLoading(false)
                    
                    if (response.isSuccessful) {
                        response.body()?.let { weatherResponse ->
                            displayWeatherData(weatherResponse)
                        } ?: run {
                            showError(getString(R.string.error_no_data_network))
                        }
                    } else {
                        when (response.code()) {
                            404 -> showError(getString(R.string.error_enter_valid_city))
                            else -> showError(getString(R.string.error_no_data_network))
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    showLoading(false)
                    showError(getString(R.string.error_no_data_network))
                }
            })
    }

    private fun getWeatherByCoordinates(latitude: Double, longitude: Double) {
        showLoading(true)
        
        RetrofitClient.instance.getWeatherByCoordinates(latitude, longitude, API_KEY)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    showLoading(false)
                    
                    if (response.isSuccessful) {
                        response.body()?.let { weatherResponse ->
                            displayWeatherData(weatherResponse)
                        } ?: run {
                            showError(getString(R.string.error_no_data_network))
                        }
                    } else {
                        showError(getString(R.string.error_no_data_network))
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    showLoading(false)
                    showError(getString(R.string.error_no_data_network))
                }
            })
    }

    private fun displayWeatherData(weather: WeatherResponse) {
        binding.apply {
            tvCityName.text = weather.name
            tvTemperature.text = "${weather.main.temp.toInt()}°C"
            tvCondition.text = weather.weather.firstOrNull()?.description?.capitalize() ?: ""
            tvHumidity.text = "${weather.main.humidity}%"
            tvWindSpeed.text = "${weather.wind.speed} m/s"
            
            scrollView.visibility = android.view.View.VISIBLE
            tvError.visibility = android.view.View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.scrollView.visibility = if (show) android.view.View.GONE else binding.scrollView.visibility
        binding.tvError.visibility = android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.apply {
            tvError.text = message
            tvError.visibility = android.view.View.VISIBLE
            scrollView.visibility = android.view.View.GONE
            progressBar.visibility = android.view.View.GONE
        }
    }
}

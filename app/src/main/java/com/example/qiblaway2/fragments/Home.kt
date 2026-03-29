package com.example.qiblaway2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.qiblaway2.R
import com.example.qiblaway2.adapter.prayerAdapter
import com.example.qiblaway2.api.RetrofitInstance
import com.example.qiblaway2.databinding.FragmentHomeBinding
import com.example.qiblaway2.model.Prayer
import com.example.qiblaway2.model.PrayerTimesResponse
import com.example.qiblaway2.model.Timings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Response
import java.util.Locale
import retrofit2.Callback
import org.json.JSONObject
import java.util.Date
import java.text.ParseException


class Home : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var prayerAdapter: prayerAdapter
    private lateinit var data: ArrayList<Prayer>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val prayerOrder = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")


    private val handler = Handler(Looper.getMainLooper())
    private var currentTimings: Timings? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding= FragmentHomeBinding.inflate(layoutInflater,container,false)
        // Hide progress bar initially
        binding.progressBar.visibility = View.GONE
        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Initialize list with placeholder times
        data=ArrayList<Prayer>()
        data.add(Prayer(1, getString(R.string.prayername1), "00:00", R.drawable.fajar))
        data.add(Prayer(2, getString(R.string.prayername2), "00:00", R.drawable.sunrise))
        data.add(Prayer(3, getString(R.string.prayername3), "00:00", R.drawable.dhuhr))
        data.add(Prayer(4, getString(R.string.prayername4), "00:00", R.drawable.asr))
        data.add(Prayer(5, getString(R.string.prayername5), "00:00", R.drawable.maghrib))
        data.add(Prayer(6, getString(R.string.prayername6), "00:00", R.drawable.isha))
        prayerAdapter= prayerAdapter(requireActivity(),data)
        binding.rvPrayers.adapter=prayerAdapter
        binding.rvPrayers.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL) //two columns
        //home menu (navigate to WallPaper&Language) ⋮
        homeMenu()
        //the code of selected background for home fragment from wallpaper
         selectedBackground()
        //API
        // Get prayer times dynamically
        requestLocationPermission()

        // أضف مستمع لنتيجة تغيير الإعدادات
        parentFragmentManager.setFragmentResultListener("prayer_settings_changed", viewLifecycleOwner) { _, _ ->
            // أعد تحميل المواقيت من جديد (تجاهل الكاش)
            fetchUserLocation(forceRefresh = true)
        }

        return binding.root
    }
    //the code of API...
    private fun getPrayerTimes(city: String, country: String, method: Int, school: Int) {
        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE
        
        // Get today's date in DD-MM-YYYY format for API
        val today = SimpleDateFormat("dd-MM-yyyy").format(Date())
        
        RetrofitInstance.api.getPrayerTimes(city, country, method, school, today).enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(call: Call<PrayerTimesResponse>, response: Response<PrayerTimesResponse>) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.data?.timings?.let { timings ->
                        updatePrayerTimes(timings)
                        // Save timings to SharedPreferences as JSON
                        saveTimingsToPrefs(timings)
                    } ?: run {
                        Toast.makeText(requireContext(), "No prayer times data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid request. Please check city and country names."
                        404 -> "City or country not found."
                        500 -> "Server error. Please try again later."
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    
                    // Try to load cached data if available
                    val (cachedTimings, _, _) = loadTimingsWithDateAndLocationFromPrefs()
                    if (cachedTimings != null) {
                        updatePrayerTimes(cachedTimings)
                    }
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                val errorMsg = t.message ?: "Unknown error"
                Toast.makeText(requireContext(), "Failed to get prayer times: $errorMsg", Toast.LENGTH_LONG).show()
                
                // Try to load cached data if available
                val (cachedTimings, _, _) = loadTimingsWithDateAndLocationFromPrefs()
                if (cachedTimings != null) {
                    updatePrayerTimes(cachedTimings)
                }
            }
        })
    }

    private fun updatePrayerTimes(timings: Timings) {
        // Update times in list
        data[0].hour = timings.Fajr
        data[1].hour = timings.Sunrise // Or adjust for Sunrise if needed
        data[2].hour = timings.Dhuhr
        data[3].hour = timings.Asr
        data[4].hour = timings.Maghrib
        data[5].hour = timings.Isha

        // Save current timings for left time calculation
        currentTimings = timings
        // Update left time immediately
        updateLeftTime()
        // Notify adapter about data change
        prayerAdapter.notifyDataSetChanged()
    }

    // Calculate and update the left time for the next prayer
    private fun updateLeftTime() {
        val timings = currentTimings ?: return
        val now = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm")
        var nextPrayerName: String? = null
        var minDiff: Long = Long.MAX_VALUE
        var nextPrayerTime: Calendar? = null
        for (prayer in prayerOrder) {
            val timeStr = when (prayer) {
                "Fajr" -> timings.Fajr
                "Sunrise" -> timings.Sunrise
                "Dhuhr" -> timings.Dhuhr
                "Asr" -> timings.Asr
                "Maghrib" -> timings.Maghrib
                "Isha" -> timings.Isha
                else -> null
            } ?: continue
            try {
                val cal = Calendar.getInstance()
                val date = sdf.parse(timeStr)
                cal.time = date
                // Set to today
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                cal.set(Calendar.MONTH, now.get(Calendar.MONTH))
                cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                // If the time already passed, skip
                if (cal.before(now)) continue
                val diff = cal.timeInMillis - now.timeInMillis
                if (diff in 1 until minDiff) {
                    minDiff = diff
                    nextPrayerName = prayer
                    nextPrayerTime = cal
                }
            } catch (e: ParseException) {
                continue
            }
        }
        if (nextPrayerName != null && nextPrayerTime != null) {
            val minutes = minDiff / 60000
            val hours = minutes / 60
            val mins = minutes % 60
            val isIn = getString(R.string.is_in)
            val hoursAnd = getString(R.string.hours_and)
            val minute = getString(R.string.minute)
            val timeStr = String.format("%02d:%02d", nextPrayerTime.get(Calendar.HOUR_OF_DAY), nextPrayerTime.get(Calendar.MINUTE))

            // ربط اسم الصلاة بالاسم المترجم
            val displayName = when (nextPrayerName) {
                "Fajr" -> getString(R.string.prayername1)
                "Sunrise" -> getString(R.string.prayername2)
                "Dhuhr" -> getString(R.string.prayername3)
                "Asr" -> getString(R.string.prayername4)
                "Maghrib" -> getString(R.string.prayername5)
                "Isha" -> getString(R.string.prayername6)
                else -> nextPrayerName ?: ""
            }

            val display = if (hours > 0) {
                "$displayName $isIn $hours $hoursAnd $mins $minute"
            } else {
                "$displayName $isIn $mins $minute"
            }
            binding.leftTime.text = display
        } else {
            // Calculate time to tomorrow's Fajr
            val sdf = SimpleDateFormat("HH:mm")
            val now = Calendar.getInstance()
            val fajrTimeStr = timings.Fajr
            val fajrCal = Calendar.getInstance()
            val fajrDate = sdf.parse(fajrTimeStr)
            fajrCal.time = fajrDate
            fajrCal.set(Calendar.YEAR, now.get(Calendar.YEAR))
            fajrCal.set(Calendar.MONTH, now.get(Calendar.MONTH))
            fajrCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + 1) // tomorrow

            val diff = fajrCal.timeInMillis - now.timeInMillis
            val minutes = diff / 60000
            val hours = minutes / 60
            val mins = minutes % 60
            val isIn = getString(R.string.is_in)
            val hoursAnd = getString(R.string.hours_and)
            val minute = getString(R.string.minute)
            val timeStr = String.format("%02d:%02d", fajrCal.get(Calendar.HOUR_OF_DAY), fajrCal.get(Calendar.MINUTE))
            val display = "${getString(R.string.prayername1)} $isIn $hours $hoursAnd $mins $minute"
            binding.leftTime.text = display
        }
    }

    // Save timings to SharedPreferences as JSON, with date and location
    private fun saveTimingsToPrefs(timings: Timings, city: String? = null, country: String? = null) {
        val sharedPrefs = requireContext().getSharedPreferences("prayer_times_cache", Context.MODE_PRIVATE)
        val json = JSONObject()
        json.put("Fajr", timings.Fajr)
        json.put("Sunrise", timings.Sunrise)
        json.put("Dhuhr", timings.Dhuhr)
        json.put("Asr", timings.Asr)
        json.put("Maghrib", timings.Maghrib)
        json.put("Isha", timings.Isha)
        // Save today's date
        val today = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val editor = sharedPrefs.edit()
        editor.putString("timings_json", json.toString())
        editor.putString("timings_date", today)
        if (city != null && country != null) {
            editor.putString("city", city)
            editor.putString("country", country)
        }
        editor.apply()
    }

    // Load timings, date, and location from SharedPreferences
    private fun loadTimingsWithDateAndLocationFromPrefs(): Triple<Timings?, String?, Pair<String?, String?>> {
        val sharedPrefs = requireContext().getSharedPreferences("prayer_times_cache", Context.MODE_PRIVATE)
        val jsonString = sharedPrefs.getString("timings_json", null)
        val dateString = sharedPrefs.getString("timings_date", null)
        val city = sharedPrefs.getString("city", null)
        val country = sharedPrefs.getString("country", null)
        val timings = if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)
                Timings(
                    json.getString("Fajr"),
                    json.getString("Sunrise"),
                    json.getString("Dhuhr"),
                    json.getString("Asr"),
                    json.getString("Maghrib"),
                    json.getString("Isha")
                )
            } catch (e: Exception) {
                null
            }
        } else null
        return Triple(timings, dateString, Pair(city, country))
    }

    //END OF API
    // *************************************


    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable) // Start updating the clock when fragment is visible
        handler.post(updateDateRunnable) // Start updating the date
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable) // Stop updating when fragment is not visible
        handler.removeCallbacks(updateDateRunnable) // Stop updates when fragment is hidden
    }

    // Use Handler and Runnable to update the current time dynamically
    private val updateTimeRunnable = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
            val minute = calendar.get(Calendar.MINUTE)

            binding.currentHour.text = String.format("%02d:%02d", hour, minute)

            // Update left time every minute
            updateLeftTime()

            // Schedule next update at the start of the next minute
            val delay = 60000 - (calendar.get(Calendar.SECOND) * 1000)
            handler.postDelayed(this, delay.toLong())
        }
    }

    // Runnable to update the date at exactly midnight
    private val updateDateRunnable = object : Runnable {
        override fun run() {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)

            binding.currentDate.text = formattedDate // Assuming you have a TextView with this ID

            // Calculate the delay until next midnight (00:00:00)
            val now = System.currentTimeMillis()
            val nextMidnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val delay = nextMidnight - now // Time difference until midnight
            handler.postDelayed(this, delay)
        }
    }
    //*******
    fun selectedBackground(){
        val sharedPref = requireActivity().getSharedPreferences("selectedBackground",MODE_PRIVATE)
        val selectedImage = sharedPref.getInt("selectedImage", R.drawable.jerusalem1)
        binding.root.setBackgroundResource(selectedImage)
    }
    //******
    fun homeMenu(){
        binding.menu.setOnClickListener {
            val popupmenu=PopupMenu(context,binding.menu)
            popupmenu.menuInflater.inflate(R.menu.homemenu,popupmenu.menu)

            popupmenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.Language -> {
                        findNavController().navigate(R.id.LanguageFragment) // Navigate properly
                    }R.id.Wallpaper -> {

                    findNavController().navigate(R.id.WallpaperFragment) // Navigate properly

                }R.id.prayerTimesSettings->{
                    val prayerTimesSettings= PrayerTimesSettings()
                    prayerTimesSettings.show(parentFragmentManager, "BottomSheetTag")

                }
                }

                true
            }
            popupmenu.show()


        }
    }
    //***Dynamically detects user's location and update
    // ✅ REQUEST LOCATION PERMISSION
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            fetchUserLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    //FETCH USER LOCATION & CITY
    @SuppressLint("MissingPermission") // Ensure permissions are granted before calling this
    private fun fetchUserLocation(forceRefresh: Boolean = false) {
        val today = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val (cachedTimings, cachedDate, cachedLocation) = loadTimingsWithDateAndLocationFromPrefs()
        if (!forceRefresh && cachedTimings != null && cachedDate == today) {
            // Show cached data for today, no API call, no progress bar
            binding.progressBar.visibility = View.GONE
            updatePrayerTimes(cachedTimings)
            // Show cached location if available
            val (city, country) = cachedLocation
            if (!city.isNullOrEmpty() && !country.isNullOrEmpty()) {
                binding.tvLocation.text = "$city,$country"
            }
            return
        }
        if (!isInternetAvailable(requireContext())) {
            // Hide progress bar if offline
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.no_internet_message), Toast.LENGTH_LONG).show()
            if (cachedTimings != null) {
                updatePrayerTimes(cachedTimings)
                // Show cached location if available
                val (city, country) = cachedLocation
                if (!city.isNullOrEmpty() && !country.isNullOrEmpty()) {
                    binding.tvLocation.text = "$city,$country"
                }
            } else {
                Toast.makeText(requireContext(), "No cached prayer times available.", Toast.LENGTH_LONG).show()
            }
            return
        }
        // Show progress bar before fetching location and data
        binding.progressBar.visibility = View.VISIBLE
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    // Use English locale to get city and country names in English for API
                    val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses?.isNotEmpty() == true) {
                        val address = addresses[0]
                        // Try multiple fields to get city name (in order of preference)
                        var city = address.locality
                        if (city.isNullOrEmpty()) {
                            city = address.subAdminArea // District or region
                        }
                        if (city.isNullOrEmpty()) {
                            city = address.adminArea // State or province
                        }
                        if (city.isNullOrEmpty()) {
                            city = address.featureName // Feature name as last resort
                        }
                        
                        val country = address.countryName ?: ""
                        
                        // Clean city and country names (remove extra spaces, special characters)
                        city = city?.trim()?.replace(Regex("[^a-zA-Z0-9\\s-]"), "") ?: ""
                        val cleanCountry = country.trim().replace(Regex("[^a-zA-Z0-9\\s-]"), "")
                        
                        // Display location (can use local language for display)
                        val displayGeocoder = Geocoder(requireContext(), Locale.getDefault())
                        val displayAddresses = displayGeocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val displayCity = displayAddresses?.getOrNull(0)?.locality ?: city
                        val displayCountry = displayAddresses?.getOrNull(0)?.countryName ?: cleanCountry
                        binding.tvLocation.setText("$displayCity,$displayCountry")
                        
                        // قراءة القيم من SharedPreferences
                        val sharedPrefs = requireContext().getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)
                        val selectedMethodId = sharedPrefs.getInt("selected_method_id", 2)
                        val selectedMadhhab = sharedPrefs.getInt("selected_madhhab", 1)
                        
                        // Use coordinates directly (more reliable than city/country names)
                        // This avoids issues with city name translations and API database mismatches
                        getPrayerTimesByCoordinates(
                            location.latitude, 
                            location.longitude, 
                            selectedMethodId, 
                            selectedMadhhab,
                            city.takeIf { it.isNotEmpty() },
                            cleanCountry.takeIf { cleanCountry.isNotEmpty() }
                        )
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Could not get address from location", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Geocoder error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                // Hide progress bar if location not found
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Hide progress bar if location fetch fails
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Location error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // دالة جديدة لتمرير الموقع مع المواقيت
    private fun getPrayerTimesWithLocation(city: String, country: String, method: Int, school: Int) {
        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE
        
        // Get today's date in DD-MM-YYYY format for API
        val today = SimpleDateFormat("dd-MM-yyyy").format(Date())
        
        RetrofitInstance.api.getPrayerTimes(city, country, method, school, today).enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(call: Call<PrayerTimesResponse>, response: Response<PrayerTimesResponse>) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.data?.timings?.let { timings ->
                        updatePrayerTimes(timings)
                        // Save timings and location to SharedPreferences as JSON
                        saveTimingsToPrefs(timings, city, country)
                    } ?: run {
                        Toast.makeText(requireContext(), "No prayer times data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid request for city: '$city', country: '$country'. Please check the names."
                        404 -> "City '$city' or country '$country' not found in API database."
                        500 -> "Server error. Please try again later."
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    
                    // Log the actual error body if available
                    try {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("PrayerTimes", "API Error: $errorBody")
                    } catch (e: Exception) {
                        android.util.Log.e("PrayerTimes", "Error reading response body: ${e.message}")
                    }
                    
                    // Try to load cached data if available
                    val (cachedTimings, _, cachedLocation) = loadTimingsWithDateAndLocationFromPrefs()
                    if (cachedTimings != null) {
                        updatePrayerTimes(cachedTimings)
                        val (cachedCity, cachedCountry) = cachedLocation
                        if (!cachedCity.isNullOrEmpty() && !cachedCountry.isNullOrEmpty()) {
                            binding.tvLocation.text = "$cachedCity,$cachedCountry"
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                val errorMsg = t.message ?: "Unknown error"
                Toast.makeText(requireContext(), "Failed to get prayer times: $errorMsg", Toast.LENGTH_LONG).show()
                
                // Try to load cached data if available
                val (cachedTimings, _, cachedLocation) = loadTimingsWithDateAndLocationFromPrefs()
                if (cachedTimings != null) {
                    updatePrayerTimes(cachedTimings)
                    val (cachedCity, cachedCountry) = cachedLocation
                    if (!cachedCity.isNullOrEmpty() && !cachedCountry.isNullOrEmpty()) {
                        binding.tvLocation.text = "$cachedCity,$cachedCountry"
                    }
                }
            }
        })
    }
    
    // دالة بديلة تستخدم الإحداثيات مباشرة (أكثر موثوقية)
    private fun getPrayerTimesByCoordinates(latitude: Double, longitude: Double, method: Int, school: Int, city: String? = null, country: String? = null) {
        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE
        
        // Get today's date in DD-MM-YYYY format for API
        val today = SimpleDateFormat("dd-MM-yyyy").format(Date())
        
        RetrofitInstance.api.getPrayerTimesByCoordinates(latitude, longitude, method, school, today).enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(call: Call<PrayerTimesResponse>, response: Response<PrayerTimesResponse>) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.data?.timings?.let { timings ->
                        updatePrayerTimes(timings)
                        // Save timings and location to SharedPreferences as JSON
                        saveTimingsToPrefs(timings, city, country)
                    } ?: run {
                        Toast.makeText(requireContext(), "No prayer times data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid coordinates."
                        404 -> "Location not found."
                        500 -> "Server error. Please try again later."
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    
                    // Try to load cached data if available
                    val (cachedTimings, _, cachedLocation) = loadTimingsWithDateAndLocationFromPrefs()
                    if (cachedTimings != null) {
                        updatePrayerTimes(cachedTimings)
                        val (cachedCity, cachedCountry) = cachedLocation
                        if (!cachedCity.isNullOrEmpty() && !cachedCountry.isNullOrEmpty()) {
                            binding.tvLocation.text = "$cachedCity,$cachedCountry"
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                // Hide progress bar
                binding.progressBar.visibility = View.GONE
                val errorMsg = t.message ?: "Unknown error"
                Toast.makeText(requireContext(), "Failed to get prayer times: $errorMsg", Toast.LENGTH_LONG).show()
                
                // Try to load cached data if available
                val (cachedTimings, _, cachedLocation) = loadTimingsWithDateAndLocationFromPrefs()
                if (cachedTimings != null) {
                    updatePrayerTimes(cachedTimings)
                    val (cachedCity, cachedCountry) = cachedLocation
                    if (!cachedCity.isNullOrEmpty() && !cachedCountry.isNullOrEmpty()) {
                        binding.tvLocation.text = "$cachedCity,$cachedCountry"
                    }
                }
            }
        })
    }

    // Check for internet connection
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}








